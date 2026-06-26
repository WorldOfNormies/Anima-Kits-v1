# AnimaKits

> A powerful kit management plugin for Paper/Spigot servers — gradient names, multi-page GUIs, timed permissions, and Bedrock support out of the box.

**Author:** World Of Normies  
**API Version:** 1.21  
**Aliases:** `/ak`, `/animakits`

---

## Compatibility

| Build | Minecraft | Java | Gradle |
|---|---|---|---|
| 26.1.2 | 26.1.0 · 26.1.1 · 26.1.2 | JDK 26+ | 9.4+ |
| 1.21.x | 1.21.0 → 1.21.4 | JDK 21+ | 8.5+ / 9.x |

Targets [PaperMC](https://papermc.io/downloads/paper). Compatible forks (Purpur, Folia, etc.) work too.

**Optional soft-dependencies:** Floodgate · Geyser-Spigot · PlaceholderAPI

---

## Features

| Feature | Details |
|---|---|
| **Gradient names & lore** | Full MiniMessage + `&#RRGGBB` hex support in kit names and lore |
| **Multi-page GUIs** | Paginated kit browser and editor — drag-and-drop items, saves on close |
| **Bedrock support** | Native Bedrock form menus via GeyserMC + Floodgate |
| **Timed permissions** | Grant/revoke any permission node with an optional expiry (`30s · 5m · 1h · 1d · -1`) |
| **Global `@a` grants** | Permissions assigned via selector automatically apply to future players on join |
| **Multi-word kit names** | Spaces, gradients, and special characters all work in kit IDs across every command |
| **Clone & rename** | Duplicate any kit in one command, preserving all items and lore |
| **Live GUI refresh** | All open browsers update in real-time when kits are created, renamed, or deleted |

---

## Commands

### Player Commands

| Command | Description | Permission | Default |
|---|---|---|---|
| `/anima kits` | Open the graphical kit claim browser | `anima.kits.use` | Everyone |
| `/anima kits claim` | Open the visual claim catalog | `anima.kits.claim` | Everyone |
| `/anima kits claim <kit>` | Claim a specific kit directly | `anima.kits.claim.<kit>` | Everyone |
| `/anima kits list` | List all available kits | `anima.kits.list` | Everyone |
| `/anima kits help` | Show the player help guide | `anima.kits.help` | Everyone |

### Admin Commands

| Command | Description | Permission |
|---|---|---|
| `/anima kits` | Open the interactive kit creator GUI | `anima.kits.add` |
| `/anima kits add <name>` | Create a new empty kit | `anima.kits.add` |
| `/anima kits delete <kit>` | Permanently delete a kit | `anima.kits.delete` |
| `/anima kits rename <kit> <new name>` | Rename a kit (supports multi-word names) | `anima.kits.rename` |
| `/anima kits clonekit <kit> <new name>` | Duplicate a kit to a new name | `anima.kits.clonekit` |
| `/anima kits lore add <kit> <text>` | Append a lore line to a kit | `anima.kits.lore.add` |
| `/anima kits lore edit <kit> <#> <text>` | Edit an existing lore line | `anima.kits.lore.edit` |
| `/anima kits lore remove <kit> <#>` | Remove a lore line by index | `anima.kits.lore.remove` |
| `/anima kits give <player\|selector> <kit> <n>` | Give a kit to a player (supports selectors) | `anima.kits.give` |
| `/anima kits giveall <kit> <n>` | Give a kit to all online players | `anima.kits.giveall` |
| `/anima kits onjoinnew <kit>` | Set the starter kit given to new players on first join | `anima.kits.onjoinnew` |
| `/anima kits onjoinnew clear` | Disable the first-join starter kit | `anima.kits.onjoinnew` |
| `/anima kits onjoinnew` | Check what kit is currently configured | `anima.kits.onjoinnew` |
| `/anima kits setcooldown <kit> <seconds>` | Set a cooldown on a kit | `anima.kits.setcooldown` |
| `/anima kits singleclaim <kit> <true\|false>` | Toggle one-time-only claim limit | `anima.kits.singleclaim` |
| `/anima kits reload` | Reload all plugin config files | `anima.kits.reload` |

**Master admin wildcard:** `anima.kits.*` (default: OP)

### Permission Commands

| Command | Description | Permission |
|---|---|---|
| `/anima kits permission add <node> <player\|@a> <time\|-1>` | Grant any permission node | `anima.kits.permission.add` |
| `/anima kits permission remove <node> <player\|@a>` | Revoke a permission node | `anima.kits.permission.remove` |
| `/anima kits permission show <player>` | View all active permissions for a player | `anima.kits.permission.show` |
| `/anima kits permission claim <player\|@a> <kit> <true\|false> [time]` | Grant/revoke kit claim access | `anima.kits.permission.claim` |
| `/anima kits permission claimfree <player\|@a> <kit> <true\|false> [time]` | Grant/revoke cooldown bypass | `anima.kits.permission.claimfree` |


#### Duration Format

| Value | Meaning |
|---|---|
| `-1` | Permanent |
| `30s` | 30 seconds |
| `5m` | 5 minutes |
| `1h` | 1 hour |
| `12h` | 12 hours |
| `1d` | 1 day |
| `7d` | 7 days |

#### Global Selector Grants (`@a`, `@e`, `@p`, `@r`)

When you use a selector like `@a`, the permission is applied to all **currently online** players **and** stored as a global entry. Any player who joins the server in the future will automatically receive that permission when they connect.

```
/anima kits permission claim @a VIP Kit true -1
```

> ✓ All online players receive access immediately.  
> ✓ Every new player who joins later gets it automatically on join.  
> ✓ A confirmation message is shown: *"Global grant stored — new players joining will automatically receive this permission."*

To remove a global grant and stop it applying to new players:

```
/anima kits permission claim @a VIP Kit false
```

---

## Multi-Word Kit Names

Kit names with spaces work across **every** command. Tab completion is fully aware of multi-word names and will suggest word-by-word, then offer the next expected argument once the name is complete.

```
/anima kits rename Empty dude VIP Starter Kit
/anima kits lore add VIP Starter Kit Welcome to the VIP experience!
/anima kits permission claim @a VIP Starter Kit true -1
/anima kits delete VIP Starter Kit
```

---

## Formatting Guide

Full MiniMessage formatting is supported in kit names and lore. When typing in-game via command, include tags as-is. When using the chat rename prompt (GUI), prefix tags with a backslash `\` so the server processes them correctly.

### Gradients

```
<gradient:#FF5500:#FFCC00>Warrior Kit</gradient>
<gradient:#54DAF4:#545EB6:#A8FF33>Elite Season Kit</gradient>
<rainbow>Arcade Fun Kit</rainbow>
```

### Solid Colors

```
<red>Ruby Kit</red>
<gold>Gold Kit</gold>
<#A8FF33>Lime Kit</#A8FF33>
&#FF5500Lava Kit
```

### Style Modifiers

```
<bold>Heavy Kit</bold>          (&l)
<italic>Swift Kit</italic>      (&o)
<underline>God Kit</underline>  (&n)
```

### Combining Styles

```
/anima kits add <gradient:#54DAF4:#545EB6><bold>Season 3 Elite</bold></gradient>
/anima kits lore add Season 3 Elite <bold><red>⚠ WARNING:</red></bold> <italic>One-Time Claim Only!</italic>
```

---

## GUI Controls

| Click | Action |
|---|---|
| Left-click | View kit items (read-only) |
| Right-click | Open kit editor |
| Shift + Left-click | Clone kit |
| Shift + Right-click | Delete kit |
| Middle Mouse Click | Different actions based on GUI|

> **Note:** Minecraft does not forward `Ctrl+Click` to the server outside creative mode — use Shift+Click instead.

---

## Bedrock (Geyser + Floodgate)

Bedrock players connected via GeyserMC and Floodgate receive native Bedrock form menus powered by the Cumulus API instead of the Java inventory GUI. No extra configuration is needed — install Floodgate and Geyser and it works automatically.

---

## Data Storage

| File | Contents |
|---|---|
| `plugins/AnimaKits/config.yml` | Messages, prefix, date format |
| `plugins/AnimaKits/kits.yml` | All kit definitions (items, lore, cooldowns) |
| `plugins/AnimaKits/permissions.yml` | Per-player and global timed permission entries |

All files are hot-reloaded with `/anima kits reload` — no server restart needed.

## Credits

Created by **World Of Normies**  
[github.com/worldofnormies/AnimaKits](https://github.com/worldofnormies/Anima-Kits)
