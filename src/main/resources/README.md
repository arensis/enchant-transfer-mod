# Enchant Transfer — Paquete de Handoff

Paquete completo para implementar el sistema **Hub & Spoke** en el mod (Fabric 1.21.1, `net.alfonsormadrid.enchanttransfer`).

## Los 3 documentos de handoff

1. **`TRANSFER_TABLE_HANDOFF.md`** — el bloque central (mesa): cubo con embudo escalonado (oro→redstone→carbón→núcleo de diamante emisivo).
2. **`INFUSOR_COIL_HANDOFF.md`** — el primer módulo (matraz): bloque 3D direccional + tanque de XP + luz del tapón.
3. **`GUI_HANDOFF.md`** — las tres interfaces: Selector (L1), Core (L2) e Infusor.

## Cómo usarlo con Claude Code

1. Copia toda la carpeta `assets/enchanttransfer/` a `src/main/resources/assets/enchanttransfer/`.
2. Pásale los 3 documentos `.md` y dile: *"implementa el sistema Hub & Spoke siguiendo estos handoffs"*.
3. Lo que va por **código** (no por textura/modelo):
   - Brillo del núcleo de la mesa (planos emisivos).
   - Tanque de XP + luz del tapón del Infusor + conector direccional.
   - Estado de módulos en vivo en el Selector/Core (qué hay acoplado en cada cara).

## Referencias visuales (`reference/`)

- `transfer_table_3d_preview.html` — visor 3D interactivo de la mesa (aprobado).
- `gui_mockups_preview.html` — las 3 GUIs a escala con anotaciones.
- `infusor_coil_block_animated.png` (+ `.mcmeta`) — textura animada de fallback del matraz.

## Estructura de assets

```
assets/enchanttransfer/
├── blockstates/        transfer_table · infusor_coil
├── models/block/       transfer_table (embudo) · infusor_coil (matraz)
├── models/item/        transfer_table · infusor_coil
└── textures/
    ├── block/          table_* (gold/redstone/carbon/core) · infusor_* (copper/brass/glass)
    └── gui/
        ├── container/   selector_gui · core_gui · infusor_gui
        └── sprites/     module_infusor (3 frames) · socket_empty
```
