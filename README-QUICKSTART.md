# 🚀 ANIMA - Quick Start Guide

Get up and running in 5 minutes!

---

## ⚡ Essential Commands

### For Players

```bash
/anima kits              # Open kit browser
/anima kits list         # View available kits
/anima kits claim [kit]  # Claim a kit
/anima ranks             # View ranks
/anima ranks rankup      # Rank up
```

### For Admins

```bash
# Kit Management
/anima kits add [name]              # Create kit
/anima kits give [player] [kit]     # Give kit to player
/anima kits setcooldown [kit] [time]  # Set cooldown (1d, 2h, 30m)

# Rank Management  
/anima ranks add [name]             # Create rank
/anima ranks promote [player]       # Promote player
/anima ranks assign [player] [rank] # Assign rank

# Item Editor
/anima itemedit rename [name]       # Rename item
/anima itemedit enchant [ench] [lvl]  # Add enchantment
/anima itemedit glow on             # Add glow effect
```

---

## 🏗️ Setup in 3 Steps

### Step 1: Install
```bash
1. Download AnimaMenu-1.1.6.jar
2. Drop in plugins/ folder
3. Restart server
```

### Step 2: Create Your First Kit
```bash
/anima kits add "Starter"
/anima kits lore add "Starter" "Welcome Kit!"
/anima kits give @s "Starter" 1
```

### Step 3: Create Your First Rank
```bash
/anima ranks add "Member"
/anima ranks set "Member" prefix "[Member]"
/anima ranks assign Player "Member"
```

---

## 📖 Real-World Examples

### Example 1: Create a VIP Kit

```bash
# Create kit
/anima kits add "VIP"

# Add description
/anima kits lore add "VIP" "Premium starter kit"
/anima kits lore add "VIP" "Includes full diamond armor"

# Set 7-day cooldown
/anima kits setcooldown "VIP" 7d

# Give to a player
/anima kits give Player "VIP" 1
```

### Example 2: Setup Rank Progression

```bash
# Create ranks
/anima ranks add "Beginner"
/anima ranks add "Member"  
/anima ranks add "VIP"
/anima ranks add "Premium"

# Set prefixes
/anima ranks set "Beginner" prefix "&8[&9Beginner&8]"
/anima ranks set "Member" prefix "&8[&bMember&8]"
/anima ranks set "VIP" prefix "&8[&6VIP&8]"
/anima ranks set "Premium" prefix "&8[&cPremium&8]"

# Assign player
/anima ranks assign Player "Beginner"

# Promote player
/anima ranks promote Player
```

### Example 3: Customize an Item

```bash
# Hold item in hand, then:

# Rename with gradient
/anima itemedit rename "<gradient:#FF0000:#0000FF>Magic Sword</gradient>"

# Add enchantments
/anima itemedit enchant sharpness 5
/anima itemedit enchant unbreaking 3
/anima itemedit enchant knockback 2

# Add glow
/anima itemedit glow on

# Make unbreakable
/anima itemedit unbreakable on

# Add lore
/anima itemedit lore add 1 "<gold>A legendary weapon</gold>"
/anima itemedit lore add 2 "<gray>Handle with care</gray>"
```

---

## 🎨 Quick Text Formatting

Use these in names, lore, and messages:

```
<bold>Bold</bold>
<italic>Italic</italic>  
<red>Red</red>
<green>Green</green>
<blue>Blue</blue>
<gold>Gold</gold>

<#FFD700>Custom Gold</color>
<gradient:#FF0000:#0000FF>Red to Blue Gradient</gradient>
<rainbow>Rainbow Text</rainbow>
```

---

## 📋 Time Format Examples

Use these for cooldowns:

```
30s   = 30 seconds
5m    = 5 minutes
2h    = 2 hours
1d    = 1 day
1d2h30m = 1 day, 2 hours, 30 minutes
```

---

## ✅ Common Tasks

### Add Lore to a Kit
```bash
/anima kits lore add "KitName" "<gold>Line of text</gold>"
```

### Set Kit to Give on First Join
```bash
/anima kits onjoinnew "Starter"
```

### Remove a Kit's Cooldown
```bash
/anima kits setcooldown "KitName" 0
```

### List All Commands
```bash
/anima kits help
/anima ranks help
/anima itemedit help
```

### Reload Configuration
```bash
/anima kits reload
```

---

## 🔐 Basic Permissions

| Permission | What it does |
|-----------|------------|
| `anima.kits.*` | Everything kits |
| `anima.kits.use` | Open kit GUI |
| `anima.kits.claim` | Claim kits |
| `anima.ranks.*` | Everything ranks |
| `anima.itemedit.*` | Everything item editor |

---

## 🆘 Quick Troubleshooting

### Commands don't work?
✓ Check permissions  
✓ Check player is online  
✓ Check for typos  

### Kits not saving?
✓ Check `plugins/AnimaMenu/kits/` folder exists  
✓ Check folder permissions (must be writable)  

### Permission denied?
✓ Grant permission in permission plugin  
✓ Use `/anima kits permission show [player]`  

### Cooldowns not working?
✓ Use correct format: `1d`, `2h`, `30m`, `30s`  
✓ Verify with `/anima kits list`

---

## 📚 Need More Help?

- **[Full Command Reference](README-COMMANDS.md)**
- **[Complete Permissions List](README-PERMISSIONS.md)**
- **[Configuration Guide](README-CONFIGURATION.md)**
- **[Usage Examples](README-GUIDE.md)**

---

⚡ **Ready to go!** Use the commands above to get started.

For detailed documentation, see [README.md](README.md)
