package com.geysermenu.extension.forms;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.network.ButtonManager;
import com.geysermenu.extension.network.ClientHandler;
import com.geysermenu.extension.network.protocol.ButtonData;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;

import java.util.List;

/**
 * The main menu shown when players double-click their inventory
 */
public class MainMenu extends BaseMenu {

    private static final String ICON_SPAWN = "textures/items/bed_red";
    private static final String ICON_BACK = "textures/ui/back_button_default";
    private static final String ICON_ENDERCHEST = "textures/blocks/ender_chest_front";
    private static final String ICON_RECONNECT = "textures/ui/refresh_light";
    private static final String ICON_SWAP_OFFHAND = "textures/ui/move";

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
        
        // Add utility buttons (these require EssentialsX on the server)
        addButton("§6Swap Offhand", ICON_SWAP_OFFHAND, () -> {
            swapOffhand();
        });
        
        addButton("§aSpawn", ICON_SPAWN, () -> {
            executeCommand("spawn");
        });
        
        addButton("§eBack", ICON_BACK, () -> {
            executeCommand("back");
        });
        
        addButton("§5Ender Chest", ICON_ENDERCHEST, () -> {
            executeCommand("ec");
        });
        
        // Only add GeyserExtras reconnect button if GeyserExtras is installed
        if (extension.isGeyserExtrasInstalled()) {
            addButton("§bReconnect", ICON_RECONNECT, () -> {
                reconnectPlayer();
            });
        }
    }
    
    /**
     * Execute a command as the player.
     */
    private void executeCommand(String command) {
        if (connection instanceof GeyserSession session) {
            extension.debug("Executing command for " + connection.bedrockUsername() + ": /" + command);
            session.sendCommand(command);
        }
    }
    
    /**
     * Reconnect the player to the server (transfer to same address).
     * This mimics GeyserExtras reconnect functionality.
     */
    private void reconnectPlayer() {
        if (connection instanceof GeyserSession session) {
            extension.debug("Reconnecting player: " + connection.bedrockUsername());
            try {
                String address = session.joinAddress();
                int port = session.joinPort();
                session.transfer(address, port);
            } catch (Exception e) {
                extension.logger().warning("Failed to reconnect player: " + e.getMessage());
            }
        }
    }
    
    /**
     * Swap the item in the player's main hand with the offhand.
     * This mimics GeyserExtras swap offhand functionality.
     */
    private void swapOffhand() {
        if (connection instanceof GeyserSession session) {
            extension.debug("Swapping offhand for player: " + connection.bedrockUsername());
            session.requestOffhandSwap();
        }
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
