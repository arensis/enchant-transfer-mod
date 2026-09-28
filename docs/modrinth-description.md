# Enchant Transfer

<div align="center">

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/transfer_table_logo.png" alt="Transfer Table" width="128" style="image-rendering: pixelated;">

![Version](https://img.shields.io/badge/version-3.0.0-d2962a)
![Minecraft](https://img.shields.io/badge/minecraft-26.2_%7C_1.21.11-62b47a?logo=minecraft&logoColor=white)
![Fabric](https://img.shields.io/badge/fabric_loader-≥0.19.3_%7C_≥0.19.2-dbd0b4)
![Java](https://img.shields.io/badge/java-25_%7C_21-ED8B00?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-CC0--1.0-lightgrey)
[![CurseForge](https://img.shields.io/curseforge/dt/1540807?logo=curseforge&label=curseforge&color=F16436)](https://www.curseforge.com/minecraft/mc-mods/enchant-transfer)
[![Modrinth](https://img.shields.io/modrinth/dt/enchant-transfer?logo=modrinth&label=modrinth&color=00AF5C)](https://modrinth.com/mod/enchant-transfer)

**Extract, transfer, and combine enchantments through Magic Cards — now with colored cards, attachable modules, and an XP conversion system.**

[Website](https://arensis.github.io/enchant-transfer-mod/) · [CurseForge](https://www.curseforge.com/minecraft/mc-mods/enchant-transfer) · [Modrinth](https://modrinth.com/mod/enchant-transfer) · [Issues](https://github.com/arensis/enchant-transfer-mod/issues)

</div>

---

**Enchant Transfer** adds a craftable block — the **Transfer Table** — that lets you extract enchantments from any item into individual **Magic Cards**, apply those cards to other compatible items, and combine two cards of the same enchantment and level to upgrade them. All operations are free (no XP cost), and vanilla enchantment compatibility rules are respected.

Version 2.0 introduced the **Module System**: specialized blocks that attach to the Transfer Table on any face, extending its capabilities. The first module — the **Infusion Coil** — converts Magic Cards into stored XP and fills glass bottles into Bottles o' Enchanting.

> **Version support:** New features and modules are developed for Minecraft 26.x only.
> The 1.21.11 version (v2.0.x) is in maintenance mode and will only receive critical bug fixes.

| Mod version | Minecraft | Status |
|-------------|-----------|--------|
| **3.0.x** | 26.2 | Active development |
| **2.0.x** | 1.21.11 | Maintenance (critical bug fixes only) |

**Requires:** Fabric Loader and Fabric API

---

## Core Features

### Extract

Place any enchanted item or book in the Transfer Table to strip its enchantments into individual Magic Cards, each one color-coded by category.

### Apply

Place a Magic Card and a target item in the Transfer Table. The enchantment transfers instantly, respecting vanilla incompatibility rules.

### Combine

Two cards of the same enchantment and level produce one card of the next level (up to vanilla maximum). Stacks work too — 5 + 6 cards yields 5 upgraded and 1 remainder.

### Zero Cost

All Transfer Table operations — extraction, application, and combination — are completely free. No XP, no materials consumed.

---

## Typed Magic Cards

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_blue.png" width="48" style="image-rendering: pixelated;" alt="Blue Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_green.png" width="48" style="image-rendering: pixelated;" alt="Green Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_red.png" width="48" style="image-rendering: pixelated;" alt="Red Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_yellow.png" width="48" style="image-rendering: pixelated;" alt="Yellow Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_purple.png" width="48" style="image-rendering: pixelated;" alt="Purple Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_black.png" width="48" style="image-rendering: pixelated;" alt="Black Card">

Magic Cards are now color-coded by enchantment category. When you extract an enchantment, the Transfer Table automatically produces a card of the matching color.

| Card | Category | Enchantments |
|------|----------|-------------|
| 🔵 Blue | Protection | Protection, Fire Protection, Blast Protection, Projectile Protection, Feather Falling |
| 🟢 Green | Nature | Respiration, Aqua Affinity, Depth Strider, Silk Touch, Fortune |
| 🔴 Red | Combat | Sharpness, Smite, Bane of Arthropods, Impaling, Fire Aspect, Looting, Thorns, Sweeping Edge |
| 🟡 Yellow | Utility | Efficiency, Unbreaking, Mending, Swift Sneak, Infinity, Power, Punch |
| 🟣 Purple | Arcane | Channeling, Riptide, Loyalty, Frost Walker |
| ⚫ Black | Curse | Curse of Vanishing, Curse of Binding |

---

## Module System

The Transfer Table now works as a **hub**. Specialized module blocks can be placed on any of its 6 faces. Right-click the Transfer Table to open the **Selector Screen** — a visual hub showing all connected modules with their real-time status.

- Up to **6 modules** per Transfer Table (one per face)
- Each module type adds different functionality
- Navigate between the hub and any module using the **dot navigation bar**
- Modules show live status in the Selector — processing progress, tank levels, and connection state

### GUI Screens

<p>
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_selector.png" width="200" alt="Selector Screen">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_core.png" width="200" alt="Core Screen">
<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_infusor.jpg" width="200" alt="Infusor Screen">
</p>

*From left to right: Selector (hub with module sockets), Transfer Table Core (enchantment operations), Infusion Coil (XP conversion). Dynamic elements like wires, tank fills, and module icons are drawn by code on top of these backgrounds.*

---

## Infusion Coil

The first module for the Transfer Table. A vertical flask made of copper, brass, and glass that converts Magic Cards into XP.

### How it works

1. **Insert** a Magic Card into the card slot — the coil processes it over ~10 seconds
2. **XP is stored** in an internal tank (1,000 XP capacity)
3. **Place glass bottles** in the bottle slot to extract XP as Bottles o' Enchanting (7 XP per bottle)

Unenchanted cards yield a minimum of 5 XP; enchanted cards yield more based on their enchantment.

> **Tip:** The Infusion Coil stays vertical regardless of which face it's attached to. The copper connector stub always points toward the Transfer Table.

### Crafting Recipe

|   |   |   |
|---|---|---|
| Brass Ingot | Amethyst Shard | Brass Ingot |
| Copper Ingot | Glass Block | Copper Ingot |
| Copper Ingot | Copper Ingot | Copper Ingot |

→ **Infusion Coil** × 1

---

## Zinc Smelter

A standalone steampunk furnace for zinc processing and brass alloy creation. An ornate kiln made of copper, brass, and iron with a glass viewport and chimney.

### New Materials

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/zinc_oxide.png" width="48" style="image-rendering: pixelated;" alt="Zinc Oxide"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/zinc_sheet.png" width="48" style="image-rendering: pixelated;" alt="Zinc Sheet"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="48" style="image-rendering: pixelated;" alt="Brass Ingot">

### Processing Chain

Three smelting recipes form a material chain that produces **Brass Ingots** — required to craft the Infusion Coil.

| Step | Input(s) | Output |
|------|----------|--------|
| 1. Calcination | Calcite | Zinc Oxide ×2 |
| 2. Reduction | Zinc Oxide + Coal | Zinc Sheet |
| 3. Alloying | Copper Ingot + Zinc Sheet | Brass Ingot |

### Crafting Recipe

|   |   |   |
|---|---|---|
| Iron Ingot | Iron Ingot | Iron Ingot |
| Copper Ingot | Glass Pane | Copper Ingot |
| Copper Ingot | Blast Furnace | Copper Ingot |

→ **Zinc Smelter** × 1

### Fuel

The Zinc Smelter only accepts **Lava Buckets** and **Blaze Rods** as fuel — no coal or wood. Lava Buckets return an empty bucket after consumption.

### GUI

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_zinc_smelter.jpg" width="200" alt="Zinc Smelter GUI">

*Steampunk-themed interface with copper input slots, a lava-bordered fuel slot, and brass output slot.*

---

## Transfer Table

### Crafting Recipe

|   |   |   |
|---|---|---|
| Gold Ingot | Gold Ingot | Gold Ingot |
| Redstone Dust | Redstone Dust | Redstone Dust |
| Coal / Charcoal | Diamond | Coal / Charcoal |

→ **Transfer Table** × 1

Requires an **iron pickaxe** or better to mine (the same applies to the Infusion Coil and the Zinc Smelter).

The 3D model reflects its recipe — gold on the outer edge, redstone in the middle ring, carbon-black inner layer, and a blue diamond core that glows from every angle.

---

## Compatibility

> **Worlds from 1.0.x are fully compatible.** Existing Transfer Tables keep working — the block ID hasn't changed. Old Magic Cards stored in chests retain their enchantment data. They will appear as the generic (blank) card variant; their functionality is unchanged. New cards extracted after updating will automatically use the color-coded system.

---

## Changelog — 3.0.0

- **Port to Minecraft 26.2** — Built against Fabric Loader 0.19.3 and Fabric API 0.158.0+26.2
- **Java 25 required** — Minecraft 26.2 needs Java 25 (the 1.21.11 version keeps using Java 21)
- **Official Mojang mappings** — Codebase migrated from Yarn to Mojang mappings (Fabric Loom 1.17)
- **New render pipeline** — Block entity renderers (Transfer Table, Infusion Coil, Zinc Smelter) and all GUI screens migrated to the 26.2 deferred rendering system
- **Translucent blocks** — Infusion Coil and Zinc Smelter now declare their translucent render type in their block models
- **Fabric API dependency** — Declared with the new `fabric-api` mod id (26.2 no longer provides the legacy `fabric` id)
- **Creative tab** — The mod's creative tab is now populated correctly on 26.2
- **No gameplay changes** — Same blocks, items, recipes, and mechanics as 2.0.2

<details>
<summary>Previous versions</summary>

### 2.0.2

- Zinc Smelter can now be mined with a pickaxe; hardness adjusted

### 2.0.1

- Added recipe book advancements for all crafting recipes

### 2.0.0

- **Module System** — The Transfer Table is now a modular hub that accepts specialized blocks on any of its 6 faces
- **Infusion Coil** — First module: converts Magic Cards into XP, fills glass bottles into Bottles o' Enchanting
- **Zinc Smelter** — Steampunk furnace for zinc processing: Calcite → Zinc Oxide → Zinc Sheet → Brass Ingot
- **New Materials** — Zinc Oxide, Zinc Sheet, and Brass Ingot with custom textures and item models
- **Typed Magic Cards** — 6 color-coded card variants by enchantment category (Protection, Nature, Combat, Utility, Arcane, Curse)
- **Selector Screen** — New hub GUI with live module status, wires, and processing indicators
- **Redesigned GUIs** — Dark starry aesthetic, golden/cyan accents, navigation dot bar, ghost input hints
- **New 3D models** — Custom Transfer Table model with stepped funnel and emissive diamond core; Infusion Coil flask with copper/brass/glass and fluid renderer
- **Block Entity Renderers** — Pulsing blue core for the Transfer Table; fluid fill animation, cap glow, and directional connector for the Infusion Coil

### 1.0.3

- Fixed block drop loot table, item translation key, and lang cleanup
- Updated Gradle wrapper to 9.5.0

### 1.0.2

- Migrated data pack paths to 1.21.4+ singular format (recipe/, loot_table/, tags/)
- Broadened minecraft version constraint to >=1.21.1

### 1.0.1

- Port to Minecraft 1.21.11
- Updated Fabric Loom to 1.14, Gradle to 9.2.0, Fabric API to 0.141.4+1.21.11
- Fixed all 1.21.4+ API changes (RegistryKey, DrawContext, ActionResult, etc.)

### 1.0.0

- Migrated to Minecraft 1.21.1 with CI/CD and release automation

</details>

---

## Installation

### Minecraft 26.2 — mod v3.0.x

1. Make sure Minecraft runs with **Java 25**
2. Install [Fabric Loader](https://fabricmc.net/use/) **≥ 0.19.3** for Minecraft 26.2
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and place it in the `mods/` folder
4. Download the Enchant Transfer **3.0.x** JAR and place it in `mods/` as well
5. Launch Minecraft with the Fabric profile

### Minecraft 1.21.11 — mod v2.0.x (maintenance)

1. Make sure Minecraft runs with **Java 21**
2. Install [Fabric Loader](https://fabricmc.net/use/) **≥ 0.19.2** for Minecraft 1.21.11
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 and place it in the `mods/` folder
4. Download the Enchant Transfer **2.0.x** JAR and place it in `mods/` as well
5. Launch Minecraft with the Fabric profile

> **Fabric API is required.** The mod will not start without it. Make sure the mod JAR, Fabric API, and Fabric Loader all match your Minecraft version.

---

## Requirements

| Dependency | Minecraft 26.2 (v3.0.x) | Minecraft 1.21.11 (v2.0.x) |
|------------|-------------------------|----------------------------|
| Java | 25 | 21 |
| Fabric Loader | ≥ 0.19.3 | ≥ 0.19.2 |
| Fabric API | 0.158.0+26.2 | 0.141.4+1.21.11 |

---

## License

This project is distributed under the **CC0-1.0** license. You may use, modify, and distribute the code without restrictions.
