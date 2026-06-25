# AnimaKits

A powerful, feature-rich kit management plugin for Paper servers — complete with premium gradient engines, paginated smart menus, interactive chat prompts, and global target variables.

---

## Compatibility

| Build | Minecraft Version | Java | Gradle |
|---|---|---|---|
| **26.1.2** | 26.1.0 · 26.1.1 · 26.1.2 | JDK 26+ | 9.4.0+ |
| **1.21.x** | 1.21.0 → 1.21.4 | JDK 21+ | 8.5+ / 9.x |

Both builds natively target PaperMC. Downstream ecosystem forks such as Purpur, Folia, or Pufferfish are fully supported.

---

## Features

- **Omnipresent Subcommands** — Subcommands register dynamically for all connected players to ensure command auto-completion populates properly. Strict permission verification is calculated during execution rather than hiding choices.
- **Global Claim Targets** — Setting permissions using the `free` selector makes designated items instantly claimable by everyone on the server, including brand-new player profile joins.
- **Paginated Smart Viewports** — Effortlessly handle deep server inventories and edit multi-page loadouts using visual pagination panels.
- **Timed Stored Permissions** — Grant custom temporary permissions inside a local flat-file storage directory, bypassing complex external permission networks for individual kits.
- **Immersive Chat Input Sessions** — Modify identifiers and properties directly inside chatboxes safely bounded within styled data layout boxes.
- **Smart Escape Sessions** — Real-time motion fallback listeners track spatial updates. Players can close chatboxes or hit Escape without typing anything, or write `/cancel`, to instantly safely terminate input sessions.
- **Real-Time Synchronisation** — Active administrative viewports update immediately when items are registered, modified, or updated.

---

## Installation

1. Download the preferred production JAR matching your environment architecture from the Releases page.
2. Drop the plugin binary directly into your server's `plugins/` directory.
3. Start or reload your server instance to generate required directories.
4. Customise localized messaging strings, visual titles, and interface constants inside `plugins/AnimaKits/config.yml`.

### Optional Dependencies

| Plugin | Purpose |
|---|---|
| Geyser + Floodgate | Custom Bedrock form engine rendering layers |
| PlaceholderAPI | Evaluates external variable expansions inside displays |

---

## Commands

### Player Subcommands

| Command | Description | Permission |
|---|---|---|
| `/anima kits` | Opens the graphical admin kit dashboard interface. | `anima.kits.use` |
| `/anima kit` | Opens the graphical player-facing kit claim workspace. | Available to all |
| `/anima kit claim` | Alternate shorthand to display the user claim screen. | Available to all |
| `/anima kit claim [kit]` | Claims a kit directly via command line execution. | `anima.kits.claim.[kit]` |

### Administrative Subcommands

| Command | Description | Permission |
|---|---|---|
| `/anima kit add [name]` | Generates a new loadout configuration block. | `anima.kits.add` |
| `/anima kit delete [kit]` | Erases an active configuration registry tracking item blocks. | `anima.kits.delete` |
| `/anima kit rename [kit] [new]` | Adjusts raw system and display names. | `anima.kits.rename` |
| `/anima kit lore add [kit] [text]` | Appends a text string directly to item descriptors. | `anima.kits.lore.add` |
| `/anima kit lore edit [kit] [#] [text]` | Replaces target lines with edited localized string details. | `anima.kits.lore.edit` |
| `/anima kit lore remove [kit] [#]` | Deletes selected layout indexes from display arrays. | `anima.kits.lore.remove` |
| `/anima kit clonekit [kit] [new]` | Duplicates an item config into a unique target profile. | `anima.kits.clonekit` |
| `/anima kit give [player|@a] [kit] [n]` | Pushes inventory blocks directly to targeted users. | `anima.kits.give` |
| `/anima kit giveall [kit] [n]` | Uniformly distributes sets to all active online player connections. | `anima.kits.giveall` |
| `/anima kit list` | Prints an itemized overview of tracking structures to text. | `anima.kits.list` |
| `/anima kit setcooldown [kit] [secs]` | Assigns waiting restrictions using standard integers. | `anima.kits.setcooldown` |
| `/anima kit singleclaim [kit] [t|f]` | Restricts access profiles to a singular activation sequence. | `anima.kits.singleclaim` |
| `/anima kit reload` | Hot-reloads values without server state interruptions. | `anima.kits.reload` |
| `/anima kit help` | Displays an inline formatted operations sheet. | `anima.kits.help` |

