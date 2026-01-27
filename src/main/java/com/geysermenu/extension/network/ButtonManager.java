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
     * Get all registered buttons from all companion plugins, sorted by priority.
     */
    public List<ButtonData> getAllButtons() {
        List<ButtonData> allButtons = new ArrayList<>();
        for (List<ButtonData> buttons : clientButtons.values()) {
            allButtons.addAll(buttons);
        }
        // Sort by priority (lower = first)
        allButtons.sort(Comparator.comparingInt(ButtonData::getPriority));
        return allButtons;
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
