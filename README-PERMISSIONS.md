# 🔐 ANIMA - Complete Permissions Reference

All 30+ permission nodes explained.

---

## 🎁 KITS PERMISSIONS

### Master Permission
**`anima.kits.*`**
- Grants access to ALL kit commands and features

### Player Permissions (Default: true)

**`anima.kits.use`**
- Open the visual kit browser GUI
- What: Can use `/anima kits` command

**`anima.kits.claim`**  
- Claim kits directly via command
- What: Can use `/anima kits claim [kit]`

**`anima.kits.claim.*`**
- Claim any existing kit by default
- What: Can claim all kits without individual permissions

**`anima.kits.list`**
- View available authorized kits
- What: Can use `/anima kits list`

**`anima.kits.help`**
- View help instructions
- What: Can use `/anima kits help`

### Admin Permissions

**`anima.kits.add`**
- Create new kits
- What: `/anima kits add [name]`

**`anima.kits.delete`**
- Delete existing kits
- What: `/anima kits delete [kit]`

**`anima.kits.rename`**
- Rename kits
- What: `/anima kits rename [kit] [name]`

**`anima.kits.clonekit`**
- Clone/duplicate kits
- What: `/anima kits clonekit [src] [dst]`

**`anima.kits.lore.add`**
- Add lore to kits
- What: `/anima kits lore add [kit] [text]`

**`anima.kits.lore.edit`**
- Edit kit lore
- What: `/anima kits lore edit [kit] [line] [text]`

**`anima.kits.lore.remove`**
- Remove kit lore
- What: `/anima kits lore remove [kit] [line]`

**`anima.kits.give`**
- Give kits to players
- What: `/anima kits give [player] [kit]`

**`anima.kits.giveall`**
- Give kits to all online players
- What: `/anima kits giveall [kit]`

**`anima.kits.setcooldown`**
- Set kit cooldowns
- What: `/anima kits setcooldown [kit] [time]`

**`anima.kits.singleclaim`**
- Toggle single-claim restrictions
- What: `/anima kits singleclaim [kit] [true|false]`

**`anima.kits.onjoinnew`**
- Set starter kits for new players
- What: `/anima kits onjoinnew [kit]`

**`anima.kits.permission.add`**
- Grant permissions to players
- What: `/anima kits permission add [player] [perm] [duration]`

**`anima.kits.permission.remove`**
- Revoke player permissions
- What: `/anima kits permission remove [player] [perm]`

**`anima.kits.permission.show`**
- View player permissions
- What: `/anima kits permission show [player]`

**`anima.kits.permission.claim`**
- Grant kit access to players
- What: `/anima kits permission claim [player] [kit] [true|false]`

**`anima.kits.permission.claimfree`**
- Bypass cooldowns for players
- What: `/anima kits permission claimfree [player] [kit] [true|false]`

**`anima.kits.permission.*`**
- All permission management commands
- What: Grants all permission.* commands above

**`anima.kits.reload`**
- Reload configuration
- What: `/anima kits reload`

---

## ⭐ RANKS PERMISSIONS

### Master Permissions

**`anima.ranks.*`**
- Grants access to ALL rank commands

**`anima.ranks.admin`**
- Admin alias for all rank management
- What: Alternative to ranks.*

### Player Permissions (Default: true)

**`anima.ranks.list`**
- View rank list and details
- What: `/anima ranks list`

**`anima.ranks.rankup`**
- Allow player to rank up
- What: `/anima ranks rankup`

**`anima.ranks.gui`**
- Open ranks GUI
- What: `/anima ranks`

### Admin Permissions

**`anima.ranks.add`**
- Create new ranks
- What: `/anima ranks add [name]`

**`anima.ranks.delete`**
- Delete ranks
- What: `/anima ranks delete [name]`

**`anima.ranks.set`**
- Set rank properties
- What: `/anima ranks set [rank] [property] [value]`

**`anima.ranks.assign`**
- Assign/remove ranks from players
- What: `/anima ranks assign [player] [rank]`

