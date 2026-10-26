# Enchant Transfer

<div align="center">

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/transfer_table_logo.png" alt="Transfer Table" width="128" style="image-rendering: pixelated;"></p>

<p><img src="https://img.shields.io/badge/version-3.2.0-d2962a" alt="Version"> <img src="https://img.shields.io/badge/minecraft-26.2%20%7C%201.21.11-62b47a?logo=minecraft&logoColor=white" alt="Minecraft"> <img src="https://img.shields.io/badge/fabric%20loader-0.19.3%20%7C%200.19.2-dbd0b4" alt="Fabric"> <img src="https://img.shields.io/badge/java-25%20%7C%2021-ED8B00?logo=openjdk&logoColor=white" alt="Java"> <img src="https://img.shields.io/badge/license-CC0--1.0-lightgrey" alt="License"> <a href="https://www.curseforge.com/minecraft/mc-mods/enchant-transfer"><img src="https://img.shields.io/curseforge/dt/1540807?logo=curseforge&label=curseforge&color=F16436" alt="CurseForge"></a> <a href="https://modrinth.com/mod/enchant-transfer"><img src="https://img.shields.io/modrinth/dt/enchant-transfer?logo=modrinth&label=modrinth&color=00AF5C" alt="Modrinth"></a></p>

<p><strong>Extract, transfer, and combine enchantments through Magic Cards — now with colored cards, attachable modules, and an XP conversion system.</strong></p>

<p><a href="https://arensis.github.io/enchant-transfer-mod/">Website</a> · <a href="https://www.curseforge.com/minecraft/mc-mods/enchant-transfer">CurseForge</a> · <a href="https://modrinth.com/mod/enchant-transfer">Modrinth</a> · <a href="https://github.com/arensis/enchant-transfer-mod/issues">Issues</a></p>

</div>

---

**Enchant Transfer** adds a craftable block — the **Transfer Table** — that lets you extract enchantments from any item into individual **Magic Cards**, apply those cards to other compatible items, and combine two cards of the same enchantment and level to upgrade them. All operations are free (no XP cost), and vanilla enchantment compatibility rules are respected.

Version 2.0 introduced the **Module System**: specialized blocks that attach to the Transfer Table on any face, extending its capabilities. The first module — the **Infusion Coil** — converts Magic Cards into stored XP and fills glass bottles into Bottles o' Enchanting.

> **Version support:** New features and modules are developed for Minecraft 26.x only.
> The 1.21.11 version (v2.0.x) is in maintenance mode and will only receive critical bug fixes.

| Mod version | Minecraft | Status |
|-------------|-----------|--------|
| **3.2.x** | 26.2 | Active development |
| **2.0.x** | 1.21.11 | Maintenance (critical bug fixes only) |

**Requires:** Fabric Loader and Fabric API

**Languages:** English · Español · Português (Brasil)\* · Русский\* · 简体中文\* · Français\* · Deutsch\* · 日本語\* — **New:** Français, Deutsch and 日本語