### Stored Permission Subcommands

| Command | Description | Permission |
|---|---|---|
| `/anima kit permission add [node] [player] [time]` | Attaches temporary permissions using formatted durations. | `anima.kits.permission.add` |
| `/anima kit permission remove [node] [player]` | Strips nodes instantly from custom file storage. | `anima.kits.permission.remove` |
| `/anima kit permission show [player]` | Reviews duration sheets and entries for a single user. | `anima.kits.permission.show` |
| `/anima kit permission claim [player|@a|free] [kit] [true|false] [time]` | Toggles kit claim rules for specific players, groups, or sets it globally free. | `anima.kits.permission.add` |

*Aliases:* `/anima`, `/ak`, `/animakits`

---

## Permissions

| Node | Description | Default |
|---|---|---|
| `anima.kits.*` | Complete operational control over all subcommands and parameters. | op |
| `anima.kits.use` | Grants rights to load administrative layout screens. | op |
| `anima.kits.add` | Grants rights to build profile data records. | op |
| `anima.kits.delete` | Grants rights to remove assets permanently. | op |
| `anima.kits.rename` | Grants rights to alter target visual strings. | op |
| `anima.kits.lore.add` | Grants rights to attach description arrays to files. | op |
| `anima.kits.lore.edit` | Grants rights to overwrite specified message rows. | op |
| `anima.kits.lore.remove` | Grants rights to truncate description tracking sheets. | op |
| `anima.kits.clonekit` | Grants rights to replicate loaded inventories instantly. | op |
| `anima.kits.give` | Grants rights to feed materials directly into target containers. | op |
| `anima.kits.giveall` | Grants rights to issue items to everyone globally. | op |
| `anima.kits.claim.*` | Wildcard enabling claim sequences across all configurations. | op |
| `anima.kits.claim.[kit]` | Authorizes a claim loop for a unique configuration path. | op |
| `anima.kits.list` | Authorizes reading database metadata summaries over text. | op |
| `anima.kits.setcooldown` | Authorizes assigning duration holds on properties. | op |
| `anima.kits.singleclaim` | Authorizes flipping lifetime access toggles on databases. | op |
| `anima.kits.reload` | Authorizes internal parameter file refreshes. | op |
| `anima.kits.help` | Access to view structural command information. | op |
| `anima.kits.permission.add` | Authorizes granting timed permission nodes. | op |
| `anima.kits.permission.remove` | Authorizes erasing user nodes manually. | op |
| `anima.kits.permission.show` | Authorizes structural profiling audits on individuals. | op |

---

## Colour & Gradient Support

Names and descriptions evaluate parsing formats natively across all inputs:

- **Legacy Constants:** Traditional codes (example: `&6&lGolden Kit`) apply fast vanilla properties.
- **Hex Formatting:** Standard web strings (example: `&#FF5500Lava Kit`) yield fine hexadecimal output variations.
- **MiniMessage Tokens:** Comprehensive tags (example: `<gradient:#54DAF4:#545EB6>My Kit</gradient>`) build clean gradient steps across components.

---

## Technical Notes

### Advanced UI Click Mapping
Because specific controls (like Ctrl+Click) are withheld by the vanilla client engine outside of creative gamemodes, alternative interaction layers coordinate administrative behaviors:
- **Left-Click:** Reviews interior item properties securely inside a protected sandbox window.
- **Right-Click:** Enters live inventory editing grids.
- **Shift + Left-Click:** Opens safe cloning duplication setups.
- **Shift + Right-Click:** Launches deletion prompt routines.

### Bedrock Compatibility Matrix
When Geyser and Floodgate stacks are present, the plugin uses a Bedrock form parser. This skips regular chest interface designs for Bedrock connections, transforming options into native mobile UI elements.

### Persistence Strategy
Data assets separate structural concerns into structured text files under `plugins/AnimaKits/`:
- `kits.yml` maintains core inventory setups, metadata tags, and duration targets.
- `permissions.yml` registers custom timestamp strings managing user node allocations.
- `players.yml` preserves cooldown tracking and single claim validation history.

---

## Building From Source

Automated GitHub actions utilize distinct workflow setups to generate deployments:

```bash
# To trigger an automated distribution release artifact build:
git tag v1.0.0
git push origin v1.0.0