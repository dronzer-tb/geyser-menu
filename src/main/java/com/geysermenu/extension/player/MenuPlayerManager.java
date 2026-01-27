package com.geysermenu.extension.player;

import org.geysermc.geyser.api.connection.GeyserConnection;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MenuPlayerManager {

    private final Map<UUID, MenuPlayer> players = new ConcurrentHashMap<>();

    public MenuPlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public MenuPlayer getPlayer(GeyserConnection connection) {
        return players.get(connection.javaUuid());
    }

    public MenuPlayer getOrCreatePlayer(GeyserConnection connection) {
        return players.computeIfAbsent(connection.javaUuid(),
                uuid -> new MenuPlayer(uuid, connection.xuid()));
    }

    public void removePlayer(UUID uuid) {
        MenuPlayer player = players.remove(uuid);
        if (player != null) {
            player.clearPendingCallbacks();
            if (player.getDoubleClickFuture() != null) {
                player.getDoubleClickFuture().cancel(false);
            }
        }
    }

    public Map<UUID, MenuPlayer> getAllPlayers() {
        return players;
    }

    public boolean hasPlayer(UUID uuid) {
        return players.containsKey(uuid);
    }
}
