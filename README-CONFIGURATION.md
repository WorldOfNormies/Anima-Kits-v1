# ⚙️ ANIMA - Configuration & Setup Guide

Complete setup and customization guide.

---

## 📦 Installation

### Requirements
- **Server:** Spigot, Paper, or compatible fork
- **Java:** 21 or higher
- **Minecraft:** 1.21+
- **Optional:** PlaceholderAPI, Vault, Floodgate

### Installation Steps

1. **Download** `AnimaMenu-1.1.6.jar`
2. **Place** in `plugins/` directory
3. **Restart** your server (or use plugin reload)
4. **Verify** `plugins/AnimaMenu/` folder was created
5. **Edit** `plugins/AnimaMenu/config.yml` if needed
6. **Restart** again to apply changes

### File Structure
```
plugins/
└── AnimaMenu/
    ├── config.yml              # Main configuration
    ├── kits/                   # Kit definitions
    │   └── kit_name.yml
    ├── ranks/                  # Rank configurations
    │   └── rank_name.yml
    └── players/                # Player data
        └── [UUID].yml
```

---

## ⚙️ Configuration File

**Location:** `plugins/AnimaMenu/config.yml`

### Default Configuration

```yaml
general:
  # Date format for displaying dates
  date-format: "dd/MM/yyyy HH:mm"
  
  # Plugin prefix (supports MiniMessage)
  prefix: "<gradient:#54DAF4:#545EB6><bold>[Anima]</bold></gradient>"

# Kit given to new players on first join
# Leave empty to disable
join-kit: ""

itemedit:
  # Cooldown in seconds for repair command (0 = disabled)
  repair-cooldown: 300
```

---

## 🎨 Customizing Messages

All messages support **MiniMessage formatting**. Edit in `config.yml`:

```yaml
messages:
  # Error messages
  no-permission: "<red>✕ You don't have permission!</red>"
  player-not-found: "<red>Player not found!</red>"
  invalid-args: "<red>Invalid arguments!</red>"
  
  # Kit messages
  kit-created: "<gold>✓ Kit created successfully!</gold>"
  kit-deleted: "<gold>✓ Kit deleted!</gold>"
  kit-received: "<green>You received the <bold>{kit}</bold> kit!</green>"
  kit-cooldown: "<red>This kit is on cooldown! ({time} remaining)</red>"
  
  # Rank messages
  rank-up: "<gradient:#FFD700:#FFA500><bold>Congratulations! You've ranked up!</bold></gradient>"
  rank-assigned: "<gold>You've been assigned rank: {rank}</gold>"
  
  # Item editor messages
  item-renamed: "<gold>Item renamed successfully!</gold>"
  item-repaired: "<gold>Item repaired!</gold>"
```

---

## 🎨 MiniMessage Formatting

### Text Styling
```
<bold>Bold text</bold>
<italic>Italic text</italic>
<underline>Underlined text</underline>
<strikethrough>Strikethrough</strikethrough>
<obfuscated>Obfuscated</obfuscated>
```

### Colors
```
<black>Black</black>
<dark_blue>Dark Blue</dark_blue>
<dark_green>Dark Green</dark_green>
<dark_aqua>Dark Aqua</dark_aqua>
<dark_red>Dark Red</dark_red>
<dark_purple>Dark Purple</dark_purple>
<gold>Gold</gold>
<gray>Gray</gray>
<dark_gray>Dark Gray</dark_gray>
<blue>Blue</blue>
<green>Green</green>
<aqua>Aqua</aqua>
<red>Red</red>
<light_purple>Light Purple</light_purple>
<yellow>Yellow</yellow>
<white>White</white>
```

### Custom Colors
```
<#FFD700>Custom color by hex code</color>
```

### Gradients
```
<gradient:#FF0000:#0000FF>Red to blue gradient</gradient>
<gradient:#FF0000:#00FF00:#0000FF>Three-color gradient</gradient>
```

### Rainbow
```
<rainbow>Rainbow colored text</rainbow>
```

---

## 📝 Configuration Examples

### Example 1: Premium Gradient Prefix

```yaml
general:
  prefix: "<gradient:#FFD700:#FF8C00><bold>⚡ ANIMA ⚡</bold></gradient>"
```

