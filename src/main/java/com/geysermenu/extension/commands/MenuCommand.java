package com.geysermenu.extension.commands;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.forms.MainMenu;
import org.geysermc.geyser.api.command.Command;
import org.geysermc.geyser.api.command.CommandSource;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.api.util.TriState;

/**
 * Command to open the GeyserMenu for Bedrock players.
 * Usage: /geysermenu menu
 */
public class MenuCommand {

    private final GeyserMenuExtension extension;

    public MenuCommand(GeyserMenuExtension extension) {
        this.extension = extension;
    }

    /**
     * Builds the menu command for registration.
     */
    public Command build() {
        return Command.builder(extension)
                .source(GeyserConnection.class)
                .name("menu")
                .description("Opens the GeyserMenu")
                .permission("geysermenu.command.menu", TriState.TRUE)
                .playerOnly(true)
                .bedrockOnly(true)
                .executor(this::execute)
                .build();
    }

    /**
     * Executes the menu command.
     */
    private void execute(CommandSource source, org.geysermc.geyser.api.command.Command command, String[] args) {
        if (!(source instanceof GeyserConnection connection)) {
            return;
        }

        debug("Menu command executed by: " + connection.bedrockUsername());

        // Open the main menu
        MainMenu menu = new MainMenu(extension);
        menu.send(connection);

        debug("Menu sent to player: " + connection.bedrockUsername());
    }

    private void debug(String message) {
        if (extension.config().isDebugMode()) {
            extension.logger().info("[DEBUG] " + message);
        }
    }
}
