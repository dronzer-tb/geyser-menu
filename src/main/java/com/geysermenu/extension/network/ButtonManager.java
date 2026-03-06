package com.geysermenu.extension.network;

import com.geysermenu.extension.network.protocol.ButtonData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages buttons registered by companion plugins.
 * Buttons are stored per-client (companion plugin) and merged for display.
 */
public class ButtonManager {
    
    // Map of client identifier -> list of buttons from that client
    private final Map<String, List<ButtonData>> clientButtons = new ConcurrentHashMap<>();
    
    // Custom button ordering: button ID -> position (lower = first)
    private final Map<String, Integer> customOrder = new ConcurrentHashMap<>();
    
    /**
     * Register buttons from a companion plugin.
     * Replaces any previously registered buttons from the same client.
     */
    public void registerButtons(String clientIdentifier, List<ButtonData> buttons) {
        clientButtons.put(clientIdentifier, new ArrayList<>(buttons));
    }
    
    /**
     * Unregister all buttons from a companion plugin.
     */
    public void unregisterButtons(String clientIdentifier) {
        clientButtons.remove(clientIdentifier);
    }
    
    /**
     * Set custom position for a button.
     * @param buttonId The button ID (or partial match for button text)
     * @param position The desired position (1-based, lower = first)
     * @return true if button was found and reordered
     */
    public boolean setButtonPosition(String buttonId, int position) {
        // Find the button by ID or text match
        ButtonData button = findButtonByIdOrText(buttonId);
        if (button != null) {
            customOrder.put(button.getId(), position);
            return true;
        }
        return false;
    }
    
    /**
     * Find a button by ID or partial text match (case-insensitive).
     */
    public ButtonData findButtonByIdOrText(String search) {
        String searchLower = search.toLowerCase();
        for (List<ButtonData> buttons : clientButtons.values()) {
            for (ButtonData button : buttons) {
                // Exact ID match
                if (button.getId().equalsIgnoreCase(search)) {
                    return button;
                }
                // Text contains match (strip color codes for comparison)
                String plainText = button.getText().replaceAll("§[0-9a-fk-or]", "").toLowerCase();
                if (plainText.contains(searchLower)) {
                    return button;
                }
            }
        }
        return null;
    }
    
    /**
     * Get all registered buttons from all companion plugins, sorted by custom order then priority.
     */
    public List<ButtonData> getAllButtons() {
        List<ButtonData> allButtons = new ArrayList<>();
        for (List<ButtonData> buttons : clientButtons.values()) {
            allButtons.addAll(buttons);
        }
        // Sort by custom order first, then by priority
        allButtons.sort((a, b) -> {
            Integer orderA = customOrder.get(a.getId());
            Integer orderB = customOrder.get(b.getId());
            
            // If both have custom order, compare by order
            if (orderA != null && orderB != null) {
                return orderA.compareTo(orderB);
            }
            // Custom ordered buttons come first
            if (orderA != null) return -1;
            if (orderB != null) return 1;
            // Fall back to priority
            return Integer.compare(a.getPriority(), b.getPriority());
        });
        return allButtons;
    }
    
    /**
     * Get the current custom order map.
     */
    public Map<String, Integer> getCustomOrder() {
        return new ConcurrentHashMap<>(customOrder);
    }
    
    /**
     * Clear all custom ordering.
     */
    public void clearCustomOrder() {
        customOrder.clear();
    }
    
    /**
     * Get a specific button by ID.
     */
    public ButtonData getButton(String buttonId) {
        for (List<ButtonData> buttons : clientButtons.values()) {
            for (ButtonData button : buttons) {
                if (button.getId().equals(buttonId)) {
                    return button;
                }
            }
        }
        return null;
    }
    
    /**
     * Find which client registered a button.
     */
    public String getClientForButton(String buttonId) {
        for (Map.Entry<String, List<ButtonData>> entry : clientButtons.entrySet()) {
            for (ButtonData button : entry.getValue()) {
                if (button.getId().equals(buttonId)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }
    
    /**
     * Check if any buttons are registered.
     */
    public boolean hasButtons() {
        return !clientButtons.isEmpty() && clientButtons.values().stream().anyMatch(list -> !list.isEmpty());
    }
    
    /**
     * Get count of registered buttons.
     */
    public int getButtonCount() {
        return clientButtons.values().stream().mapToInt(List::size).sum();
    }
}
