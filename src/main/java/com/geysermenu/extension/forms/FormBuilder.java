package com.geysermenu.extension.forms;

import com.geysermenu.extension.protocol.FormRequest;
import com.geysermenu.extension.protocol.FormResponse;
import org.geysermc.cumulus.form.Form;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.util.FormImage;
import org.geysermc.geyser.session.GeyserSession;

import java.util.function.Consumer;

/**
 * Builds Cumulus forms from FormRequest objects received via TCP.
 * 
 * Credits: Based on FormsAPI by DronzerStudios (https://dronzerstudios.tech/)
 */
public class FormBuilder {

    /**
     * Build a form based on the request type.
     * 
     * @param request The form request from the companion plugin
     * @param responseHandler Handler for form responses
     * @param session The player's Geyser session
     * @return The built form ready to be sent
     */
    public static Form buildForm(FormRequest request, Consumer<FormResponse> responseHandler, GeyserSession session) {
        String formType = request.getFormType();

        if (formType == null) {
            formType = "simple";
        }

        switch (formType.toLowerCase()) {
            case "modal":
                return buildModalForm(request, responseHandler);
            case "simple":
            default:
                return buildSimpleForm(request, responseHandler, session);
        }
    }

    /**
     * Build a SimpleForm (button list).
     */
    private static SimpleForm buildSimpleForm(FormRequest request, Consumer<FormResponse> responseHandler, GeyserSession session) {
        SimpleForm.Builder builder = SimpleForm.builder()
                .title(request.getTitle() != null ? request.getTitle() : "")
                .content(request.getContent() != null ? request.getContent() : "");

        // Add buttons
        if (request.getButtons() != null) {
            for (FormRequest.FormButton btn : request.getButtons()) {
                if (btn.getImageType() != null && btn.getImageData() != null) {
                    FormImage.Type imageType = "url".equalsIgnoreCase(btn.getImageType())
                            ? FormImage.Type.URL
                            : FormImage.Type.PATH;
                    builder.button(btn.getText(), imageType, btn.getImageData());
                } else {
                    builder.button(btn.getText());
                }
            }
        }

        // Handle valid result (button clicked)
        builder.validResultHandler(response -> {
            int clickedId = response.clickedButtonId();
            FormResponse formResponse = FormResponse.success(
                    request.getRequestId(),
                    request.getPlayerUuid(),
                    clickedId
            );
            responseHandler.accept(formResponse);
            
            // Execute button-specific command if defined
            if (request.getButtons() != null && clickedId < request.getButtons().size()) {
                FormRequest.FormButton clickedButton = request.getButtons().get(clickedId);
                if (clickedButton.getCommand() != null && !clickedButton.getCommand().isEmpty()) {
                    session.sendCommand(clickedButton.getCommand());
                }
            }
        });

        // Handle closed/invalid
        builder.closedOrInvalidResultHandler(() -> {
            FormResponse formResponse = FormResponse.closed(
                    request.getRequestId(),
                    request.getPlayerUuid()
            );
            responseHandler.accept(formResponse);
        });

        return builder.build();
    }

    /**
     * Build a ModalForm (Yes/No dialog).
     */
    private static ModalForm buildModalForm(FormRequest request, Consumer<FormResponse> responseHandler) {
        ModalForm.Builder builder = ModalForm.builder()
                .title(request.getTitle() != null ? request.getTitle() : "")
                .content(request.getContent() != null ? request.getContent() : "")
                .button1(request.getButton1() != null ? request.getButton1() : "Confirm")
                .button2(request.getButton2() != null ? request.getButton2() : "Cancel");

        // Handle valid result
        builder.validResultHandler(response -> {
            // response.clickedFirst() returns true if button1 was clicked
            boolean confirmed = response.clickedFirst();
            FormResponse formResponse = FormResponse.modalSuccess(
                    request.getRequestId(),
                    request.getPlayerUuid(),
                    confirmed
            );
            responseHandler.accept(formResponse);
        });

        // Handle closed/invalid
        builder.closedOrInvalidResultHandler(() -> {
            FormResponse formResponse = FormResponse.closed(
                    request.getRequestId(),
                    request.getPlayerUuid()
            );
            responseHandler.accept(formResponse);
        });

        return builder.build();
    }
}
