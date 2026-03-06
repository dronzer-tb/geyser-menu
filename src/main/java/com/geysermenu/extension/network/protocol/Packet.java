package com.geysermenu.extension.network.protocol;

/**
 * Base packet class for communication protocol
 */
public class Packet {

    private PacketType type;
    private String payload;

    public Packet() {}

    public Packet(PacketType type, String payload) {
        this.type = type;
        this.payload = payload;
    }

    public enum PacketType {
        // Handshake
        AUTH_REQUEST,       // Client -> Server: Authentication request with secret key
        AUTH_RESPONSE,      // Server -> Client: Authentication result

        // Menu operations
        SEND_MENU,          // Client -> Server: Request to send menu to player
        MENU_RESPONSE,      // Server -> Client: Player response to menu

        // Button registration
        REGISTER_BUTTONS,   // Client -> Server: Register menu buttons from companion
        BUTTON_CLICKED,     // Server -> Client: A registered button was clicked
        REQUEST_BUTTONS,    // Server -> Client: Request companion to send registered buttons
        OPEN_MAIN_MENU,     // Client -> Server: Request to open main menu for a player
        REORDER_BUTTON,     // Client -> Server: Reorder a button to a specific position

        // FormsAPI compatibility (simple JSON form requests)
        FORM_REQUEST,       // Client -> Server: Simple form request (FormsAPI format)
        FORM_RESPONSE,      // Server -> Client: Simple form response (FormsAPI format)

        // Player state
        PLAYER_JOIN,        // Server -> Client: Bedrock player joined
        PLAYER_LEAVE,       // Server -> Client: Bedrock player left
        PLAYER_LIST,        // Client -> Server: Request list of online Bedrock players
        PLAYER_LIST_RESPONSE, // Server -> Client: List of online Bedrock players

        // Utility
        PING,               // Keepalive ping
        PONG,               // Keepalive pong
        ERROR               // Error message
    }

    public PacketType getType() {
        return type;
    }

    public void setType(PacketType type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
