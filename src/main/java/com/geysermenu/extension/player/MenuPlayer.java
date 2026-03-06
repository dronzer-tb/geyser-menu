package com.geysermenu.extension.player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class MenuPlayer {

    private final UUID uuid;
    private final String xuid;
    private long lastInventoryClickTime = 0;
    private ScheduledFuture<?> doubleClickFuture;
    private int doubleClickThresholdMs = 200;

    // Pending form responses
    private final Map<String, FormCallback> pendingCallbacks = new ConcurrentHashMap<>();

    public MenuPlayer(UUID uuid, String xuid) {
        this.uuid = uuid;
        this.xuid = xuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getXuid() {
        return xuid;
    }

    public long getLastInventoryClickTime() {
        return lastInventoryClickTime;
    }

    public void setLastInventoryClickTime(long time) {
        this.lastInventoryClickTime = time;
    }

    public ScheduledFuture<?> getDoubleClickFuture() {
        return doubleClickFuture;
    }

    public void setDoubleClickFuture(ScheduledFuture<?> future) {
        this.doubleClickFuture = future;
    }

    public int getDoubleClickThresholdMs() {
        return doubleClickThresholdMs;
    }

    public void setDoubleClickThresholdMs(int ms) {
        this.doubleClickThresholdMs = ms;
    }

    public void addPendingCallback(String formId, FormCallback callback) {
        pendingCallbacks.put(formId, callback);
    }

    public FormCallback getPendingCallback(String formId) {
        return pendingCallbacks.remove(formId);
    }

    public void clearPendingCallbacks() {
        pendingCallbacks.clear();
    }

    @FunctionalInterface
    public interface FormCallback {
        void onResponse(FormResponse response);
    }

    public record FormResponse(String formId, int buttonId, String buttonText, boolean closed) {}
}
