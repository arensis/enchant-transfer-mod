# Contexto de migración: 1.16.5 → 1.21.1

Objetivo: migrar el mod Enchant Transfer de Minecraft 1.16.5 a 1.21.1 con Fabric, conservando la arquitectura actual íntegramente.

## Versiones objetivo

| Dependencia | 1.16.5 (antes) | 1.21.1 (después) |
|---|---|---|
| minecraft_version | 1.16.5 | 1.21.1 |
| yarn_mappings | 1.16.5+build.10 | 1.21.1+build.3 |
| loader_version | 0.11.6 | 0.16.5 |
| fabric_version | 0.40.1+1.16 | 0.102.0+1.21.1 |
| fabric-loom | 0.6-SNAPSHOT | 1.7-SNAPSHOT |
| Java | 8 | 21 |

## Cambios por archivo

### gradle.properties
- Actualizar todas las versiones listadas arriba.

### build.gradle
- `fabric-loom` version: `0.6-SNAPSHOT` → `1.7-SNAPSHOT`
- `sourceCompatibility` / `targetCompatibility`: `VERSION_1_8` → `VERSION_21`
- Bloque `def targetVersion = 8` → `it.options.release = 21`

### fabric.mod.json
- `"fabricloader": ">=0.7.4"` → `">=0.15.0"`
- `"minecraft": "1.16.x"` → `"~1.21.1"`

### EnchantTransferMod.java
- `new Identifier(...)` → `Identifier.of(...)`
- `Registry.BLOCK/ITEM/BLOCK_ENTITY_TYPE` → `Registries.BLOCK/ITEM/BLOCK_ENTITY_TYPE`
- `FabricItemGroupBuilder.build(...)` eliminado → `FabricItemGroup.builder()` + `ItemGroupEvents.modifyEntriesEvent`
- `ScreenHandlerRegistry` → mismo nombre, verificar paquete
- Loot tables: `LootTableLoadingCallback` (v1) → `LootTableEvents.MODIFY` (v2), `ConstantLootTableRange` → `ConstantLootNumberProvider`, `LootTableEntry` → `LootTableLootEntry`

### EnchantTransferClientMod.java
- `ScreenRegistry.register(...)` → `HandledScreens.register(...)`

### TransferTableBlock.java
- `FabricToolTags.PICKAXES` eliminado → mining level vía data tags en `data/minecraft/tags/blocks/`
- `createBlockEntity(BlockView)` → `createBlockEntity(BlockPos pos, BlockState state)`
- `onUse(BlockState, World, BlockPos, PlayerEntity, Hand, BlockHitResult)` → sin parámetro `Hand` en 1.21

### TransferTableBlockEntity.java
- Constructor: `()` → `(BlockPos pos, BlockState state)`
- `super(ENTITY_TYPE)` → `super(ENTITY_TYPE, pos, state)`
- `new TranslatableText(...)` → `Text.translatable(...)`
- `BlockEntityType.Builder.create(...).build(null)` → `.build()`

### TransferTableItem.java + MagicCardItem.java
- `.group(ItemGroup)` → eliminar (items se añaden al tab vía `ItemGroupEvents`)
- `.fireproof()` → `.fireImmune()`

### TransferTableScreen.java
- `MatrixStack` → `DrawContext` en todos los métodos de render
- `RenderSystem.color4f(...)` → eliminar
- `client.getTextureManager().bindTexture(TEXTURE)` → eliminar
- `drawTexture(matrices, ...)` → `context.drawTexture(TEXTURE, ...)`
- `renderBackground(matrices)` → `renderBackground(context, mouseX, mouseY, delta)`
- `super.render(matrices, ...)` → `super.render(context, ...)`
- `drawMouseoverTooltip(matrices, ...)` → `drawMouseoverTooltip(context, ...)`

### TransferTableScreenHandler.java
- `player.world` → `player.getWorld()`

### CombineCardService.java + TransferItemSlot.java + TransferItemContentSlot.java
Cambio mayor en la API de encantamientos en 1.20.5+:
- `EnchantmentHelper.get(stack)` ya no devuelve `Map<Enchantment, Integer>`
- Ahora se usa `stack.getEnchantments()` → `ItemEnchantmentsComponent`
- Los encantamientos son `RegistryEntry<Enchantment>`, no `Enchantment` directamente
- Para iterar: `component.getEnchantments()` → `Set<RegistryEntry<Enchantment>>`
- Para nivel: `component.getLevel(registryEntry)` → `int`
- Para max level: `registryEntry.value().getMaxLevel()`
- Para compatibilidad: `Enchantment.canCombine(entry1, entry2)` o `EnchantmentHelper.isCompatible(collection, entry)`
- Para si acepta item: `registryEntry.value().isAcceptableItem(itemStack)`
- `stack.addEnchantment(enchantment, level)` → `stack.addEnchantment(registryEntry, level)`
- `EnchantmentHelper.set(map, stack)` → construir `ItemEnchantmentsComponent` o usar `stack.set(DataComponentTypes.ENCHANTMENTS, component)`
- `stack.hasEnchantments()` → `!stack.getEnchantments().isEmpty()`
- `stack.getEnchantments().size()` → `stack.getEnchantments().getSize()`

### MagicCardItem.java
- `stack.getEnchantments().size() < 1` → `stack.getEnchantments().isEmpty()`

### Nuevos archivos de recursos (data tags)
Crear para que el bloque sea mineable con pickaxe y requiera hierro:
- `src/main/resources/data/minecraft/tags/blocks/mineable/pickaxe.json`
- `src/main/resources/data/minecraft/tags/blocks/needs_iron_tool.json`

Contenido de ambos:
```json
{ "values": ["enchanttransfer:transfer_table_block"] }
```

## Arquitectura — sin cambios
Todos los paquetes y clases se conservan:
- `blocks.transfertable` — sin restructurar
- `gui.transfertable` — sin cambios
- `item` — sin cambios estructurales
- `screens.transfertable` + `screens.transfertable.slot` — sin restructurar
- `services` — sin cambios estructurales
