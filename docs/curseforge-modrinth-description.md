# Enchant Transfer

<div align="center">

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/transfer_table_logo.png" alt="Transfer Table" width="128" style="image-rendering: pixelated;">

<img src="https://img.shields.io/badge/version-3.1.1-d2962a" alt="Version">
<img src="https://img.shields.io/badge/minecraft-26.2_%7C_1.21.11-62b47a?logo=minecraft&logoColor=white" alt="Minecraft">
<img src="https://img.shields.io/badge/fabric_loader-%E2%89%A50.19.3_%7C_%E2%89%A50.19.2-dbd0b4" alt="Fabric">
<img src="https://img.shields.io/badge/java-25_%7C_21-ED8B00?logo=openjdk&logoColor=white" alt="Java">
<img src="https://img.shields.io/badge/license-CC0--1.0-lightgrey" alt="License">
<a href="https://www.curseforge.com/minecraft/mc-mods/enchant-transfer"><img src="https://img.shields.io/curseforge/dt/1540807?logo=curseforge&label=curseforge&color=F16436" alt="CurseForge"></a>
<a href="https://modrinth.com/mod/enchant-transfer"><img src="https://img.shields.io/modrinth/dt/enchant-transfer?logo=modrinth&label=modrinth&color=00AF5C" alt="Modrinth"></a>

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
| **3.1.x** | 26.2 | Active development |
| **2.0.x** | 1.21.11 | Maintenance (critical bug fixes only) |

**Requires:** Fabric Loader and Fabric API

**Languages:** English · Español · Português (Brasil)\* · Русский\* · 简体中文\*

