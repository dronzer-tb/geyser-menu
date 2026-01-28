package com.geysermenu.extension.handlers;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.config.GeyserMenuConfig;
import org.cloudburstmc.protocol.bedrock.packet.InteractPacket;
import org.geysermc.geyser.session.GeyserSession;
import org.geysermc.geyser.translator.protocol.PacketTranslator;
import org.geysermc.geyser.translator.protocol.bedrock.entity.player.BedrockInteractTranslator;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Intercepts Bedrock InteractPacket to detect double-click inventory actions.
 * When a player double-clicks the inventory button quickly, it opens the GeyserMenu.
 * Extends the original BedrockInteractTranslator to maintain normal functionality.
 */
public class BedrockInteractInjector extends PacketTranslator<InteractPacket> {

    private static final Map<UUID, Long> lastInventoryClickTime = new ConcurrentHashMap<>();
    private static final Map<UUID, ScheduledFuture<?>> pendingInventoryFutures = new ConcurrentHashMap<>();
    
    // Reference to the original translator
    private final BedrockInteractTranslator originalTranslator = new BedrockInteractTranslator();

    @Override
    public void translate(GeyserSession session, InteractPacket packet) {
        GeyserMenuExtension extension = GeyserMenuExtension.getInstance();
        GeyserMenuConfig config = extension != null ? extension.config() : null;
        
        if (packet.getAction() == InteractPacket.Action.OPEN_INVENTORY) {
            if (config != null && config.isEnableDoubleClickMenu()) {
                UUID playerUuid = session.getPlayerEntity().getUuid();
                long currentTime = System.currentTimeMillis();
                Long lastClick = lastInventoryClickTime.get(playerUuid);
                
                int thresholdMs = config.getDoubleClickThresholdMs();
                
                if (config.isDebugMode()) {
                    extension.debug("Inventory click from " + session.bedrockUsername() + 
                        ", last click: " + (lastClick != null ? (currentTime - lastClick) + "ms ago" : "never"));
                }
                
                // Check if this is a double-click (second click within threshold)
                if (lastClick != null && (currentTime - lastClick) <= thresholdMs) {
                    // Cancel any pending inventory open (may or may not exist)
                    ScheduledFuture<?> pendingFuture = pendingInventoryFutures.remove(playerUuid);
                    if (pendingFuture != null && !pendingFuture.isCancelled() && !pendingFuture.isDone()) {
                        pendingFuture.cancel(false);
                    }
                    
                    if (config.isDebugMode()) {
                        extension.debug("Double-click detected for " + session.bedrockUsername() + 
                            " (interval: " + (currentTime - lastClick) + "ms) - Opening GeyserMenu!");
                    }
                    
                    // Open the GeyserMenu - timing check is sufficient, pending future is optional
                    extension.openMenuForPlayer(session);
                    lastInventoryClickTime.remove(playerUuid);
                    return;
                }
                
                // First click - schedule delayed inventory open
                lastInventoryClickTime.put(playerUuid, currentTime);
                
                // Schedule the normal inventory open with a delay
                ScheduledFuture<?> future = session.scheduleInEventLoop(() -> {
                    // If we get here, it was a single click - open inventory normally
                    pendingInventoryFutures.remove(playerUuid);
                    originalTranslator.translate(session, packet);
                }, thresholdMs + 20, TimeUnit.MILLISECONDS);
                
                pendingInventoryFutures.put(playerUuid, future);
                return;
            }
        }
        
        // For non-inventory actions, or if double-click is disabled, pass through normally
        originalTranslator.translate(session, packet);
    }
    
    /**
     * Cleans up tracking data when a player disconnects
     */
    public static void cleanupPlayer(UUID uuid) {
        lastInventoryClickTime.remove(uuid);
        ScheduledFuture<?> future = pendingInventoryFutures.remove(uuid);
        if (future != null) {
            future.cancel(false);
        }
    }
}