### Example 2: Custom Kit Messages

```yaml
messages:
  kit-received: |
    <gradient:#54DAF4:#545EB6>
    ✦ You received the <bold>{kit}</bold> kit!
    </gradient>
```

### Example 3: Colorful Error Messages

```yaml
messages:
  no-permission: "<#FF4B4B><bold>✕</bold> <#FF6B6B>Access Denied!</#FF6B6B></color>"
```

---

## 🔄 Reloading Configuration

Changes take effect immediately:

```bash
/anima kits reload
```

No server restart needed!

---

## 📁 Kit Configuration

Each kit is stored in: `plugins/AnimaMenu/kits/[name].yml`

### Kit File Format

```yaml
display-name: "Starter Kit"
lore:
  - "Welcome to the server!"
  - "This kit contains basics"

items:
  - type: DIAMOND_SWORD
    quantity: 1
    enchantments:
      sharpness: 5
      unbreaking: 3
  
  - type: DIAMOND_PICKAXE
    quantity: 1
  
  - type: COOKED_BEEF
    quantity: 64

cooldown: 604800  # 7 days in seconds
single-claim: false
permissions: []
```

---

## 📊 Rank Configuration

Each rank is stored in: `plugins/AnimaMenu/ranks/[name].yml`

### Rank File Format

```yaml
hierarchy: 1
prefix: "&8[&bMember&8]"
suffix: ""
color-name: "&b"
chat-color: "&7"
is-rankable: true
is-buyable: false
permissions:
  - essentials.home
  - essentials.warp
  - essentials.mail
```

---

## 🔧 Advanced Settings

### Performance Optimization

For servers with many kits/ranks, adjust:

```yaml
general:
  # Cache settings
  cache-time: 3600
  
  # Max items per page in GUI
  items-per-page: 27
```

### Custom Date Formats

```yaml
general:
  date-format: "yyyy-MM-dd HH:mm:ss"    # 2024-06-28 15:30:45
  date-format: "MMM d, yyyy"             # Jun 28, 2024
  date-format: "EEEE, MMMM d, yyyy"    # Friday, June 28, 2024
```

---

## 🌐 Optional Dependencies

### PlaceholderAPI Support

Install PlaceholderAPI to use placeholders in messages:

```yaml
messages:
  kit-received: "You got {kit}! Enjoy, {player}!"
```

### Vault Integration

For economy features:

```bash
# Install Vault plugin
# Install economy plugin (Essentials, CMI, etc.)
# Configure in config.yml
```

---

## 🆘 Troubleshooting Configuration

### Config file not loading?
- Check syntax with YAML validator
- Ensure proper indentation (spaces, not tabs)
- Check file encoding (UTF-8)
- Check console for error messages

### Messages not formatting?
- Verify MiniMessage syntax
- Check color codes are correct
- Use `<#HEXCODE>` for custom colors
- Test with simple colors first

### Kits/Ranks not working?
- Verify files exist in correct folders
- Check file names and permissions
- Verify YAML syntax in kit/rank files
- Reload with `/anima kits reload`

---

## 💾 Backup Configuration

Always backup before changes:

```bash
# Backup before updating
cp -r plugins/AnimaMenu/ backups/AnimaMenu_backup/

# Restore if needed
cp -r backups/AnimaMenu_backup/* plugins/AnimaMenu/
```

---

## 📖 Configuration Best Practices

✅ **DO:**
- Backup before major changes
- Test on dev server first
- Use consistent formatting
- Comment your config
- Document custom changes

❌ **DON'T:**
- Edit config while server is running
- Use tabs (use spaces instead)
- Mix up nesting levels
- Leave incomplete sections
- Ignore error messages

---

## 🔗 Related Documentation

- **[Quick Start](README-QUICKSTART.md)** - Quick setup guide
- **[Commands](README-COMMANDS.md)** - How to manage kits/ranks
- **[Permissions](README-PERMISSIONS.md)** - Permission settings
- **[Usage Guide](README-GUIDE.md)** - Complete examples

---

**⚡ ANIMA - Configuration Complete**

© 2024 World Of Normies
