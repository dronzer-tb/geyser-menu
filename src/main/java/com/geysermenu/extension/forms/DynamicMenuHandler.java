package com.geysermenu.extension.forms;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.network.protocol.MenuData;
import com.geysermenu.extension.network.protocol.MenuResponse;
import com.geysermenu.extension.network.ClientHandler;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.UUID;

/**
 * Handles dynamic menus sent from the companion plugin
 */
public class DynamicMenuHandler {

    private final GeyserMenuExtension extension;

    public DynamicMenuHandler(GeyserMenuExtension extension) {
        this.extension = extension;
    }

    /**
     * Sends a dynamic menu to a player based on MenuData from companion plugin
     */
    public void sendMenu(GeyserConnection connection, MenuData menuData, ClientHandler sourceClient) {
        switch (menuData.getType()) {
            case SIMPLE -> sendSimpleForm(connection, menuData, sourceClient);
            case MODAL -> sendModalForm(connection, menuData, sourceClient);
            case CUSTOM -> sendCustomForm(connection, menuData, sourceClient);
        }
    }

    private void sendSimpleForm(GeyserConnection connection, MenuData menuData, ClientHandler sourceClient) {
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(menuData.getTitle())
                .content(menuData.getContent() != null ? menuData.getContent() : "");

        for (MenuData.Button button : menuData.getButtons()) {
            if (button.imageUrl() != null && !button.imageUrl().isEmpty()) {
                if (button.imageUrl().startsWith("http")) {
                    builder.button(button.text(), FormImage.Type.URL, button.imageUrl());
                } else {
                    builder.button(button.text(), FormImage.Type.PATH, button.imageUrl());
                }
            } else {
                builder.button(button.text());
            }
        }

        builder.validResultHandler(response -> {
            int clickedId = response.clickedButtonId();
            String buttonText = clickedId >= 0 && clickedId < menuData.getButtons().size()
                    ? menuData.getButtons().get(clickedId).text()
                    : "";

            MenuResponse menuResponse = new MenuResponse(
                    menuData.getFormId(),
                    connection.javaUuid(),
                    MenuResponse.ResponseType.BUTTON_CLICK,
                    clickedId,
                    buttonText,
                    null
            );

            sourceClient.sendResponse(menuResponse);
        });

        builder.closedOrInvalidResultHandler(response -> {
            MenuResponse menuResponse = new MenuResponse(
                    menuData.getFormId(),
                    connection.javaUuid(),
                    MenuResponse.ResponseType.CLOSED,
                    -1,
                    null,
                    null
            );

            sourceClient.sendResponse(menuResponse);
        });

        connection.sendForm(builder.build());
    }

    private void sendModalForm(GeyserConnection connection, MenuData menuData, ClientHandler sourceClient) {
        String button1 = menuData.getButtons().size() > 0 ? menuData.getButtons().get(0).text() : "Yes";
        String button2 = menuData.getButtons().size() > 1 ? menuData.getButtons().get(1).text() : "No";

        ModalForm.Builder builder = ModalForm.builder()
                .title(menuData.getTitle())
                .content(menuData.getContent() != null ? menuData.getContent() : "")
                .button1(button1)
                .button2(button2);

        builder.validResultHandler(response -> {
            int clickedId = response.clickedButtonId();
            String buttonText = clickedId == 0 ? button1 : button2;

            MenuResponse menuResponse = new MenuResponse(
                    menuData.getFormId(),
                    connection.javaUuid(),
                    MenuResponse.ResponseType.BUTTON_CLICK,
                    clickedId,
                    buttonText,
                    null
            );

            sourceClient.sendResponse(menuResponse);
        });

        builder.closedOrInvalidResultHandler(response -> {
            MenuResponse menuResponse = new MenuResponse(
                    menuData.getFormId(),
                    connection.javaUuid(),
                    MenuResponse.ResponseType.CLOSED,
                    -1,
                    null,
                    null
            );

            sourceClient.sendResponse(menuResponse);
        });

        connection.sendForm(builder.build());
    }

    private void sendCustomForm(GeyserConnection connection, MenuData menuData, ClientHandler sourceClient) {
        CustomForm.Builder builder = CustomForm.builder()
                .title(menuData.getTitle());

        // Add components based on menuData
        for (MenuData.FormComponent component : menuData.getComponents()) {
            switch (component.type()) {
                case LABEL -> builder.label(component.text());
                case INPUT -> builder.input(component.text(), component.placeholder(), component.defaultValue());
                case TOGGLE -> builder.toggle(component.text(), Boolean.parseBoolean(component.defaultValue()));
                case SLIDER -> builder.slider(component.text(),
                        component.min(), component.max(), component.step(),
                        (int) component.min());
                case DROPDOWN -> builder.dropdown(component.text(), component.options().toArray(new String[0]));
                case STEP_SLIDER -> builder.stepSlider(component.text(), component.options().toArray(new String[0]));
            }
        }

        builder.validResultHandler(response -> {
            // Collect all responses
            java.util.Map<String, Object> responses = new java.util.HashMap<>();
            for (int i = 0; i < menuData.getComponents().size(); i++) {
                MenuData.FormComponent comp = menuData.getComponents().get(i);
                Object value = switch (comp.type()) {
                    case LABEL -> null;
                    case INPUT -> response.asInput(i);
                    case TOGGLE -> response.asToggle(i);
                    case SLIDER -> response.asSlider(i);
                    case DROPDOWN -> response.asDropdown(i);
                    case STEP_SLIDER -> response.asStepSlider(i);
                };
                if (value != null) {
                    responses.put(comp.id(), value);
                }
            }

            MenuResponse menuResponse = new MenuResponse(
                    menuData.getFormId(),
                    connection.javaUuid(),
                    MenuResponse.ResponseType.FORM_SUBMIT,
                    -1,
                    null,
                    responses
            );

            sourceClient.sendResponse(menuResponse);
        });

        builder.closedOrInvalidResultHandler(response -> {
            MenuResponse menuResponse = new MenuResponse(
                    menuData.getFormId(),
                    connection.javaUuid(),
                    MenuResponse.ResponseType.CLOSED,
                    -1,
                    null,
                    null
            );

            sourceClient.sendResponse(menuResponse);
        });

        connection.sendForm(builder.build());
    }
}
