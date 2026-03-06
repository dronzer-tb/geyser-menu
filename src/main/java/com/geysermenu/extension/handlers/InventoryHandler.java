package com.geysermenu.extension.handlers;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.forms.MainMenu;
import com.geysermenu.extension.player.MenuPlayer;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.bedrock.SessionDisconnectEvent;
import org.geysermc.geyser.api.event.java.ServerDefineCommandsEvent;

public class InventoryHandler {

    private final GeyserMenuExtension extension;

    public InventoryHandler(GeyserMenuExtension extension) {
        this.extension = extension;
        extension.eventBus().register(this);
        debug("InventoryHandler initialized and registered");
    }

    @Subscribe
    public void onServerDefineCommands(ServerDefineCommandsEvent event) {
        // This event fires after the player is fully connected and has a Java UUID
        GeyserConnection connection = event.connection();
        if (connection.javaUuid() != null) {
            extension.getPlayerManager().getOrCreatePlayer(connection);
            debug("Player registered: " + connection.bedrockUsername() + " (UUID: " + connection.javaUuid() + ")");
        } else {
            debug("Player connection event received but javaUuid is null for: " + connection.bedrockUsername());
        }
    }

    @Subscribe
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        GeyserConnection connection = event.connection();
        if (connection.javaUuid() != null) {
            extension.getPlayerManager().removePlayer(connection.javaUuid());
            // Clean up double-click tracking for this player
            BedrockInteractInjector.cleanupPlayer(connection.javaUuid());
            debug("Player left: " + connection.bedrockUsername() + " (UUID: " + connection.javaUuid() + ")");
        }
    }

    /**
     * Called when the player attempts to open their inventory.
     * This method should be hooked into the BedrockInteractPacket handler.
     * 
     * NOTE: Currently, Geyser's public API does not expose an event for intercepting
     * the OPEN_INVENTORY packet. This method is prepared for future API additions
     * or internal hooking.
     *
     * @param connection The player's connection
     * @return true if the menu was opened (double-click detected), false otherwise
     */
    public boolean handleInventoryOpen(GeyserConnection connection) {
        debug("handleInventoryOpen called for: " + connection.bedrockUsername());
        
        if (!extension.config().isEnableDoubleClickMenu()) {
            debug("Double-click menu is disabled in config");
            return false;
        }

        MenuPlayer player = extension.getPlayerManager().getOrCreatePlayer(connection);
        long currentTime = System.currentTimeMillis();
        int threshold = extension.config().getDoubleClickThresholdMs();
        long lastClick = player.getLastInventoryClickTime();

        debug("Current time: " + currentTime + ", Last click: " + lastClick + ", Threshold: " + threshold);

        // Check if this is a double-click
        if (currentTime - lastClick < threshold) {
            debug("Double-click detected! Opening menu...");
            
            // Cancel any existing future
            if (player.getDoubleClickFuture() != null) {
                player.getDoubleClickFuture().cancel(false);
            }

            // Open the menu
            openMainMenu(connection);
            player.setLastInventoryClickTime(0);
            return true;
        } else {
            debug("First click recorded, waiting for second click...");
            // First click - record time
            player.setLastInventoryClickTime(currentTime);

            // Cancel any existing future
            if (player.getDoubleClickFuture() != null) {
                player.getDoubleClickFuture().cancel(false);
            }

            return false;
        }
    }

    /**
     * Opens the main menu for the player
     */
    public void openMainMenu(GeyserConnection connection) {
        debug("Opening main menu for: " + connection.bedrockUsername());
        MainMenu menu = new MainMenu(extension);
        menu.send(connection);
        debug("Main menu sent to: " + connection.bedrockUsername());
    }

    private void debug(String message) {
        extension.debug(message);
    }
}
