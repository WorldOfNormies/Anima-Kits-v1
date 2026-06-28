# 📖 ANIMA - Complete Usage Guide

Advanced examples and best practices.

---

## 🎁 Kits Guide

### Creating a Complete Kit System

**Step 1: Create Base Kits**

```bash
/anima kits add "Starter"
/anima kits add "VIP"
/anima kits add "Premium"
```

**Step 2: Add Descriptions**

```bash
/anima kits lore add "Starter" "<gold>Starter Kit</gold>"
/anima kits lore add "Starter" "<gray>Basic items for new players</gray>"

/anima kits lore add "VIP" "<gold>VIP Kit</gold>"
/anima kits lore add "VIP" "<gray>Premium items</gray>"
/anima kits lore add "VIP" "<gray>Includes full diamond armor</gray>"
```

**Step 3: Set Cooldowns**

```bash
/anima kits setcooldown "Starter" 0      # No cooldown
/anima kits setcooldown "VIP" 1d         # 1 day
/anima kits setcooldown "Premium" 7d    # 7 days
```

**Step 4: Set New Player Kit**

```bash
/anima kits onjoinnew "Starter"
```

**Step 5: Distribute to Players**

```bash
/anima kits give Player "Starter" 1
/anima kits giveall "Starter" 1
```

---

### Kit Best Practices

✅ **DO:**
- Name kits clearly (Starter, VIP, Event, etc.)
- Add helpful lore descriptions
- Set reasonable cooldowns
- Use permissions for special kits
- Test kits before distributing

❌ **DON'T:**
- Create too many similar kits
- Give unlimited items
- Set unreasonable cooldowns
- Forget to add descriptions
- Give admin items to regular players

---

## ⭐ Ranks Guide

### Setting Up a Rank Hierarchy

**Step 1: Create Ranks (in order)**

```bash
/anima ranks add "Newcomer"
/anima ranks add "Member"
/anima ranks add "Premium"
/anima ranks add "VIP"
/anima ranks add "Moderator"
/anima ranks add "Admin"
```

**Step 2: Set Prefixes**

```bash
/anima ranks set "Newcomer" prefix "&8[&7Newcomer&8]"
/anima ranks set "Member" prefix "&8[&bMember&8]"
/anima ranks set "Premium" prefix "&8[&6Premium&8]"
/anima ranks set "VIP" prefix "&8[&5VIP&8]"
/anima ranks set "Moderator" prefix "&8[&cMod&8]"
/anima ranks set "Admin" prefix "&8[&4Admin&8]"
```

**Step 3: Add Permissions**

```bash
# Member permissions
/anima ranks permissions "Member" add essentials.home
/anima ranks permissions "Member" add essentials.mail
/anima ranks permissions "Member" add essentials.back

# VIP permissions
/anima ranks permissions "VIP" add essentials.vip
/anima ranks permissions "VIP" add essentials.kit
/anima ranks permissions "VIP" add essentials.worth

# Mod permissions
/anima ranks permissions "Moderator" add essentials.kick
/anima ranks permissions "Moderator" add essentials.ban
/anima ranks permissions "Moderator" add essentials.warn
```

**Step 4: Assign Players**

```bash
/anima ranks assign Player "Newcomer"
/anima ranks promote Player  # Now Member
/anima ranks promote Player  # Now Premium
/anima ranks promote Player  # Now VIP
```

---

### Rank Best Practices

✅ **DO:**
- Create a clear hierarchy
- Use consistent naming
- Assign meaningful permissions
- Update player ranks regularly
- Test permission inheritance

❌ **DON'T:**
- Mix rank order
- Give too many permissions
- Create too many ranks
- Skip ranks for players
- Change prefix mid-season

---

## ✏️ Item Customization Guide

### Creating a Legendary Item

**Step 1: Get the Base Item**

Hold the item in your hand (sword, pickaxe, etc.)

**Step 2: Rename with Gradient**

```bash
/anima itemedit rename "<gradient:#FF0000:#0000FF>Legend's Blade</gradient>"
```

**Step 3: Add Enchantments**

```bash
/anima itemedit enchant sharpness 5
/anima itemedit enchant knockback 2
/anima itemedit enchant unbreaking 3
/anima itemedit enchant looting 3
```

**Step 4: Add Glow**

```bash
/anima itemedit glow on
```

**Step 5: Make Unbreakable**

```bash
/anima itemedit unbreakable on
```

**Step 6: Add Lore**

```bash
/anima itemedit lore add 1 "<gold>━━━━━━━━━━━━━━━━━</gold>"
/anima itemedit lore add 2 "<gold>A legendary weapon</gold>"
/anima itemedit lore add 3 "<gray>Forged in ancient times</gray>"
/anima itemedit lore add 4 "<gold>━━━━━━━━━━━━━━━━━</gold>"
```

### Item Customization Examples

**Rainbow Sword**
```bash
/anima itemedit rename "<rainbow>Rainbow Sword</rainbow>"
/anima itemedit enchant sharpness 10
/anima itemedit glow on
```

**Golden Pickaxe**
```bash
/anima itemedit rename "<#FFD700>Golden Pickaxe</#FFD700>"
/anima itemedit enchant efficiency 5
/anima itemedit enchant unbreaking 5
```

