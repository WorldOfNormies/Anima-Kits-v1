# 🎮 ANIMA - Complete Command Reference

All 45+ commands documented with examples and permissions.

---

## 🎁 KITS COMMANDS

### Player Commands

#### `/anima kits` 
Open the visual kit browser GUI
- **Permission:** `anima.kits.use` (default: true)
- **Aliases:** `/ak kits`

#### `/anima kits list`
View available authorized kits in text format
- **Permission:** `anima.kits.list` (default: true)

#### `/anima kits claim [kit_name]`
Directly claim a specific kit
- **Permission:** `anima.kits.claim` (default: true)
- **Example:** `/anima kits claim "Starter"`

#### `/anima kits help`
Display help instructions for players
- **Permission:** `anima.kits.help` (default: true)

---

### Admin Commands - Kit Management

#### `/anima kits add [name]`
Create a new kit
- **Permission:** `anima.kits.add`
- **Example:** `/anima kits add "VIP"`

#### `/anima kits delete [kit]`
Delete an existing kit permanently
- **Permission:** `anima.kits.delete`
- **Example:** `/anima kits delete "VIP"`

#### `/anima kits rename [kit] [new_name]`
Rename a kit (supports MiniMessage)
- **Permission:** `anima.kits.rename`
- **Example:** `/anima kits rename "Old" "<gradient:#FFD700:#FF8C00>Golden</gradient>"`

#### `/anima kits clonekit [source] [dest]`
Clone a kit to create a new one
- **Permission:** `anima.kits.clonekit`
- **Example:** `/anima kits clonekit "Starter" "Premium"`

---

### Admin Commands - Lore Management

#### `/anima kits lore add [kit] [text]`
Add a line of lore to a kit
- **Permission:** `anima.kits.lore.add`
- **Example:** `/anima kits lore add "Starter" "<gold>Welcome Kit!</gold>"`

#### `/anima kits lore edit [kit] [line#] [text]`
Edit an existing lore line
- **Permission:** `anima.kits.lore.edit`
- **Example:** `/anima kits lore edit "Starter" 1 "Updated text"`

#### `/anima kits lore remove [kit] [line#]`
Delete a lore line
- **Permission:** `anima.kits.lore.remove`
- **Example:** `/anima kits lore remove "Starter" 1`

---

### Admin Commands - Kit Distribution

#### `/anima kits give [player] [kit] [amount]`
Give kit to a specific player
- **Permission:** `anima.kits.give`
- **Example:** `/anima kits give Player "Starter" 1`

#### `/anima kits giveall [kit] [amount]`
Give kit to all online players
- **Permission:** `anima.kits.giveall`
- **Example:** `/anima kits giveall "Starter" 1`

#### `/anima kits onjoinnew [kit_name|clear]`
Set starter kit for new players
- **Permission:** `anima.kits.onjoinnew`
- **Example:** `/anima kits onjoinnew "Starter"`

---

### Admin Commands - Cooldown & Restrictions

#### `/anima kits setcooldown [kit] [time]`
Set cooldown for kit claiming
- **Permission:** `anima.kits.setcooldown`
- **Format:** `30s`, `5m`, `2h`, `1d`, `1d2h30m`
- **Example:** `/anima kits setcooldown "Starter" 7d`

#### `/anima kits singleclaim [kit] [true|false]`
Toggle one-time-only claims
- **Permission:** `anima.kits.singleclaim`
- **Example:** `/anima kits singleclaim "VIP" true`

---

### Admin Commands - Permissions

#### `/anima kits permission add [player] [perm] [duration]`
Grant permission to player
- **Permission:** `anima.kits.permission.add`
- **Duration:** `permanent`, `30d`, `7d`, etc.
- **Example:** `/anima kits permission add Player anima.kits.claim.vip permanent`

#### `/anima kits permission remove [player] [perm]`
Revoke permission from player
- **Permission:** `anima.kits.permission.remove`
- **Example:** `/anima kits permission remove Player anima.kits.claim.vip`

#### `/anima kits permission show [player]`
View all player permissions
- **Permission:** `anima.kits.permission.show`
- **Example:** `/anima kits permission show Player`

#### `/anima kits permission claim [player] [kit] [true|false]`
Grant/revoke kit access
- **Permission:** `anima.kits.permission.claim`
- **Example:** `/anima kits permission claim Player "VIP" true`

#### `/anima kits permission claimfree [player] [kit] [true|false]`
Bypass cooldown for player
- **Permission:** `anima.kits.permission.claimfree`
- **Example:** `/anima kits permission claimfree Player "VIP" true`

---

### System Commands

#### `/anima kits reload`
Reload all kit configuration
- **Permission:** `anima.kits.reload`

---

## ⭐ RANKS COMMANDS

### Player Commands

#### `/anima ranks`
Open ranks GUI
- **Permission:** `anima.ranks.gui` (default: true)

