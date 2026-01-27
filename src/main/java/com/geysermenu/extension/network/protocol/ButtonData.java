package com.geysermenu.extension.network.protocol;

import java.util.List;

/**
 * Data class for transmitting button information between companion and extension
 */
public class ButtonData {
    
    private String id;
    private String text;
    private String imageUrl;
    private String imagePath;
    private int priority;
    
    public ButtonData() {}
    
    public ButtonData(String id, String text, String imageUrl, String imagePath, int priority) {
        this.id = id;
        this.text = text;
        this.imageUrl = imageUrl;
        this.imagePath = imagePath;
        this.priority = priority;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public String getImagePath() {
        return imagePath;
    }
    
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
    
    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
        this.priority = priority;
    }
    
    public boolean hasImage() {
        return imageUrl != null || imagePath != null;
    }
    
    public boolean isUrlImage() {
        return imageUrl != null;
    }
    
    /**
     * Wrapper for a list of buttons
     */
    public static class ButtonList {
        private List<ButtonData> buttons;
        
        public ButtonList() {}
        
        public ButtonList(List<ButtonData> buttons) {
            this.buttons = buttons;
        }
        
        public List<ButtonData> getButtons() {
            return buttons;
        }
        
        public void setButtons(List<ButtonData> buttons) {
            this.buttons = buttons;
        }
    }
    
    /**
     * Data for button click event
     */
    public static class ButtonClick {
        private String buttonId;
        private String playerUuid;
        private String playerName;
        private String xuid;
        
        public ButtonClick() {}
        
        public ButtonClick(String buttonId, String playerUuid, String playerName, String xuid) {
            this.buttonId = buttonId;
            this.playerUuid = playerUuid;
            this.playerName = playerName;
            this.xuid = xuid;
        }
        
        public String getButtonId() {
            return buttonId;
        }
        
        public void setButtonId(String buttonId) {
            this.buttonId = buttonId;
        }
        
        public String getPlayerUuid() {
            return playerUuid;
        }
        
        public void setPlayerUuid(String playerUuid) {
            this.playerUuid = playerUuid;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public void setPlayerName(String playerName) {
            this.playerName = playerName;
        }
        
        public String getXuid() {
            return xuid;
        }
        
        public void setXuid(String xuid) {
            this.xuid = xuid;
        }
    }
}