**Fire Staff (Axe)**
```bash
/anima itemedit rename "<gradient:#FF4500:#FFD700>Fire Staff</gradient>"
/anima itemedit enchant knockback 5
/anima itemedit enchant unbreaking 3
/anima itemedit glow on
```

---

## 🎨 MiniMessage Examples

### Text Effects

```
<bold>Bold text</bold>
<italic>Italic text</italic>
<underline>Underlined text</underline>
```

### Color Examples

```
<red>Red text</red>
<blue>Blue text</blue>
<gold>Gold text</gold>
<green>Green text</green>
<dark_red>Dark Red</dark_red>
```

### Custom Hex Colors

```
<#FF0000>Pure red</color>
<#00FF00>Pure green</color>
<#0000FF>Pure blue</color>
<#FFD700>Gold</color>
<#FF1493>Deep pink</color>
```

### Gradients

**Two-Color Gradient**
```
<gradient:#FF0000:#0000FF>Red to blue gradient</gradient>
```

**Three-Color Gradient**
```
<gradient:#FF0000:#00FF00:#0000FF>Red, green, blue gradient</gradient>
```

### Rainbow

```
<rainbow>Rainbow colored text</rainbow>
<rainbow phase="0.5">Rainbow with phase</rainbow>
```

### Combined Effects

```
<bold><gradient:#FFD700:#FF8C00>Golden Bold</gradient></bold>
<italic><blue>Italic blue</blue></italic>
<underline><#FF1493>Underlined pink</underline></color>
```

---

## ⏱️ Cooldown Format Examples

### Time Formats
```
30s    = 30 seconds
5m     = 5 minutes
2h     = 2 hours
1d     = 1 day
1d2h   = 1 day 2 hours
1d2h30m = 1 day 2 hours 30 minutes
```

### Practical Cooldowns
```
/anima kits setcooldown "Starter" 0       # No cooldown
/anima kits setcooldown "Daily" 24h       # 1 day
/anima kits setcooldown "Weekly" 7d       # 7 days
/anima kits setcooldown "Monthly" 30d     # 30 days
/anima kits setcooldown "Premium" 1h      # 1 hour
```

---

## 📊 Real-World Scenarios

### Scenario 1: Prison Server

```bash
# Setup
/anima kits add "Miner"
/anima kits lore add "Miner" "Mining supplies"
/anima kits setcooldown "Miner" 24h

/anima ranks add "Block1"
/anima ranks add "Block2"
/anima ranks add "Block3"
```

### Scenario 2: Survival Server

```bash
# New player kit
/anima kits onjoinnew "Survival"
/anima kits lore add "Survival" "Starter tools"

# Create ranks
/anima ranks add "Builder"
/anima ranks permissions "Builder" add essentials.home
```

### Scenario 3: PvP Server

```bash
# Create kits
/anima kits add "NoDebuff"
/anima kits add "Debuff"
/anima kits add "UHC"

# Set cooldowns
/anima kits setcooldown "NoDebuff" 0
/anima kits setcooldown "Debuff" 0
/anima kits setcooldown "UHC" 0
```

---

## 🔧 Troubleshooting Guide

### Problem: "Permission Denied"
**Solution:**
```bash
/anima kits permission show [player]
# Check if they have the right permission
```

### Problem: "Kit Not Found"
**Solution:**
```bash
/anima kits list
# Verify kit name spelling and capitalization
```

### Problem: "Cooldown Active"
**Solution:**
```bash
# Wait for cooldown or set to 0:
/anima kits setcooldown [kit] 0
```

### Problem: "Item Won't Glow"
**Solution:**
```bash
# Make sure you're holding an item:
/anima itemedit glow on
# Check for plugin conflicts
```

### Problem: "Gradient Not Working"
**Solution:**
- Use correct format: `<gradient:#HEX1:#HEX2>text</gradient>`
- Ensure hex codes are valid
- Check MiniMessage support in your chat plugin

---

## 💡 Advanced Tips

### Tip 1: Chain Permissions
```bash
/anima kits permission add Player anima.kits.claim.vip 7d
# Grant for 7 days, then expires automatically
```

### Tip 2: Bulk Give Kits
```bash
/anima kits giveall "Event" 1
# Give to all online players at once
```

### Tip 3: Clone and Modify
```bash
/anima kits clonekit "Starter" "StarterPlus"
# Clone then customize the new kit
```

### Tip 4: Prefix/Suffix Combinations
```bash
/anima itemedit prefix "<gold>✦ </gold>"
# Adds before name
/anima itemedit suffix " <gold>✦</gold>"
# Adds after name
```

---

## 🎯 Performance Tips

- Don't create too many kits (keep under 100)
- Use meaningful cooldowns to prevent spam
- Reload configuration regularly
- Backup your data folder
- Monitor server logs for errors

---

## 📚 More Documentation

- **[Quick Start](README-QUICKSTART.md)** - 5-minute setup
- **[Commands](README-COMMANDS.md)** - All commands
- **[Permissions](README-PERMISSIONS.md)** - Permission nodes
- **[Configuration](README-CONFIGURATION.md)** - Setup guide

---

**⚡ ANIMA - Master These Guides**

© 2024 World Of Normies
