# GeyserMenu Extension

A Geyser extension that provides a customizable menu system for Bedrock players, accessible by double-clicking the inventory button.

## Features

- **Main Menu**: Displays when Bedrock players double-click their inventory
- **Button Registration API**: Companion plugins can register custom buttons
- **TCP Communication**: Communicates with companion plugins via Netty-based TCP
- **Settings Menu**: Built-in settings configuration for players

## Requirements

- Geyser 2.9.0+
- Java 21+

## Installation

1. Download `GeyserMenu.jar` from releases
2. Place in your Geyser `extensions/` folder
3. Start Geyser
4. Configure `config.yml` as needed

## Configuration

```yaml
# config.yml
default-menu-title: "Server Menu"
require-authentication: true
secret-key: "your-secret-key-here"
server-port: 19135
```

## For Developers

Companion plugins can register buttons to appear in the main menu. See the [GeyserMenuCompanion](https://github.com/yourusername/geyser-menu-companion) project for the Spigot-side API.

### Button Registration Protocol

Buttons are registered via TCP packets:
- `REGISTER_BUTTONS`: Send button list to extension
- `BUTTON_CLICKED`: Receive click events from extension

## Building

```bash
./gradlew shadowJar
```

Output: `build/libs/GeyserMenu.jar`

## License

MIT License