**`anima.ranks.promote`**
- Promote/demote players
- What: `/anima ranks promote/demote [player]`

**`anima.ranks.permissions`**
- Manage rank permissions
- What: `/anima ranks permissions [rank] [add|remove] [perm]`

---

## ✏️ ITEM EDITOR PERMISSIONS

### Master Permissions

**`anima.itemedit.*`**
- All item editor commands (admin)

**`anima.itemedit.admin`**
- Admin alias - grants all permissions
- Bypasses all cooldowns

### Admin-Only Permissions

**`anima.itemedit.enchant`**
- Add/edit/remove enchantments
- Allows unsafe/overpowered levels
- What: `/anima itemedit enchant [ench] [level]`

**`anima.itemedit.glow`**
- Apply or remove glow effect
- What: `/anima itemedit glow [on|off]`

**`anima.itemedit.unbreakable`**
- Toggle unbreakable flag
- What: `/anima itemedit unbreakable [on|off]`

**`anima.itemedit.lore`**
- Add/edit/remove/clear lore lines
- What: `/anima itemedit lore [add|edit|remove|clear]`

### Player Permissions (Default: false)

**`anima.itemedit.rename`**
- Rename held item
- What: `/anima itemedit rename [name]`

**`anima.itemedit.prefix`**
- Add/remove prefix on item
- What: `/anima itemedit prefix [text|remove]`

**`anima.itemedit.suffix`**
- Add/remove suffix on item
- What: `/anima itemedit suffix [text|remove]`

**`anima.itemedit.repair`**
- Repair held item via command
- What: `/anima itemedit repair`
- Subject to cooldown (300s default)

**`anima.itemedit.repair.cooldown`**
- Subject to repair cooldown
- Indicates player has rate limiting
- Admins with `itemedit.admin` bypass this

---

## 📊 Permission Summary

| System | Master | Player | Admin | Total |
|--------|--------|--------|-------|-------|
| Kits | 1 | 5 | 15 | 21 |
| Ranks | 2 | 3 | 6 | 11 |
| Item Editor | 2 | 5 | 4 | 11 |
| **TOTAL** | **5** | **13** | **25** | **43** |

---

## 🔧 Setting Up Permissions

### Using LuckPerms

```bash
/luckperms user [player] permission set anima.kits.use true
/luckperms user [player] permission set anima.ranks.rankup true
/luckperms group [group] permission set anima.kits.* true
```

### Using PermissionsEx

```bash
pex user [player] add anima.kits.use
pex user [player] add anima.ranks.rankup
pex group [group] add anima.kits.*
```

### Using Bukkit/YAML

```yaml
permissions:
  anima.kits.*:
    description: All kit commands
    children:
      anima.kits.use: true
      anima.kits.claim: true
```

---

## 💡 Permission Strategy Tips

### For New Players
```
anima.kits.use: true
anima.kits.list: true
anima.kits.claim: true
anima.ranks.list: true
anima.ranks.rankup: true
```

### For VIP Players
```
anima.kits.*: true
anima.ranks.gui: true
anima.itemedit.rename: true
anima.itemedit.prefix: true
```

### For Moderators
```
anima.kits.*: true
anima.ranks.*: true
anima.itemedit.rename: true
anima.itemedit.enchant: true
```

### For Admins
```
anima.kits.*: true
anima.ranks.*: true
anima.itemedit.*: true
```

---

## 🔍 Checking Permissions

### View Player Permissions
```bash
/anima kits permission show [player]
```

### View All Permissions
Players with `anima.*` can use all commands.

### Default Permissions
- Player permissions default to `true`
- Admin permissions default to OP only
- Item editing defaults to `false`

---

## 📖 Related Documentation

- **[Quick Start](README-QUICKSTART.md)** - Permission examples
- **[Commands](README-COMMANDS.md)** - Which permissions for each command
- **[Configuration](README-CONFIGURATION.md)** - Config file options
- **[Usage Guide](README-GUIDE.md)** - Best practices

---

**⚡ ANIMA - Complete Permission Documentation**

© 2024 World Of Normies
