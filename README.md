# AnimaKits

> A powerful, feature-rich kit management plugin for Paper servers — with gradient support, multi-page GUIs, and Bedrock compatibility.

---

## Compatibility

| Build | Minecraft Version | Java | Gradle |
|---|---|---|---|
| **26.1.2** | 26.1.0 · 26.1.1 · **26.1.2** | JDK 26+ | 9.4.0+ |
| **1.21.x** | 1.21.0 → **1.21.4** | JDK 21+ | 8.5+ / 9.x |

> **Note:** Both builds target [PaperMC](https://papermc.io/downloads/paper). Compatible forks (Purpur, Folia, etc.) are also supported.

---

## Features

- **Gradient Support** — Full MiniMessage and Birdflop `&#RRGGBB` gradient support in kit names and lore lines.
- **Multi-page GUIs** — Browse large kit libraries and edit kit contents across paginated menus.
- **Bedrock Support** — Native form menus for Bedrock players via [GeyserMC](https://geysermc.org/) and [Floodgate](https://github.com/GeyserMC/Floodgate).
- **Timed Permissions** — Grant temporary, time-limited access to any AnimaKits permission node.
- **Drag-and-Drop Editing** — Place or remove items directly inside the kit editor GUI; changes save on close.
- **Live GUI Refresh** — All open kit browsers update in real-time when kits are created, renamed, or deleted.
- **Clone & Rename** — Duplicate any kit with a single command, preserving items and lore.

---

## Installation

1. Download the correct JAR for your server version from the [Releases](../../releases) page.
2. Drop it into your server's `plugins/` folder.
3. Restart or reload your server.
4. Edit `plugins/AnimaKits/config.yml` to customise messages, GUI titles, and colours.

### Optional Dependencies

| Plugin | Purpose |
|---|---|
| [Geyser](https://geysermc.org/) + [Floodgate](https://github.com/GeyserMC/Floodgate) | Bedrock player support |
| [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) | Placeholder support |

---

## Commands

| Command | Description | Permission |
|---|---|---|
| `/anima kits` | Open the main kit browser GUI. | `anima.kits.use` |
| `/anima kits display <kit>` | View a kit's contents (read-only). | `anima.kits.display` |
| `/anima kits edit add <name>` | Create a new kit. | `anima.kits.edit.add` |
| `/anima kits edit remove <kit>` | Delete a kit. | `anima.kits.edit.remove` |
| `/anima kits edit rename <kit> <new>` | Rename a kit. | `anima.kits.edit.rename` |
| `/anima kits lore add <kit> <text>` | Add a lore line to a kit. | `anima.kits.lore.add` |
| `/anima kits lore edit <kit> <#> <text>` | Edit a specific lore line. | `anima.kits.lore.edit` |
| `/anima kits lore remove <kit> <#>` | Remove a lore line. | `anima.kits.lore.remove` |
| `/anima kits open <kit>` | Open the kit editor directly. | `anima.kits.open` |
| `/anima kits clonekit <kit> <new>` | Clone an existing kit. | `anima.kits.clonekit` |
| `/anima kits give <player> <kit> <amount>` | Give a kit to a specific player. | `anima.kits.give` |
| `/anima kits giveall <kit> <amount>` | Give a kit to all online players. | `anima.kits.giveall` |
| `/anima kits reload` | Reload the plugin configuration. | `anima.kits.reload` |
| `/anima kits help` | Display the help menu. | `anima.kits.help` |
| `/anima kits permission add <perm> <player> <time>` | Grant a timed permission. | `anima.kits.permission.add` |
| `/anima kits permission remove <perm> <player>` | Revoke a permission. | `anima.kits.permission.remove` |
| `/anima kits permission show <player>` | Show all permissions for a player. | `anima.kits.permission.show` |

> **Aliases:** `/anima`, `/ak`, `/animakits`

---

## Permissions

| Node | Description | Default |
|---|---|---|
| `anima.kits.*` | Grants access to **all** AnimaKits features. | `op` |
| `anima.kits.use` | Open the main kit browser GUI. | `op` |
| `anima.kits.display` | View kit contents (read-only). | `op` |
| `anima.kits.edit.add` | Create new kits. | `op` |
| `anima.kits.edit.remove` | Delete kits. | `op` |
| `anima.kits.edit.rename` | Rename kits. | `op` |
| `anima.kits.lore.add` | Add lore lines to kits. | `op` |
| `anima.kits.lore.edit` | Edit existing lore lines. | `op` |
| `anima.kits.lore.remove` | Remove lore lines. | `op` |
| `anima.kits.open` | Open the kit editor GUI directly. | `op` |
| `anima.kits.clonekit` | Clone kits. | `op` |
| `anima.kits.give` | Give a kit to a specific player. | `op` |
| `anima.kits.giveall` | Give a kit to all online players. | `op` |
| `anima.kits.reload` | Reload the plugin. | `op` |
| `anima.kits.help` | View the help menu. | `op` |
| `anima.kits.permission.add` | Grant timed permissions to players. | `op` |
| `anima.kits.permission.remove` | Revoke permissions from players. | `op` |
| `anima.kits.permission.show` | View a player's permission list. | `op` |

---

## Colour & Gradient Support

Kit names and lore lines support all three colour formats simultaneously:

| Format | Example | Result |
|---|---|---|
| Legacy codes | `&6&lGolden Kit` | Bold gold text |
| Hex (Birdflop) | `&#FF5500Lava Kit` | Orange hex colour |
| MiniMessage | `<gradient:#54DAF4:#545EB6>My Kit</gradient>` | Blue-to-purple gradient |

---

## Technical Notes

**Click Mapping in the Kit Browser**

Minecraft does not forward `Ctrl+Click` to the server for inventory slots outside creative mode. AnimaKits uses the following alternative mappings inside the kit browser:

| Click | Action |
|---|---|
| Left-click | View kit (read-only display) |
| Right-click | Open kit editor |
| Shift + Right-click | Delete kit |
| Shift + Left-click | Clone kit |

**Bedrock Players**

Bedrock players connected via Geyser and Floodgate receive native Bedrock form menus (powered by the Cumulus API) instead of the Java inventory GUI, providing a seamless cross-platform experience.

**Data Storage**

Kits are stored in `plugins/AnimaKits/kits.yml`. Timed permissions are stored separately in `plugins/AnimaKits/permissions.yml`. Both files are reloaded via `/anima kits reload` without a server restart.

---

## Building from Source

Two GitHub Actions workflows are included:

```bash
# Trigger a release build (creates a GitHub Release automatically)
git tag v1.0.0
git push origin v1.0.0
```

| Workflow | Target Paper | Java Required | Gradle Required |
|---|---|---|---|
| `build26_1_2.yml` | 26.1.2 | JDK 26 | 9.4.0+ |
| `build1_21_x.yml` | 1.21.4 | JDK 21 | 8.5+ |

---

## Credits

Created by **World Of Normies**.  
Website: [github.com/worldofnormies/AnimaKits](https://github.com/worldofnormies/AnimaKits)
