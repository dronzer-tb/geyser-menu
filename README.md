# GeyserMenu Extension

[![Modrinth](https://img.shields.io/modrinth/v/geysermenu?logo=modrinth&label=Modrinth)](https://modrinth.com/plugin/geysermenu)
[![GitHub Release](https://img.shields.io/github/v/release/dronzer-tb/geyser-menu)](https://github.com/dronzer-tb/geyser-menu/releases)
[![License](https://img.shields.io/github/license/dronzer-tb/geyser-menu)](LICENSE)

A Geyser extension that provides a customizable menu system for Bedrock players, accessible by double-clicking the inventory button.

## Features

- **Inventory Double-Click Detection**: Opens menu when Bedrock players quickly double-click their inventory
- **Button Registration API**: Companion plugins can register custom buttons via TCP
- **TCP Communication**: Secure Netty-based TCP protocol with authentication
- **GeyserExtras Integration**: Automatically detects and works with GeyserExtras addon buttons
- **Per-Player Button Filtering**: Buttons can be conditionally shown based on permissions

## Requirements

- Geyser 2.9.0+
- Java 21+
- [GeyserMenu Companion](https://github.com/dronzer-tb/geyser-menu-companion) plugin on your server (Spigot/Paper/Velocity)

## Installation

1. Download `GeyserMenu.jar` from [Modrinth](https://modrinth.com/plugin/geysermenu) or [GitHub Releases](https://github.com/dronzer-tb/geyser-menu/releases)
2. Place in your Geyser `extensions/` folder
3. Start Geyser to generate `config.yml`
4. Configure the settings as needed
5. Install the companion plugin on your server

## Configuration

```yaml
# config.yml
default-menu-title: "Server Menu"
require-authentication: true
secret-key: "your-secret-key-here"
server-port: 19133
```

| Option | Description | Default |
|--------|-------------|---------|
| `default-menu-title` | Title shown at the top of the menu | `"Server Menu"` |
| `require-authentication` | Whether companion plugins must authenticate | `true` |
| `secret-key` | Shared secret for authentication | Random generated |
| `server-port` | TCP port for companion communication | `19133` |

## How It Works

1. **Player triggers menu**: Bedrock player double-clicks their inventory button
2. **Extension intercepts**: GeyserMenu detects the double-click pattern
3. **Menu displayed**: A form menu is shown with all registered buttons
4. **Button clicked**: Click event is sent to the companion plugin
5. **Action executed**: Companion plugin handles the action (command, form, etc.)

## Architecture

```
┌─────────────────┐     TCP/19133      ┌─────────────────────┐
│  GeyserMenu     │◄──────────────────►│  Companion Plugin   │
│  (Extension)    │   Authentication   │  (Spigot/Velocity)  │
│                 │   Button Sync      │                     │
│  - Menu Forms   │   Click Events     │  - Button Registry  │
│  - Packet Hook  │   Player Events    │  - API for plugins  │
└─────────────────┘                    └─────────────────────┘
```

## For Developers

To register buttons in the menu, you need to use the companion plugin's API. See the [GeyserMenu Companion](https://github.com/dronzer-tb/geyser-menu-companion) documentation for full API details.

### TCP Protocol Overview

The extension communicates with companion plugins using a binary protocol:

| Packet Type | Direction | Description |
|-------------|-----------|-------------|
| `AUTH` | Client→Server | Authentication handshake |
| `REGISTER_BUTTONS` | Client→Server | Register/update buttons |
| `BUTTON_CLICKED` | Server→Client | Notify button click |
| `PLAYER_JOIN` | Server→Client | Bedrock player joined |
| `PLAYER_LEAVE` | Server→Client | Bedrock player left |
| `SEND_FORM` | Client→Server | Send form to player |
| `FORM_RESPONSE` | Server→Client | Form response from player |

## Building

```bash
./gradlew shadowJar
```

Output: `build/libs/GeyserMenu.jar`

## License

MIT License
