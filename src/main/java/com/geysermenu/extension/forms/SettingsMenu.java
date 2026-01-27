package com.geysermenu.extension.forms;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.player.MenuPlayer;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.geyser.api.connection.GeyserConnection;

/**
 * Settings menu for player preferences
 */
public class SettingsMenu {

    private final GeyserMenuExtension extension;

    public SettingsMenu(GeyserMenuExtension extension) {
        this.extension = extension;
    }

    public void send(GeyserConnection connection) {
        MenuPlayer player = extension.getPlayerManager().getPlayer(connection);
        if (player == null) return;

        int currentThreshold = player.getDoubleClickThresholdMs();

        CustomForm.Builder builder = CustomForm.builder()
                .title("GeyserMenu Settings")
                .slider("Double-Click Threshold (ms)", 100, 500, 50, currentThreshold)
                .toggle("Enable Double-Click Menu", extension.config().isEnableDoubleClickMenu());

        builder.validResultHandler(response -> {
            // Get slider value (index 0)
            int newThreshold = (int) response.asSlider(0);
            player.setDoubleClickThresholdMs(newThreshold);

            extension.logger().debug("Player " + player.getUuid() + " set threshold to " + newThreshold + "ms");
        });

        builder.closedOrInvalidResultHandler(response -> {
            // Settings closed without saving, that's fine
        });

        connection.sendForm(builder.build());
    }
}
