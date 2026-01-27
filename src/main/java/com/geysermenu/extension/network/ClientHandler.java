package com.geysermenu.extension.network;

import com.geysermenu.extension.GeyserMenuExtension;
import com.geysermenu.extension.forms.FormBuilder;
import com.geysermenu.extension.network.protocol.*;
import com.geysermenu.extension.player.MenuPlayer;
import com.geysermenu.extension.protocol.FormRequest;
import com.geysermenu.extension.protocol.FormResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.geysermc.cumulus.form.Form;
import org.geysermc.geyser.api.GeyserApi;
import org.geysermc.geyser.api.connection.GeyserConnection;
import org.geysermc.geyser.session.GeyserSession;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles individual client connections from companion plugins
 */
public class ClientHandler extends SimpleChannelInboundHandler<String> {

    private static final Gson GSON = new GsonBuilder().create();

    private final GeyserMenuExtension extension;
    private final MenuServer server;

    private Channel channel;
    private String clientIdentifier;
    private boolean authenticated = false;

    public ClientHandler(GeyserMenuExtension extension, MenuServer server) {
        this.extension = extension;
        this.server = server;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.channel = ctx.channel();
        extension.logger().debug("New connection from: " + channel.remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (clientIdentifier != null) {
            server.unregisterClient(clientIdentifier);
        }
        extension.logger().debug("Connection closed: " + channel.remoteAddress());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String message) {
        try {
            Packet packet = GSON.fromJson(message, Packet.class);
            handlePacket(packet);
        } catch (Exception e) {
            extension.logger().error("Error processing message: " + e.getMessage());
            sendError("Invalid packet format");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        extension.logger().error("Connection error", cause);
        ctx.close();
    }

    private void handlePacket(Packet packet) {
        switch (packet.getType()) {
            case AUTH_REQUEST -> handleAuthRequest(packet);
            case SEND_MENU -> handleSendMenu(packet);
            case FORM_REQUEST -> handleFormRequest(packet);
            case REGISTER_BUTTONS -> handleRegisterButtons(packet);
            case PLAYER_LIST -> handlePlayerListRequest();
            case PING -> sendPacket(new Packet(Packet.PacketType.PONG, ""));
            default -> sendError("Unknown packet type: " + packet.getType());
        }
    }

    private void handleAuthRequest(Packet packet) {
        AuthData authData = GSON.fromJson(packet.getPayload(), AuthData.class);

        if (!extension.config().isRequireAuthentication()) {
            // Authentication disabled
            authenticated = true;
            clientIdentifier = authData.getServerIdentifier();
            server.registerClient(clientIdentifier, this);
            sendAuthResponse(true, "Authentication disabled - connected");
            return;
        }

        // Validate secret key
        if (authData.getSecretKey() != null &&
                authData.getSecretKey().equals(extension.config().getSecretKey())) {
            authenticated = true;
            clientIdentifier = authData.getServerIdentifier();
            server.registerClient(clientIdentifier, this);
            sendAuthResponse(true, "Authentication successful");
            extension.logger().info("Client authenticated: " + clientIdentifier);
        } else {
            sendAuthResponse(false, "Invalid secret key");
            extension.logger().warning("Authentication failed from: " + channel.remoteAddress());
            channel.close();
        }
    }

    /**
     * Handle button registration from companion plugins.
     * Buttons are stored and displayed in the main menu.
     */
    private void handleRegisterButtons(Packet packet) {
        if (!authenticated) {
            sendError("Not authenticated");
            return;
        }

        try {
            ButtonData.ButtonList buttonList = GSON.fromJson(packet.getPayload(), ButtonData.ButtonList.class);
            
            if (buttonList == null || buttonList.getButtons() == null) {
                sendError("Invalid button list");
                return;
            }

            server.getButtonManager().registerButtons(clientIdentifier, buttonList.getButtons());
            extension.logger().info("Registered " + buttonList.getButtons().size() + " buttons from client: " + clientIdentifier);
            
        } catch (Exception e) {
            extension.logger().error("Error registering buttons", e);
            sendError("Failed to register buttons: " + e.getMessage());
        }
    }

    /**
     * Send a button click event to the companion plugin.
     */
    public void sendButtonClick(String buttonId, UUID playerUuid, String playerName, String xuid) {
        ButtonData.ButtonClick click = new ButtonData.ButtonClick(buttonId, playerUuid.toString(), playerName, xuid);
        Packet packet = new Packet(Packet.PacketType.BUTTON_CLICKED, GSON.toJson(click));
        sendPacket(packet);
    }

    private void handleSendMenu(Packet packet) {
        if (!authenticated) {
            sendError("Not authenticated");
            return;
        }

        try {
            MenuData menuData = GSON.fromJson(packet.getPayload(), MenuData.class);

            // Find the target player
            GeyserConnection connection = GeyserApi.api().connectionByUuid(menuData.getTargetPlayer());

            if (connection == null) {
                sendError("Player not found: " + menuData.getTargetPlayer());
                return;
            }

            // Send the menu
            server.getDynamicMenuHandler().sendMenu(connection, menuData, this);

        } catch (Exception e) {
            extension.logger().error("Error sending menu", e);
            sendError("Failed to send menu: " + e.getMessage());
        }
    }

    /**
     * Handle FormsAPI-style form requests.
     * This provides compatibility with the Geyser-FormsAPI protocol.
     */
    private void handleFormRequest(Packet packet) {
        if (!authenticated) {
            sendError("Not authenticated");
            return;
        }

        try {
            FormRequest request = GSON.fromJson(packet.getPayload(), FormRequest.class);

            if (request.getPlayerUuid() == null) {
                sendError("Form request missing player UUID");
                return;
            }

            UUID playerUuid;
            try {
                playerUuid = UUID.fromString(request.getPlayerUuid());
            } catch (IllegalArgumentException e) {
                sendError("Invalid player UUID: " + request.getPlayerUuid());
                return;
            }

            // Find the player's session
            GeyserConnection connection = GeyserApi.api().connectionByUuid(playerUuid);
            if (connection == null) {
                sendError("Player session not found: " + request.getPlayerUuid());
                return;
            }

            GeyserSession session = (GeyserSession) connection;

            // Build the form with response handling
            Form form = FormBuilder.buildForm(request, response -> {
                extension.debug("Form response: " + GSON.toJson(response));

                // Send the response back to the companion plugin
                sendFormResponse(response);

                // Execute command based on response (for modal forms)
                if (response.isSuccess() && "modal".equalsIgnoreCase(request.getFormType())) {
                    if (response.isConfirmed() && request.getCommandAccept() != null) {
                        session.sendCommand(request.getCommandAccept());
                        extension.debug("Executed accept command: " + request.getCommandAccept());
                    } else if (!response.isConfirmed() && request.getCommandDeny() != null) {
                        session.sendCommand(request.getCommandDeny());
                        extension.debug("Executed deny command: " + request.getCommandDeny());
                    }
                }
            }, session);

            // Send the form
            session.sendForm(form);
            extension.debug("Sent form to player: " + session.javaUuid());

        } catch (Exception e) {
            extension.logger().error("Error sending form", e);
            sendError("Failed to send form: " + e.getMessage());
        }
    }

    /**
     * Sends a form response back to the companion plugin.
     */
    public void sendFormResponse(FormResponse response) {
        Packet packet = new Packet(
                Packet.PacketType.FORM_RESPONSE,
                GSON.toJson(response)
        );
        sendPacket(packet);
    }

    private void handlePlayerListRequest() {
        if (!authenticated) {
            sendError("Not authenticated");
            return;
        }

        // Get all online Bedrock players
        List<Map<String, String>> players = new ArrayList<>();

        for (GeyserConnection connection : GeyserApi.api().onlineConnections()) {
            Map<String, String> playerData = new HashMap<>();
            playerData.put("uuid", connection.javaUuid().toString());
            playerData.put("xuid", connection.xuid());
            playerData.put("name", connection.bedrockUsername());
            players.add(playerData);
        }

        Packet response = new Packet(
                Packet.PacketType.PLAYER_LIST_RESPONSE,
                GSON.toJson(players)
        );
        sendPacket(response);
    }

    public void sendResponse(MenuResponse response) {
        Packet packet = new Packet(
                Packet.PacketType.MENU_RESPONSE,
                GSON.toJson(response)
        );
        sendPacket(packet);
    }

    public void sendPlayerJoin(UUID playerUuid, String playerName, String xuid) {
        if (!authenticated) return;

        Map<String, String> data = new HashMap<>();
        data.put("uuid", playerUuid.toString());
        data.put("name", playerName);
        data.put("xuid", xuid);

        Packet packet = new Packet(Packet.PacketType.PLAYER_JOIN, GSON.toJson(data));
        sendPacket(packet);
    }

    public void sendPlayerLeave(UUID playerUuid) {
        if (!authenticated) return;

        Map<String, String> data = new HashMap<>();
        data.put("uuid", playerUuid.toString());

        Packet packet = new Packet(Packet.PacketType.PLAYER_LEAVE, GSON.toJson(data));
        sendPacket(packet);
    }

    private void sendAuthResponse(boolean success, String message) {
        AuthData response = success ? AuthData.success(message) : AuthData.failure(message);
        Packet packet = new Packet(Packet.PacketType.AUTH_RESPONSE, GSON.toJson(response));
        sendPacket(packet);
    }

    private void sendError(String message) {
        Packet packet = new Packet(Packet.PacketType.ERROR, message);
        sendPacket(packet);
    }

    private void sendPacket(Packet packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(GSON.toJson(packet));
        }
    }

    public void disconnect(String reason) {
        sendError(reason);
        if (channel != null) {
            channel.close();
        }
    }

    public String getClientIdentifier() {
        return clientIdentifier;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}