> \* Pending review by a native speaker (Portuguese, Russian, Chinese, French, German and Japanese). Do you speak one of them? Help us review the translations or add your own language: open an [issue](https://github.com/arensis/enchant-transfer-mod/issues) or a pull request editing the [language files](https://github.com/arensis/enchant-transfer-mod/tree/master/src/main/resources/assets/enchanttransfer/lang).

---

## Core Features

### Extract

Place any enchanted item or book in the Transfer Table to strip its enchantments into individual Magic Cards, each one color-coded by category.

### Apply

Place a Magic Card and a target item in the Transfer Table. The enchantment transfers instantly, respecting vanilla incompatibility rules.

**Shift + click** a card in your inventory and it goes straight onto the item on the table, if the item accepts it and it doesn't clash with its enchantments. Otherwise it goes to the combine slots, the stack split evenly between both (the odd card stays in your inventory) — with an item on the table, only when the cards make a pair that can level up.

### Combine

Two cards of the same enchantment and level produce one card of the next level (up to vanilla maximum). Stacks work too — 5 + 6 cards yields 5 upgraded and 1 remainder.

### Zero Cost

All Transfer Table operations — extraction, application, and combination — are completely free. No XP, no materials consumed.

---

## Typed Magic Cards

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_blue.png" width="48" style="image-rendering: pixelated;" alt="Blue Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_green.png" width="48" style="image-rendering: pixelated;" alt="Green Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_red.png" width="48" style="image-rendering: pixelated;" alt="Red Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_yellow.png" width="48" style="image-rendering: pixelated;" alt="Yellow Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_purple.png" width="48" style="image-rendering: pixelated;" alt="Purple Card"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_black.png" width="48" style="image-rendering: pixelated;" alt="Black Card"></p>

Magic Cards are now color-coded by enchantment category. When you extract an enchantment, the Transfer Table automatically produces a card of the matching color.

| Card | Category | Enchantments |
|------|----------|-------------|
| 🔵 Blue | Protection | Protection, Fire Protection, Blast Protection, Projectile Protection, Feather Falling |
| 🟢 Green | Nature | Respiration, Aqua Affinity, Depth Strider, Silk Touch, Fortune, Luck of the Sea, Lure, Harvesting\* |
| 🔴 Red | Combat | Sharpness, Smite, Bane of Arthropods, Impaling, Looting, Thorns, Sweeping Edge, Knockback, Density, Breach, Lunge, Power, Punch, Multishot, Piercing |
| 🟡 Yellow | Utility | Efficiency, Unbreaking, Quick Charge, Swift Sneak, Magnetism\* |
| 🟣 Purple | Arcane | Channeling, Riptide, Loyalty, Frost Walker, Wind Burst, Fire Aspect, Flame, Infinity, Mending, Soul Speed |
| ⚫ Black | Curse | Curse of Vanishing, Curse of Binding |

\* Added by this mod, see [Enchantments](#enchantments-).

---

## Enchantments 🆕

Two new enchantments, from the Enchanting Table, villagers and loot — and, like any other, extractable into Magic Cards.

### Magnetism

**Utility · I–V · metal armor** (chainmail, iron, copper, gold, netherite). Worn, it pulls nearby items and XP orbs toward you: 4 / 6 / 8 / 10 / 12 blocks (the highest level you wear counts). Sneak to switch it off; items you throw yourself are left alone.

### Harvesting

**Nature · I–IV · hoes and axes.** It only works when you carry what it replants — the crop's seed or the tree's sapling — and spends one per plant.

| Level | Hoe — right-click a ripe crop | Axe — break a log of a natural tree |
|-------|-------------------------------|-------------------------------------|
| I | Harvests it and replants it | Replants the sapling where the base log was |
| II | Also the next two crops ahead (a row of 3) | Also fells the logs straight above |
| III | The whole connected field, for **0.5 XP levels per started 9×9** | The whole tree, branches included, for **0.5 XP levels** |
| IV | Grows every replanted crop with bone meal from your inventory (doubles the field's XP cost) | Grows the replanted sapling into a tree with bone meal from your inventory |

- **Wear**: 1 durability per crop or log, like tilling or chopping. **Sneak** to harvest a single crop or chop a single log.
- **XP cost**: 0.5 levels by default, set per world with the game rules `enchanttransfer:harvesting_field_xp_cost` and `enchanttransfer:harvesting_tree_xp_cost` (0 makes it free).
- **Not enough XP**: the field is cut to the 9×9s you can pay for (or a row if not even one), and a tree falls like level II.
- **Safe for builds**: it only fells trees whose logs touch leaves that grew there, not leaves placed by a player (up to 256 logs).
- **Crops and trees from other mods**: any crop built on vanilla's crop block works out of the box; others can be added to the `#enchanttransfer:harvesting_replantable` block tag (or left out with `harvesting_excluded`). Trees whose logs and leaves are in `#minecraft:logs` and `#minecraft:leaves` are felled, and the sapling comes from datapack rules in `data/<namespace>/enchanttransfer/harvesting_sapling/` — `{"logs": "mymod:maple_log", "sapling": "mymod:maple_sapling"}` — or, without one, from the name (`maple_log` → `maple_sapling`). They reload with `/reload`.

---

## Card Decks

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_blue.png" width="37" alt="Protection Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_green.png" width="37" alt="Nature Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_red.png" width="37" alt="Combat Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_yellow.png" width="37" alt="Utility Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_purple.png" width="37" alt="Arcane Deck"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_rare.png" width="37" alt="Rarity Deck"></p>

A **Card Deck** is a brass-bound case that stores Magic Cards of one type. Place decks in a **Deck Lectern** within an Enchanting Table's normal bookshelf range (same positions and air gap as regular bookshelves, but **at the table's own height**) and the table's rolls shift according to what you've stored.

| Deck | Stores | Effect on nearby Enchanting Tables |
|------|--------|-----------------------------------|
| Protection / Nature / Combat / Utility / Arcane | Cards of that color | Favors that category, and even more the exact enchantments stored |
| Rarity (wildcard) | Curse cards | Favors rare enchantments (weight ≤ 2) |

### Crafting Recipe

|   |   |   |
|---|---|---|
|   | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |   |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/leather.png" width="32" alt="Leather"><br>Leather | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/magic_card_red.png" width="32" alt="Card of the deck's type"><br>Card of the deck's type | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/leather.png" width="32" alt="Leather"><br>Leather |
|   | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |   |

<p>→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/card_deck_red.png" width="42" alt="Card Deck"> <strong>Card Deck</strong> × 1 — the card is consumed and sets the deck's type (Rarity: a Curse card)</p>

### How it works

- **Storing**: right-click a card onto the deck (or the deck onto a card), like a bundle. Right-click with an empty cursor to take the last stack back out.
- **Deck screen**: right-click with a deck in hand to open it — its cards in a grid, with a points bar and a reserve bar — and move cards in and out at a glance (Shift + click works). The 64-point limit still applies. The deck's tooltip also shows its reserve and each worn card's.
- **Points**: a level N card is worth 2^(N-1) points (same ratio as combining). Each deck holds up to 64 points.
- **Enchanting power**: a full Deck Lectern counts as **two bookshelves** — the two that would stack in its column (every 3 decks holding cards = 2 bookshelves). They add up with regular bookshelves, and the vanilla cap of 15 still applies — so higher enchantment levels come from power, just like with bookshelves; 8 full lecterns alone reach level 30.
- **Balance**: the bias is relative — a library with every category in equal parts barely changes anything; a specialized one makes its category up to ×3 more likely. Every card favors its category; only max-level cards (Efficiency V, Fortune III, Silk Touch I…) also favor their exact enchantment, up to ×15 in all. Strength builds up with the points stored — fast at first, so even a few level I cards show, and in full with one full deck (64 points); more decks don't raise it further, but add reserve and power.
- **Location affinity**: some decks resonate with where the table is — Combat ×2 in the Nether, Fire/Blast Protection ×1.5 and Arcane fire (Fire Aspect, Flame) ×2 in the Nether, Rarity ×1.5 in the Nether, Arcane ×2 in the End, Nature's water enchantments ×1.5 in oceans and rivers Fortune/Silk Touch ×1.5 in forests, jungles and taigas, and Harvesting ×1.5 in plains. It scales the deck's extra effect (capped at ×3). Rules are datapack JSON in `data/<namespace>/enchanttransfer/library_affinity/`, so modpacks can add their own dimensions and biomes (`deck`, optional `dimension`, `biome`, `enchantments`, and `multiplier`); they reload with `/reload`.
- **Wear**: decks pay, in card reserve, only for what their bonus brings in — not for what the table would have rolled anyway, nor for what comes from the decks' power. Each card holds as much reserve as the points it takes (a level V card holds 16, so a full deck always holds 64); worn cards show it as a bar. Each marked enchantment costs 20% of a max-level card of it, scaled by the level obtained (Efficiency V: 3.2, Efficiency III: 1.92, Silk Touch I: 0.2): in full when max-level cards of it are in the decks, half for a category or Rarity bonus. It's split among the deck kinds that boosted it, in proportion to their boost, and taken first from a card already started, then from the lowest-level ones. An option the decks can't pay for is disabled. Worn cards work normally at the Transfer Table, and combining two adds up what they had left.
- **Influence mark**: options changed by the decks show a small card in their top-right corner and an *Influenced by card decks* line in their tooltip.

> **No marks? That's normal.** Decks tilt the odds, they don't pick for the table: when it would have rolled the same enchantment anyway, the option isn't marked and costs no card. Some rolls show no marks at all — you'll see more with fuller, more specialized decks and higher table levels. The rolls only change after you enchant something, so re-placing the item shows the same options.

### Deck Lectern

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_deck_lectern.png" width="114" alt="Deck Lectern"></p>

The block that holds your decks: a low brass base with a tilted velvet tray for **3 decks**, under a glass bell.

- **Placement**: only counts **at the same height as the enchanting table**. Its glass bell fills the whole column, so **nothing can be placed on top of it** (and it can't go under a block) — that's why it's worth two bookshelves.
- **Right-click** opens a small screen with 3 deck slots. Hovering the amethyst gem shows what the lectern gives a table on its own, next to each maximum: each category's and max-level enchantment's factor, the power and the wear reserve.
- **Lamps**: one per slot — dark brown (no deck), light (deck without cards), emerald (deck with cards).
- **Off by default.** Shift + right-click switches it on (it needs at least one deck with cards) or off, so you choose which lecterns count; the amethyst on top of the bell glows while it's on. It switches itself off when its decks run out of cards, but never on.
- While active, runes fly from the lectern to the enchanting table, like with bookshelves.
- Place lecterns where regular bookshelves would go around an Enchanting Table (same positions and air gap). One ring of 16 at table height already reaches the cap of 15, so they don't need to be stacked.

|   |   |   |
|---|---|---|
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_block.png" width="32" alt="Glass"><br>Glass | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/amethyst_shard.png" width="32" alt="Amethyst Shard"><br>Amethyst Shard | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_block.png" width="32" alt="Glass"><br>Glass |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone"><br>Redstone | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/oak_planks.png" width="32" alt="Planks"><br>Planks | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/oak_planks.png" width="32" alt="Planks"><br>Planks | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/oak_planks.png" width="32" alt="Planks"><br>Planks |

<p>→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_deck_lectern.png" width="61" alt="Deck Lectern"> <strong>Deck Lectern</strong> × 1</p>

---

## Module System

The Transfer Table now works as a **hub**. Specialized module blocks can be placed on any of its 6 faces. Right-click the Transfer Table to open the **Selector Screen** — a visual hub showing all connected modules with their real-time status.

- Up to **6 modules** per Transfer Table (one per face)
- Each module type adds different functionality
- Navigate between the hub and any module using the **dot navigation bar**
- Modules show live status in the Selector — processing progress, tank levels, and connection state

### GUI Screens

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_selector.png" width="200" alt="Selector Screen"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_core.png" width="200" alt="Core Screen"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_infusor.jpg" width="200" alt="Infusor Screen"></p>

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
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/amethyst_shard.png" width="32" alt="Amethyst Shard"><br>Amethyst Shard | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="32" alt="Brass Ingot"><br>Brass Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_block.png" width="32" alt="Glass Block"><br>Glass Block | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |

<p>→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_infusion_coil.png" width="46" alt="Infusion Coil"> <strong>Infusion Coil</strong> × 1</p>

---

## Zinc Smelter

A standalone steampunk furnace for zinc processing and brass alloy creation. An ornate kiln made of copper, brass, and iron with a glass viewport and chimney.

### New Materials

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/zinc_oxide.png" width="48" style="image-rendering: pixelated;" alt="Zinc Oxide"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/zinc_sheet.png" width="48" style="image-rendering: pixelated;" alt="Zinc Sheet"> <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/brass_ingot.png" width="48" style="image-rendering: pixelated;" alt="Brass Ingot"></p>

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
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/iron_ingot.png" width="32" alt="Iron Ingot"><br>Iron Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/iron_ingot.png" width="32" alt="Iron Ingot"><br>Iron Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/iron_ingot.png" width="32" alt="Iron Ingot"><br>Iron Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/glass_pane.png" width="32" alt="Glass Pane"><br>Glass Pane | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/blast_furnace.png" width="32" alt="Blast Furnace"><br>Blast Furnace | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/copper_ingot.png" width="32" alt="Copper Ingot"><br>Copper Ingot |

<p>→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_zinc_smelter.png" width="59" alt="Zinc Smelter"> <strong>Zinc Smelter</strong> × 1</p>

### Fuel

The Zinc Smelter only accepts **Lava Buckets** and **Blaze Rods** as fuel — no coal or wood. Lava Buckets return an empty bucket after consumption.

### GUI

<p><img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gui_zinc_smelter.jpg" width="200" alt="Zinc Smelter GUI"></p>

*Steampunk-themed interface with copper input slots, a lava-bordered fuel slot, and brass output slot.*

---

## Transfer Table

### Crafting Recipe

|   |   |   |
|---|---|---|
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gold_ingot.png" width="32" alt="Gold Ingot"><br>Gold Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gold_ingot.png" width="32" alt="Gold Ingot"><br>Gold Ingot | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/gold_ingot.png" width="32" alt="Gold Ingot"><br>Gold Ingot |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone Dust"><br>Redstone Dust | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone Dust"><br>Redstone Dust | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/redstone.png" width="32" alt="Redstone Dust"><br>Redstone Dust |
| <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/coal.png" width="32" alt="Coal / Charcoal"><br>Coal / Charcoal | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/diamond.png" width="32" alt="Diamond"><br>Diamond | <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/coal.png" width="32" alt="Coal / Charcoal"><br>Coal / Charcoal |

<p>→ <img src="https://raw.githubusercontent.com/arensis/enchant-transfer-mod/master/docs/images/render_transfer_table.png" width="61" alt="Transfer Table"> <strong>Transfer Table</strong> × 1</p>

Requires an **iron pickaxe** or better to mine (the same applies to the Infusion Coil and the Zinc Smelter).

The 3D model reflects its recipe — gold on the outer edge, redstone in the middle ring, carbon-black inner layer, and a blue diamond core that glows from every angle.

---

## Compatibility

> **Worlds from 3.0.x are fully compatible:** nothing in them changes, and an Enchanting Table without Deck Lecterns nearby works exactly like vanilla. Cards extracted before 3.1 keep their color (e.g. yellow Mending, green Soul Speed, or blank Knockback cards) but still go into the deck of their current category — decks check the enchantment, not the card color.

> **Worlds from 1.0.x are fully compatible.** Existing Transfer Tables keep working — the block ID hasn't changed. Old Magic Cards stored in chests retain their enchantment data. They will appear as the generic (blank) card variant; their functionality is unchanged. New cards extracted after updating will automatically use the color-coded system.

---

## Changelog — 3.2.x

### 3.2.1

- **Enchantment names on older clients** — Magnetism and Harvesting fall back to their English name instead of a raw translation key when a client has an older version of the mod
- **Server stop hang fixed** — Stopping the server could hang forever on "Saving worlds" when a chunk with an Infusion Coil unloaded after its Transfer Table's chunk. Infusion Coils also no longer load neighbouring chunks while looking for their Transfer Table

### 3.2.0

- **Magnetism** — New Utility enchantment (I–V) for metal armor: pulls nearby items and XP orbs toward you (4 to 12 blocks). Sneak to switch it off
- **Harvesting** — New Nature enchantment (I–IV) for hoes and axes. Hoe: right-click a ripe crop to harvest and replant it, then a row of 3, the whole field (0.5 XP levels per 9×9) and bone meal growth. Axe: replant the sapling, then fell the trunk, the whole tree (0.5 XP levels) and grow the sapling back with bone meal. It spends a seed or sapling from your inventory per plant, and only fells natural trees
- **Crops and trees from other mods** — Harvesting works with other mods' crops out of the box, and their trees through datapack rules in `data/<namespace>/enchanttransfer/harvesting_sapling/` or the `_log` → `_sapling` naming convention
- **Shift + click at the Transfer Table** — A card goes straight onto the item when it fits; otherwise the stack is split evenly into both combine slots. Colored cards can now be shift-clicked too
- **Zinc Smelter drops itself** — It dropped nothing when mined; it now drops with an iron pickaxe or better
- **Card categories reviewed** — Each color now has a clear theme: bow and crossbow attacks (Power, Punch, Multishot, Piercing) move to **Combat**; Fire Aspect, Flame and Infinity move to **Arcane**, which now means magic (fire, ice, lightning, wind, souls, experience) rather than "treasure"; Swift Sneak moves to **Utility** (speed and durability). Cards already extracted keep their color; take them out of their old deck and they go into the new one
- **Configurable XP cost** — Game rules `enchanttransfer:harvesting_field_xp_cost` and `enchanttransfer:harvesting_tree_xp_cost` (0.5 levels by default; 0 makes them free, any higher value works)
- **Plains affinity** — Nature decks favor Harvesting ×1.5 at tables in plains (`#c:is_plains`, so modded plains count too)
- **New languages** — French (fr_fr), German (de_de) and Japanese (ja_jp)

**Older versions:** see the [full changelog](https://arensis.github.io/enchant-transfer-mod/#changelog).

---

## Installation

### Minecraft 26.2 — mod v3.2.x

1. Make sure Minecraft runs with **Java 25**
2. Install [Fabric Loader](https://fabricmc.net/use/) **≥ 0.19.3** for Minecraft 26.2
3. Download [Fabric API](https://modrinth.com/mod/fabric-api) for 26.2 and place it in the `mods/` folder
4. Download the Enchant Transfer **3.2.x** JAR and place it in `mods/` as well
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

| Dependency | Minecraft 26.2 (v3.2.x) | Minecraft 1.21.11 (v2.0.x) |
|------------|-------------------------|----------------------------|
| Java | 25 | 21 |
| Fabric Loader | ≥ 0.19.3 | ≥ 0.19.2 |
| Fabric API | 0.158.0+26.2 | 0.141.4+1.21.11 |

---

## License

This project is distributed under the **CC0-1.0** license. You may use, modify, and distribute the code without restrictions.
