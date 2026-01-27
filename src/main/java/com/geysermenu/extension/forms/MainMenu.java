package com.geysermenu.extension.forms;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.network.ButtonManager;
import com.geysermenu.extension.network.ClientHandler;
import com.geysermenu.extension.network.protocol.ButtonData;
import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.List;

/**
 * The main menu shown when players double-click their inventory
 */
public class MainMenu extends BaseMenu {

    private final GeyserMenuExtension extension;
    private GeyserConnection connection;

    public MainMenu(GeyserMenuExtension extension) {
        super(extension.config().getDefaultMenuTitle());
        this.extension = extension;
    }

    @Override
    protected void buildMenu() {
        // Add buttons registered from companion plugins (sorted by priority)
        ButtonManager buttonManager = extension.getMenuServer().getButtonManager();
        List<ButtonData> registeredButtons = buttonManager.getAllButtons();
        
        for (ButtonData buttonData : registeredButtons) {
            String imageUrl = buttonData.getImageUrl();
            String imagePath = buttonData.getImagePath();
            String image = (imageUrl != null) ? imageUrl : imagePath;
            
            final String buttonId = buttonData.getId();
            addButton(buttonData.getText(), image, () -> {
                handleButtonClick(buttonId);
            });
        }
        
        // Add default Settings button at the end
        addButton("Settings", () -> {
            new SettingsMenu(extension).send(connection);
        });
    }
    
    /**
     * Handle a click on a registered button.
     * Sends a BUTTON_CLICKED packet to the companion plugin that registered it.
     */
    private void handleButtonClick(String buttonId) {
        extension.debug("Button clicked: " + buttonId);
        
        // Find which client registered this button
        ButtonManager buttonManager = extension.getMenuServer().getButtonManager();
        String clientId = buttonManager.getClientForButton(buttonId);
        
        if (clientId == null) {
            extension.logger().warning("No client found for button: " + buttonId);
            return;
        }
        
        // Get the client handler
        ClientHandler client = extension.getMenuServer().getClient(clientId);
        if (client == null) {
            extension.logger().warning("Client not connected: " + clientId);
            return;
        }
        
        // Send button click event to the companion plugin
        client.sendButtonClick(
            buttonId,
            connection.javaUuid(),
            connection.bedrockUsername(),
            connection.xuid()
        );
    }

    @Override
    public void send(GeyserConnection connection) {
        this.connection = connection;
        super.send(connection);
    }

    @Override
    protected void onClose(GeyserConnection connection) {
        // Menu closed, nothing special to do
    }
}
