package com.geysermenu.extension.network.protocol;

import java.util.Map;
import java.util.UUID;

/**
 * Represents the response from a menu interaction
 */
public class MenuResponse {

    private String formId;
    private UUID playerUuid;
    private ResponseType responseType;
    private int buttonId;
    private String buttonText;
    private Map<String, Object> formData;

    public MenuResponse() {}

    public MenuResponse(String formId, UUID playerUuid, ResponseType responseType, int buttonId, String buttonText, Map<String, Object> formData) {
        this.formId = formId;
        this.playerUuid = playerUuid;
        this.responseType = responseType;
        this.buttonId = buttonId;
        this.buttonText = buttonText;
        this.formData = formData;
    }

    public enum ResponseType {
        BUTTON_CLICK,   // SimpleForm or ModalForm button clicked
        FORM_SUBMIT,    // CustomForm submitted
        CLOSED          // Form closed without action
    }

    public String getFormId() {
        return formId;
    }

    public void setFormId(String formId) {
        this.formId = formId;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(UUID playerUuid) {
        this.playerUuid = playerUuid;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public int getButtonId() {
        return buttonId;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }

    public Map<String, Object> getFormData() {
        return formData;
    }

    public void setFormData(Map<String, Object> formData) {
        this.formData = formData;
    }
}
