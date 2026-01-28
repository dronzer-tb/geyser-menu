package com.geysermenu.extension;

import com.geysermenu.extension.commands.MenuCommand;
import com.geysermenu.extension.config.GeyserMenuConfig;
import com.geysermenu.extension.forms.MainMenu;
import com.geysermenu.extension.handlers.BedrockInteractInjector;
import com.geysermenu.extension.handlers.InventoryHandler;
import com.geysermenu.extension.network.MenuServer;
import com.geysermenu.extension.player.MenuPlayerManager;
import org.cloudburstmc.protocol.bedrock.packet.InteractPacket;
import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCommandsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPreInitializeEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserShutdownEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.registry.Registries;
import org.geysermc.geyser.session.GeyserSession;

public class GeyserMenuExtension implements Extension {

    private static GeyserMenuExtension instance;

    private GeyserMenuConfig config;
    private MenuServer menuServer;
    private MenuPlayerManager playerManager;
    private InventoryHandler inventoryHandler;
    private boolean geyserExtrasInstalled = false;

    @Subscribe
    public void onPreInitialize(GeyserPreInitializeEvent event) {
        instance = this;

        // Load configuration
        this.config = GeyserMenuConfig.load(this);
        this.playerManager = new MenuPlayerManager();

        logger().info("GeyserMenu configuration loaded");
    }

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        // Start TCP server for companion plugin connections
        this.menuServer = new MenuServer(this);
        this.menuServer.start();

        // Register inventory handler for player join/leave events
        this.inventoryHandler = new InventoryHandler(this);

        // Register the BedrockInteractInjector to intercept inventory open packets
        if (config.isEnableDoubleClickMenu()) {
            registerPacketInjectors();
            logger().info("Double-click inventory menu detection enabled");
        }
        
        // Check for GeyserExtras and notify admins
        checkForGeyserExtras();

        debug("TCP server started, inventory handler registered");
        logger().info("GeyserMenu v" + this.description().version() + " has been enabled!");
    }
    
    /**
     * Check if GeyserExtras extension is installed and notify admins if so.
     */
    private void checkForGeyserExtras() {
        try {
            // Check if GeyserExtras is loaded as an extension
            boolean found = this.geyserApi().extensionManager().extensions().stream()
                .anyMatch(ext -> ext.description().id().equalsIgnoreCase("geyserextras") 
                              || ext.description().name().equalsIgnoreCase("GeyserExtras"));
            
            if (found) {
                geyserExtrasInstalled = true;
                
                logger().warning("========================================");
                logger().warning("GeyserExtras detected!");
                logger().warning("Please disable GeyserExtras custom menu");
                logger().warning("in its config to avoid conflicts with");
                logger().warning("GeyserMenu's inventory double-click menu.");
                logger().warning("========================================");
            } else {
                geyserExtrasInstalled = false;
                debug("GeyserExtras not detected");
            }
        } catch (Exception e) {
            geyserExtrasInstalled = false;
            debug("GeyserExtras detection failed: " + e.getMessage());
        }
    }
    
    /**
     * Check if GeyserExtras is installed.
     * @return true if GeyserExtras is available
     */
    public boolean isGeyserExtrasInstalled() {
        return geyserExtrasInstalled;
    }

    /**
     * Registers packet injectors to intercept Bedrock packets
     */
    private void registerPacketInjectors() {
        BedrockInteractInjector injector = new BedrockInteractInjector();
        Registries.BEDROCK_PACKET_TRANSLATORS.register(InteractPacket.class, injector);
        logger().info("Registered BedrockInteractInjector for double-click detection");
        debug("InteractPacket translator registered: " + Registries.BEDROCK_PACKET_TRANSLATORS.get(InteractPacket.class).getClass().getName());
    }

    @Subscribe
    public void onDefineCommands(GeyserDefineCommandsEvent event) {
        // Register the menu command
        MenuCommand menuCommand = new MenuCommand(this);
        event.register(menuCommand.build());
        debug("Registered /geysermenu menu command");
        
        // Register the /gemu fallback command
        event.register(menuCommand.buildGemuCommand());
        debug("Registered /gemu fallback command");
    }

    @Subscribe
    public void onShutdown(GeyserShutdownEvent event) {
        if (menuServer != null) {
            menuServer.stop();
        }
        logger().info("GeyserMenu has been disabled!");
    }

    public static GeyserMenuExtension getInstance() {
        return instance;
    }

    public GeyserMenuConfig config() {
        return config;
    }

    public MenuServer getMenuServer() {
        return menuServer;
    }

    public MenuPlayerManager getPlayerManager() {
        return playerManager;
    }

    public InventoryHandler getInventoryHandler() {
        return inventoryHandler;
    }

    /**
     * Logs a debug message if debug mode is enabled.
     */
    public void debug(String message) {
        if (config != null && config.isDebugMode()) {
            logger().info("[DEBUG] " + message);
        }
    }

    /**
     * Opens the main menu for a player using their GeyserSession.
     * This is called by the BedrockInteractInjector when double-click is detected.
     */
    public void openMenuForPlayer(GeyserSession session) {
        GeyserConnection connection = session;
        debug("Opening menu for player: " + session.bedrockUsername());
        new MainMenu(this).send(connection);
    }

    /**
     * Opens the main menu for a player using their GeyserConnection.
     */
    public void openMenuForPlayer(GeyserConnection connection) {
        debug("Opening menu for player via connection");
        new MainMenu(this).send(connection);
    }
}