> \* Pending review by a native speaker. Do you speak one of them? Help us review the translations or add your own language: open an [issue](https://github.com/arensis/enchant-transfer-mod/issues) or a pull request editing the [language files](https://github.com/arensis/enchant-transfer-mod/tree/master/src/main/resources/assets/enchanttransfer/lang).

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
| 🟢 Green | Nature | Respiration, Aqua Affinity, Depth Strider, Silk Touch, Fortune, Luck of the Sea, Lure |
| 🔴 Red | Combat | Sharpness, Smite, Bane of Arthropods, Impaling, Fire Aspect, Looting, Thorns, Sweeping Edge, Knockback, Density, Breach, Lunge |
| 🟡 Yellow | Utility | Efficiency, Unbreaking, Infinity, Power, Punch, Flame, Multishot, Piercing, Quick Charge |
| 🟣 Purple | Arcane | Channeling, Riptide, Loyalty, Frost Walker, Wind Burst, Mending, Swift Sneak, Soul Speed |
| ⚫ Black | Curse | Curse of Vanishing, Curse of Binding |

---

## Card Decks

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_blue.png" width="37" alt="Protection Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_green.png" width="37" alt="Nature Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_red.png" width="37" alt="Combat Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_yellow.png" width="37" alt="Utility Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_purple.png" width="37" alt="Arcane Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_rare.png" width="37" alt="Rarity Deck">

A **Card Deck** is a brass-bound case that stores Magic Cards of one type. Place decks in a **Deck Lectern** within an Enchanting Table's normal bookshelf range (same positions and air gap as regular bookshelves) and the table's rolls shift according to what you've stored.

| Deck | Stores | Effect on nearby Enchanting Tables |
|------|--------|-----------------------------------|
| Protection / Nature / Combat / Utility / Arcane | Cards of that color | Favors that category, and even more the exact enchantments stored |
| Rarity (wildcard) | Curse cards | Favors rare (low-weight) enchantments |

### Crafting Recipe

|   |   |   |
|:-:|:-:|:-:|
|   | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |   |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/leather.png" width="32" alt="Leather"><br>Leather | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_red.png" width="32" alt="Card of the deck's type"><br>Card of the deck's type | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/leather.png" width="32" alt="Leather"><br>Leather |
|   | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |   |

→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_red.png" width="42" alt="Card Deck"> **Card Deck** × 1 — the card is consumed and sets the deck's type (Rarity: a Curse card)

### How it works

- **Storing**: right-click a card onto the deck (or the deck onto a card), like a bundle. Right-click with an empty cursor to take the last stack back out.
- **Points**: a level N card is worth 2^(N-1) points (same ratio as combining). Each deck holds up to 64 points.
- **Enchanting power**: every 3 decks holding cards count as one bookshelf (a full Deck Lectern = 1 bookshelf). They add up with regular bookshelves, and the vanilla cap of 15 still applies — so higher enchantment levels come from power, just like with bookshelves.
- **Balance**: the bias is relative — a library with every category in equal parts barely changes anything; a specialized one makes its enchantments up to ×3 more likely.
- **Location affinity**: some decks resonate with where the table is — Combat ×2 in the Nether (Fire Aspect ×3), Fire/Blast Protection and Flame ×1.5 in the Nether, Rarity ×1.5 in the Nether, Arcane ×2 in the End, Nature's water enchantments ×1.5 in oceans and rivers and Fortune/Silk Touch ×1.5 in forests, jungles and taigas. It scales the deck's extra effect (capped at ×3). Rules are datapack JSON in `data/<namespace>/enchanttransfer/library_affinity/`, so modpacks can add their own dimensions and biomes (`deck`, optional `dimension`, `biome`, `enchantments`, and `multiplier`); they reload with `/reload`.
- **Wear**: each enchantment helped by the library has a 20% chance to consume one card (the cheapest) from a matching deck.

### Deck Lectern

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_deck_lectern.png" width="114" alt="Deck Lectern">

The block that holds your decks: a low brass base with a tilted velvet tray for **3 decks**, under a glass bell.

- **Right-click** opens a small screen with 3 deck slots.
- **Lamps**: one per slot — dark brown (no deck), light (deck without cards), emerald (deck with cards).
- **Off by default.** Shift + right-click switches it on (it needs at least one deck with cards) or off, so you choose which lecterns count; the amethyst on top of the bell glows while it's on. It switches itself off when its decks run out of cards, but never on.
- While active, runes fly from the lectern to the enchanting table, like with bookshelves.
- Place lecterns where regular bookshelves would go around an Enchanting Table (same positions and air gap). One ring of 16 at table height already reaches the cap of 15, so they don't need to be stacked.

|   |   |   |
|:-:|:-:|:-:|
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_block.png" width="32" alt="Glass"><br>Glass | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/amethyst_shard.png" width="32" alt="Amethyst Shard"><br>Amethyst Shard | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_block.png" width="32" alt="Glass"><br>Glass |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone"><br>Redstone | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/oak_planks.png" width="32" alt="Planks"><br>Planks | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/oak_planks.png" width="32" alt="Planks"><br>Planks | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/oak_planks.png" width="32" alt="Planks"><br>Planks |

→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_deck_lectern.png" width="61" alt="Deck Lectern"> **Deck Lectern** × 1

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
|:-:|:-:|:-:|
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/amethyst_shard.png" width="32" alt="Amethyst Shard"><br>Amethyst Shard | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_block.png" width="32" alt="Glass Block"><br>Glass Block | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |

→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_infusion_coil.png" width="46" alt="Infusion Coil"> **Infusion Coil** × 1

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
|:-:|:-:|:-:|
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/iron_ingot.png" width="32" alt="Iron Ingot"><br>Iron Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/iron_ingot.png" width="32" alt="Iron Ingot"><br>Iron Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/iron_ingot.png" width="32" alt="Iron Ingot"><br>Iron Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_pane.png" width="32" alt="Glass Pane"><br>Glass Pane | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/blast_furnace.png" width="32" alt="Blast Furnace"><br>Blast Furnace | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |

→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_zinc_smelter.png" width="59" alt="Zinc Smelter"> **Zinc Smelter** × 1

### Fuel

The Zinc Smelter only accepts **Lava Buckets** and **Blaze Rods** as fuel — no coal or wood. Lava Buckets return an empty bucket after consumption.

### GUI

<img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_zinc_smelter.jpg" width="200" alt="Zinc Smelter GUI">

*Steampunk-themed interface with copper input slots, a lava-bordered fuel slot, and brass output slot.*

---

## Transfer Table

### Crafting Recipe

|   |   |   |
|:-:|:-:|:-:|
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gold_ingot.png" width="32" alt="Gold Ingot"><br>Gold Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gold_ingot.png" width="32" alt="Gold Ingot"><br>Gold Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gold_ingot.png" width="32" alt="Gold Ingot"><br>Gold Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone Dust"><br>Redstone Dust | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone Dust"><br>Redstone Dust | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone Dust"><br>Redstone Dust |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/coal.png" width="32" alt="Coal / Charcoal"><br>Coal / Charcoal | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/diamond.png" width="32" alt="Diamond"><br>Diamond | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/coal.png" width="32" alt="Coal / Charcoal"><br>Coal / Charcoal |

→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_transfer_table.png" width="61" alt="Transfer Table"> **Transfer Table** × 1

Requires an **iron pickaxe** or better to mine (the same applies to the Infusion Coil and the Zinc Smelter).

The 3D model reflects its recipe — gold on the outer edge, redstone in the middle ring, carbon-black inner layer, and a blue diamond core that glows from every angle.

---

## Compatibility

> **Worlds from 3.0.x are fully compatible:** nothing in them changes, and an Enchanting Table without Deck Lecterns nearby works exactly like vanilla. Cards extracted before 3.1 keep their color (e.g. yellow Mending, green Soul Speed, or blank Knockback cards) but still go into the deck of their current category — decks check the enchantment, not the card color.

> **Worlds from 1.0.x are fully compatible.** Existing Transfer Tables keep working — the block ID hasn't changed. Old Magic Cards stored in chests retain their enchantment data. They will appear as the generic (blank) card variant; their functionality is unchanged. New cards extracted after updating will automatically use the color-coded system.

---

## Changelog — 3.1.1

- **Translatable tooltips** — Module tooltips in the Selector, Transfer Table and Infusion Coil screens (empty sockets, directions, XP level, Idle/Infusing) are no longer hardcoded in English
- **New languages** — Simplified Chinese (zh_cn), Brazilian Portuguese (pt_br) and Russian (ru_ru)

### 3.1.0

- **Card Decks** — New brass-bound decks that store Magic Cards of one type: Protection, Nature, Combat, Utility, Arcane, and a Rarity wildcard filled with Curse cards. Crafted with 2 Brass Ingots, 2 Leather and 1 card of the deck's type (the card is consumed). Store and take cards with right-click, like a bundle
- **Deck Lectern** — New block that holds 3 decks on a tilted tray under a glass bell. A lamp per slot shows whether each deck has cards, and Shift + right-click toggles it (the amethyst on top glows while active). Every 3 decks holding cards count as one bookshelf of enchanting power, adding up with regular bookshelves (vanilla cap of 15 still applies)
- **Steered enchanting** — Decks make the table favor their category, and even more the exact enchantments stored (up to ×3 with a specialized library). The Rarity wildcard favors rare, low-weight enchantments
- **Location affinity** — Decks resonate with where the table is: Combat ×2 in the Nether (Fire Aspect ×3), fire protections and Flame ×1.5 in the Nether, Arcane ×2 in the End, Nature's water enchantments in oceans and rivers, Fortune and Silk Touch in forests, jungles and taigas
- **Datapack-driven affinities** — Affinity rules are JSON files in `data/<namespace>/enchanttransfer/library_affinity/`, so modpacks can add rules for any dimension or biome; they reload with `/reload`
- **Deck wear** — Each enchantment the library helps with has a 20% chance to consume the cheapest matching card
- **26.2 enchantments categorized** — Knockback, Density, Breach, Lunge, Flame, Multishot, Piercing, Quick Charge, Luck of the Sea, Lure and Wind Burst now have card colors
- **Arcane = rare magic** — All treasure enchantments (Mending, Swift Sneak, Soul Speed, Frost Walker, Wind Burst) are now Arcane cards. Cards already extracted keep their color and still go into the matching deck
- **Lighter website** — 3D block previews are now static images, much faster on phones

**Older versions:** see the [full changelog](https://arensis.github.io/enchant-transfer-mod/#changelog).

---

## Installation

### Minecraft 26.2 — mod v3.1.x

1. Make sure Minecraft runs with **Java 25**
2. Install [Fabric Loader](https://fabricmc.net/use/) **≥ 0.19.3** for Minecraft 26.2
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and place it in the `mods/` folder
4. Download the Enchant Transfer **3.1.x** JAR and place it in `mods/` as well
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

| Dependency | Minecraft 26.2 (v3.1.x) | Minecraft 1.21.11 (v2.0.x) |
|------------|-------------------------|----------------------------|
| Java | 25 | 21 |
| Fabric Loader | ≥ 0.19.3 | ≥ 0.19.2 |
| Fabric API | 0.158.0+26.2 | 0.141.4+1.21.11 |

---

## License

This project is distributed under the **CC0-1.0** license. You may use, modify, and distribute the code without restrictions.