#### `/anima ranks list`
View all ranks with details
- **Permission:** `anima.ranks.list` (default: true)

#### `/anima ranks rankup`
Rank up to next rank (if eligible)
- **Permission:** `anima.ranks.rankup` (default: true)

---

### Admin Commands - Rank Management

#### `/anima ranks add [rank_name]`
Create a new rank
- **Permission:** `anima.ranks.add`
- **Example:** `/anima ranks add "Member"`

#### `/anima ranks delete [rank_name]`
Delete a rank
- **Permission:** `anima.ranks.delete`
- **Example:** `/anima ranks delete "Member"`

#### `/anima ranks set [rank_name] [property] [value]`
Set rank properties
- **Permission:** `anima.ranks.set`
- **Properties:** `prefix`, `suffix`, `colorname`, `chatcolor`, `isrankable`, `isbuyable`
- **Example:** `/anima ranks set "Member" prefix "&8[&bMember&8]"`

---

### Admin Commands - Player Assignment

#### `/anima ranks assign [player] [rank]`
Assign rank to player
- **Permission:** `anima.ranks.assign`
- **Example:** `/anima ranks assign Player "Member"`

#### `/anima ranks promote [player]`
Promote player to next rank
- **Permission:** `anima.ranks.promote`
- **Example:** `/anima ranks promote Player`

#### `/anima ranks demote [player]`
Demote player to previous rank
- **Permission:** `anima.ranks.promote`
- **Example:** `/anima ranks demote Player`

---

### Admin Commands - Permissions

#### `/anima ranks permissions [rank] [add|remove] [perm_node]`
Manage rank permissions
- **Permission:** `anima.ranks.permissions`
- **Example:** `/anima ranks permissions "Member" add essentials.home`

---

## ✏️ ITEM EDITOR COMMANDS

### Player Commands

#### `/anima itemedit rename [name]`
Rename held item (MiniMessage supported)
- **Permission:** `anima.itemedit.rename` (default: false)
- **Example:** `/anima itemedit rename "<bold><red>Legendary Sword</red></bold>"`

#### `/anima itemedit prefix [text|remove]`
Add prefix to item name
- **Permission:** `anima.itemedit.prefix` (default: false)
- **Example:** `/anima itemedit prefix "<gold>✦ </gold>"`

#### `/anima itemedit suffix [text|remove]`
Add suffix to item name
- **Permission:** `anima.itemedit.suffix` (default: false)
- **Example:** `/anima itemedit suffix " <gold>✦</gold>"`

#### `/anima itemedit repair`
Repair held item to full durability
- **Permission:** `anima.itemedit.repair` (default: false)
- **Cooldown:** 300 seconds (configurable)

---

### Admin Commands - Item Customization

#### `/anima itemedit rename [name]`
Rename item (admin version, no cooldown)
- **Permission:** `anima.itemedit.admin` or `anima.itemedit.rename`
- **Example:** `/anima itemedit rename "<gradient:#FF0000:#0000FF>Rainbow Sword</gradient>"`

#### `/anima itemedit lore [add|edit|remove|clear] [line#] [text]`
Manage item lore
- **Permission:** `anima.itemedit.admin` or `anima.itemedit.lore`
- **Example:** `/anima itemedit lore add 1 "<gold>Legendary Weapon</gold>"`

#### `/anima itemedit enchant [enchantment] [level]`
Add/modify enchantments
- **Permission:** `anima.itemedit.admin` or `anima.itemedit.enchant`
- **Example:** `/anima itemedit enchant sharpness 32`

#### `/anima itemedit glow [on|off]`
Add/remove glow effect
- **Permission:** `anima.itemedit.admin` or `anima.itemedit.glow`
- **Example:** `/anima itemedit glow on`

#### `/anima itemedit unbreakable [on|off]`
Toggle unbreakable flag
- **Permission:** `anima.itemedit.admin` or `anima.itemedit.unbreakable`
- **Example:** `/anima itemedit unbreakable on`

---

## 📊 Command Summary

| System | Player Commands | Admin Commands | Total |
|--------|-----------------|-----------------|-------|
| Kits | 4 | 16 | 20 |
| Ranks | 3 | 7 | 10 |
| Item Editor | 4 | 6 | 10 |
| **TOTAL** | **11** | **29** | **40+** |

---

## 🔗 Command Aliases

| Alias | Full Command |
|-------|--------------|
| `/ak` | `/anima` |
| `/ak kits` | `/anima kits` |

---

## 📖 Need More Info?

- **[Quick Start](README-QUICKSTART.md)** - Get started fast
- **[Permissions](README-PERMISSIONS.md)** - Permission reference
- **[Configuration](README-CONFIGURATION.md)** - Setup guide
- **[Usage Guide](README-GUIDE.md)** - Examples and tips

---

**⚡ ANIMA - All Commands Documented**

© 2024 World Of Normies
