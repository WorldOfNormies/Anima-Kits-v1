# ⚡ ANIMA v1.1.6

> A powerful, feature-rich kit, rank, and item management plugin for Minecraft servers

```
    ___    _   ________  _______  
   / _ |  / | / /  _/  |/  / __ |
  / __ | /  |/ // // /|_/ / /_/ /
 / ___ |/ /|  // // /|  / / ____/ 
/_/  |_/_/ |_/___/_/ |_/_/_/      
```

**Version:** 1.1.6  
**Status:** ✅ Production Ready  
**API Version:** Minecraft 1.21+

---

## 📚 Documentation Files

This documentation is broken into organized sections for easy navigation:

| File | Purpose |
|------|---------|
| **[README-QUICKSTART.md](README-QUICKSTART.md)** | 🚀 Get started in 5 minutes |
| **[README-COMMANDS.md](README-COMMANDS.md)** | 🎮 All 45+ commands documented |
| **[README-PERMISSIONS.md](README-PERMISSIONS.md)** | 🔐 All 30+ permissions explained |
| **[README-CONFIGURATION.md](README-CONFIGURATION.md)** | ⚙️ Setup and customization |
| **[README-GUIDE.md](README-GUIDE.md)** | 📖 Complete usage guide |

---

## 🌟 What is ANIMA?

ANIMA is an all-in-one Minecraft server management plugin providing:

- **🎁 Kits System** - Distribute item collections with cooldowns
- **⭐ Ranks System** - Complete player progression framework  
- **✏️ Item Editor** - Advanced item customization with MiniMessage

---

## ✨ Key Features

✅ **Gradient Text Support** - Full MiniMessage formatting  
✅ **Multi-Page GUIs** - Navigate large collections easily  
✅ **Fine-Grained Permissions** - Control access per player  
✅ **Cooldown System** - Prevent kit abuse  
✅ **Playtime Tracking** - Track player progression  
✅ **Bedrock Support** - Works with Geyser/Floodgate  
✅ **Fully Configurable** - Customize all messages  
✅ **Hot Reload** - No restart needed  

---

## 📦 Installation

### Requirements
- **Server:** Spigot, Paper, or compatible fork
- **Java:** 21 or higher  
- **Minecraft:** 1.21+

### Quick Install
1. Download `AnimaMenu-1.1.6.jar`
2. Place in `plugins/` folder
3. Restart server
4. Done! Configuration files auto-generate

[👉 Full installation guide](README-CONFIGURATION.md#installation)

---

## 🚀 Quick Start

```bash
# Open kit browser
/anima kits

# View available kits
/anima kits list

# Claim a kit
/anima kits claim [kit_name]

# Admin: Create a kit
/anima kits add [name]

# Admin: Give kit to player
/anima kits give [player] [kit]
```

[👉 More quick start examples](README-QUICKSTART.md)

---

## 🎮 Command Overview

**Kits:** 20+ commands for kit management  
**Ranks:** 15+ commands for rank progression  
**Item Editor:** 10+ commands for item customization  

[👉 View all commands](README-COMMANDS.md)

---

## 🔐 Permissions

| Permission | Description | Default |
|-----------|-------------|---------|
| `anima.kits.*` | All kit commands | OP |
| `anima.kits.use` | Use kit system | true |
| `anima.ranks.*` | All rank commands | OP |
| `anima.itemedit.*` | Item editing | OP |

[👉 Complete permissions list](README-PERMISSIONS.md)

---

## ⚙️ Configuration

Main config: `plugins/AnimaMenu/config.yml`

```yaml
general:
  prefix: "<gradient:#54DAF4:#545EB6><bold>[Anima]</bold></gradient>"
  date-format: "dd/MM/yyyy HH:mm"

join-kit: ""  # Kit for new players

itemedit:
  repair-cooldown: 300  # Seconds
```

[👉 Full configuration guide](README-CONFIGURATION.md)

---

## 🎨 MiniMessage Formatting

```
<bold>Bold</bold>
<italic>Italic</italic>
<red>Red text</red>
<#FFD700>Gold text</color>
<gradient:#FF0000:#0000FF>Gradient</gradient>
<rainbow>Rainbow text</rainbow>
```

[👉 More formatting examples](README-GUIDE.md#minmessage)

---

## 📋 Three Powerful Systems

### 🎁 Kits System
Distribute predefined item collections with:
- Cooldown restrictions
- Permission-based access
- One-time-only claims
- Multi-page GUI browsing

### ⭐ Ranks System
Complete player progression with:
- Rank hierarchy
- Playtime tracking
- Custom prefix/suffix
- Permission inheritance

### ✏️ Item Editor
Customize items with:
- Custom names and lore
- Enchantments
- Glow effects
- Full MiniMessage support

---

## 🔧 Build from Source

```bash
cd integrated_project_fixed/
./gradlew shadowJar --no-daemon
# Output: build/libs/AnimaMenu-1.1.6.jar
```

---

## 📞 Support

- **GitHub:** [worldofnormies/Anima](https://github.com/worldofnormies/Anima)
- **Issues:** Report bugs on GitHub
- **Documentation:** See README files above

---

## 📖 What to Read Next?

- **New users:** Start with [README-QUICKSTART.md](README-QUICKSTART.md)
- **Need commands?** See [README-COMMANDS.md](README-COMMANDS.md)
- **Setting up?** Check [README-CONFIGURATION.md](README-CONFIGURATION.md)
- **Want examples?** Read [README-GUIDE.md](README-GUIDE.md)

---

## 🎉 Version Info

**Latest Version:** 1.1.6  
**Status:** ✅ Production Ready  
**Java:** 21+  
**Minecraft:** 1.21+

---

**⚡ ANIMA - Making Minecraft Server Management Powerful & Simple**

© 2024 World Of Normies | Built with ❤️ for Server Administrators
