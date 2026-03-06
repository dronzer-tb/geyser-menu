package com.geysermenu.extension.protocol;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.UUID;

/**
 * Represents a form request sent from a Spigot plugin to GeyserMenu.
 * This is serialized as JSON over the TCP connection.
 * 
 * Credits: Based on FormsAPI by DronzerStudios (https://dronzerstudios.tech/)
 */
public class FormRequest {

    /**
     * Unique request ID for tracking responses
     */
    @SerializedName("request_id")
    private String requestId;

    /**
     * The UUID of the player to show the form to (Java UUID)
     */
    @SerializedName("player_uuid")
    private String playerUuid;

    /**
     * Type of form: "simple", "modal", or "custom"
     */
    @SerializedName("form_type")
    private String formType;

    /**
     * Form title
     */
    @SerializedName("title")
    private String title;

    /**
     * Form content/body text
     */
    @SerializedName("content")
    private String content;

    /**
     * Buttons for simple forms
     */
    @SerializedName("buttons")
    private List<FormButton> buttons;

    /**
     * Button 1 text for modal forms (confirm/yes)
     */
    @SerializedName("button1")
    private String button1;

    /**
     * Button 2 text for modal forms (deny/no)
     */
    @SerializedName("button2")
    private String button2;

    /**
     * Command to execute when button1/confirm is clicked
     */
    @SerializedName("command_accept")
    private String commandAccept;

    /**
     * Command to execute when button2/deny is clicked
     */
    @SerializedName("command_deny")
    private String commandDeny;

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

    public UUID getPlayerUuidAsUUID() {
        return playerUuid != null ? UUID.fromString(playerUuid) : null;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getFormType() {
        return formType;
    }

    public void setFormType(String formType) {
        this.formType = formType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<FormButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<FormButton> buttons) {
        this.buttons = buttons;
    }

    public String getButton1() {
        return button1;
    }

    public void setButton1(String button1) {
        this.button1 = button1;
    }

    public String getButton2() {
        return button2;
    }

    public void setButton2(String button2) {
        this.button2 = button2;
    }

    public String getCommandAccept() {
        return commandAccept;
    }

    public void setCommandAccept(String commandAccept) {
        this.commandAccept = commandAccept;
    }

    public String getCommandDeny() {
        return commandDeny;
    }

    public void setCommandDeny(String commandDeny) {
        this.commandDeny = commandDeny;
    }

    /**
     * Button definition for simple forms
     */
    public static class FormButton {
        @SerializedName("text")
        private String text;

        @SerializedName("image_type")
        private String imageType; // "path" or "url"

        @SerializedName("image_data")
        private String imageData;

        @SerializedName("command")
        private String command; // Command to execute when button is clicked

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getImageType() {
            return imageType;
        }

        public void setImageType(String imageType) {
            this.imageType = imageType;
        }

        public String getImageData() {
            return imageData;
        }

        public void setImageData(String imageData) {
            this.imageData = imageData;
        }

        public String getCommand() {
            return command;
        }

        public void setCommand(String command) {
            this.command = command;
        }
    }
}
