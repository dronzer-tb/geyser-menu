package com.geysermenu.extension.forms;

import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.component.ButtonComponent;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Base class for creating Bedrock menus using Cumulus SimpleForm
 */
public abstract class BaseMenu {

    protected String title;
    protected String content;
    protected final List<MenuButton> buttons = new ArrayList<>();

    public BaseMenu(String title) {
        this.title = title;
        this.content = "";
    }

    public BaseMenu(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * Override this to add buttons to the menu
     */
    protected abstract void buildMenu();

    /**
     * Called when the menu is closed without selecting a button
     */
    protected void onClose(GeyserConnection connection) {
        // Override if needed
    }

    /**
     * Adds a button to the menu
     */
    protected void addButton(String text, Runnable onClick) {
        buttons.add(new MenuButton(text, null, onClick));
    }

    /**
     * Adds a button with an image to the menu
     */
    protected void addButton(String text, String imageUrl, Runnable onClick) {
        buttons.add(new MenuButton(text, imageUrl, onClick));
    }

    /**
     * Sends this menu to the player
     */
    public void send(GeyserConnection connection) {
        buttons.clear();
        buildMenu();

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(title)
                .content(content);

        for (MenuButton button : buttons) {
            if (button.imageUrl != null && !button.imageUrl.isEmpty()) {
                if (button.imageUrl.startsWith("http")) {
                    builder.button(button.text, FormImage.Type.URL, button.imageUrl);
                } else {
                    builder.button(button.text, FormImage.Type.PATH, button.imageUrl);
                }
            } else {
                builder.button(button.text);
            }
        }

        builder.validResultHandler(response -> {
            int clickedId = response.clickedButtonId();
            if (clickedId >= 0 && clickedId < buttons.size()) {
                MenuButton clicked = buttons.get(clickedId);
                if (clicked.onClick != null) {
                    clicked.onClick.run();
                }
            }
        });

        builder.closedOrInvalidResultHandler(response -> {
            onClose(connection);
        });

        connection.sendForm(builder.build());
    }

    protected record MenuButton(String text, String imageUrl, Runnable onClick) {}
}
