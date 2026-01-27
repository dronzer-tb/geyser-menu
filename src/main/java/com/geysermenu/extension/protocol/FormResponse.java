package com.geysermenu.extension.protocol;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a form response sent back to the companion plugin.
 * This is serialized as JSON over the TCP connection.
 * 
 * Credits: Based on FormsAPI by DronzerStudios (https://dronzerstudios.tech/)
 */
public class FormResponse {

    /**
     * The original request ID
     */
    @SerializedName("request_id")
    private String requestId;

    /**
     * The player's UUID
     */
    @SerializedName("player_uuid")
    private String playerUuid;

    /**
     * Response type: "success", "closed", "invalid", "error"
     */
    @SerializedName("response_type")
    private String responseType;

    /**
     * For simple forms: the clicked button index (0-based)
     * For modal forms: 0 = button1 (confirm), 1 = button2 (deny)
     */
    @SerializedName("clicked_button")
    private int clickedButton = -1;

    /**
     * For modal forms: true if button1 was clicked
     */
    @SerializedName("confirmed")
    private boolean confirmed;

    /**
     * Error message if response_type is "error"
     */
    @SerializedName("error_message")
    private String errorMessage;

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public int getClickedButton() {
        return clickedButton;
    }

    public void setClickedButton(int clickedButton) {
        this.clickedButton = clickedButton;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Helper methods
    public boolean isSuccess() {
        return "success".equals(responseType);
    }

    public boolean isClosed() {
        return "closed".equals(responseType);
    }

    public boolean isError() {
        return "error".equals(responseType);
    }

    // Static factory methods for common responses
    public static FormResponse success(String requestId, String playerUuid, int clickedButton) {
        FormResponse response = new FormResponse();
        response.setRequestId(requestId);
        response.setPlayerUuid(playerUuid);
        response.setResponseType("success");
        response.setClickedButton(clickedButton);
        return response;
    }

    public static FormResponse modalSuccess(String requestId, String playerUuid, boolean confirmed) {
        FormResponse response = new FormResponse();
        response.setRequestId(requestId);
        response.setPlayerUuid(playerUuid);
        response.setResponseType("success");
        response.setConfirmed(confirmed);
        response.setClickedButton(confirmed ? 0 : 1);
        return response;
    }

    public static FormResponse closed(String requestId, String playerUuid) {
        FormResponse response = new FormResponse();
        response.setRequestId(requestId);
        response.setPlayerUuid(playerUuid);
        response.setResponseType("closed");
        return response;
    }

    public static FormResponse error(String requestId, String playerUuid, String errorMessage) {
        FormResponse response = new FormResponse();
        response.setRequestId(requestId);
        response.setPlayerUuid(playerUuid);
        response.setResponseType("error");
        response.setErrorMessage(errorMessage);
        return response;
    }
}
