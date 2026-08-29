# Horno de Zinc — Handoff Completo

## Resumen
Bloque standalone steampunk (1×1×1) para fundir minerales de zinc y crear aleaciones de latón.
Variante elegida: **C — Ornate Kiln**.

---

## 1. Bloque — Zinc Smelter

### BlockState
- `facing` (NORTH/EAST/SOUTH/WEST) — orienta la puerta/mirilla
- `lit` (true/false) — estado encendido/apagado (textura lava + partículas)

### Geometría (Variante C — Ornate Kiln)
| # | Pieza | Coords MC (from → to) | Material |
|---|-------|----------------------|----------|
| ① | Base ornamental | [-1,0,-1] → [17,2,17] | Latón |
| ② | Cuerpo principal | [0.5,2,0.5] → [15.5,14,15.5] | Cobre |
| ③ | Aristas ×4 | [-0.5,2,-0.5] → [2,14,2] (y esquinas) | Latón |
| ④ | Puerta frontal | [4,3,16] → [12,9,17] | Cobre |
| ⑤ | Mirilla | [6,4.5,16.5] → [10,7.5,17] | Cristal |
| ⑥ | Bandas ×3 | y:5, y:10, y:13 (full width) | Latón |
| ⑦ | Tapa | [-0.5,14,-0.5] → [16.5,15.5,16.5] | Latón |
| ⑧ | Chimenea (BER) | [6,16,6] → [10,22,10] | Cobre + Latón |
| ⑨ | Tubo lateral | [-2,7.5,6.5] → [0.5,10.5,9.5] | Cobre + Latón |

### Animación (lit=true)
- **Lava**: brillo naranja pulsante visible por la mirilla
- **Llamas**: 4 lenguas de fuego animadas detrás del cristal
- **Humo**: `ParticleTypes.SMOKE` + `LAVA` desde la chimenea
- **Glow**: resplandor naranja ambiental en la mirilla

### Texturas de bloque (16×16)
- `smelter_copper.png` — cobre forjado con highlights
- `smelter_brass.png` — latón pulido
- `smelter_iron.png` — hierro fundido con noise
- `smelter_lava.png` — lava animada (usar como overlay lit)
- `smelter_glass.png` — cristal ahumado verde (con alpha)

---

## 2. Items

### Lingote de Latón (`brass_ingot`)
- 16×16, forma trapezoidal 3D estilo vanilla MC
- Tono dorado-cobre (#c89030 base, #f0d868 highlight)

### Óxido de Zinc (`zinc_oxide`)
- 16×16, montículo de polvo blanco-gris
- Tonos #d8d4cc a #f8f4ec, con motas azuladas

### Lámina de Zinc (`zinc_sheet`)
- 16×16, rectángulo metálico plano gris-azulado
- Tonos #9098a8 base, #b0b8c8 highlights, marcas de martilleo

---

## 3. GUI — Horno de Zinc (Steampunk)

### Tema
Estilo steampunk propio — NO sigue el estilo de la Transfer Table.
- Fondo: marrón oscuro (#1e160e)
- Bordes: latón (#8a6030)
- Remaches en esquinas (#c89030)
- Tuberías decorativas laterales
- Title bar: latón (#c89030)

### Layout (256×256)
```
[title bar — latón]

[input1 ■]              [═══▶]   [■ output]
[input2 ■]   [🔥]
             [fuel ■]
             
─ ─ ─ ─ ● ─ ─ ─ ─  (separador)

[inv 3×9]
[hotbar 1×9 — borde latón]
```

### Slots
| Slot | Tipo | Border | Posición MC |
|------|------|--------|-------------|
| input1 | Input | Cobre (#b86a28) | (12, 22) |
| input2 | Input | Cobre (#b86a28) | (12, 52) |
| fuel | Fuel | Lava (#e85818) | (42, 62) |
| output | Output | Latón (#c89030) | (136, 30) |

### Fuel whitelist
- `Items.LAVA_BUCKET`
- `Items.BLAZE_ROD`

---

## 4. Recetas

### RecipeType: `ZincSmeltingRecipe`
Serializer custom que acepta 1 o 2 ingredientes.

```json
// ① Calcinación: Calcita → Óxido de Zinc
{
  "type": "enchanttransfer:zinc_smelting",
  "input1": { "item": "minecraft:calcite" },
  "fuel_time": 200,
  "result": { "item": "enchanttransfer:zinc_oxide", "count": 2 }
}

// ② Reducción: Óxido de Zinc + Carbón → Lámina de Zinc
{
  "type": "enchanttransfer:zinc_smelting",
  "input1": { "item": "enchanttransfer:zinc_oxide" },
  "input2": { "item": "minecraft:coal" },
  "fuel_time": 300,
  "result": { "item": "enchanttransfer:zinc_sheet", "count": 1 }
}

// ③ Aleación: Lingote de Cobre + Lámina de Zinc → Lingote de Latón
{
  "type": "enchanttransfer:zinc_smelting",
  "input1": { "item": "minecraft:copper_ingot" },
  "input2": { "item": "enchanttransfer:zinc_sheet" },
  "fuel_time": 400,
  "result": { "item": "enchanttransfer:brass_ingot", "count": 1 }
}
```

---

## 5. Implementación

### ZincSmelterBlock
- Extends `BaseEntityBlock`
- BlockState: `FACING` (horizontal) + `LIT` (boolean)
- Se coloca orientado al jugador (como un horno vanilla)
- Drop: sí mismo + contenido de slots

### ZincSmelterBlockEntity
- 4 slots: `input1` (0), `input2` (1), `fuel` (2), `output` (3)
- `litTime`, `litDuration` — combustible restante
- `cookingProgress`, `cookingTotalTime` — progreso de fundición
- Tick: si hay receta válida + fuel → avanza progreso
- Fuel check: solo `LAVA_BUCKET` o `BLAZE_ROD`
- Al consumir lava bucket: devuelve bucket vacío al slot fuel

### ZincSmelterScreen + ZincSmelterMenu
- Menu: 4 slots bloque + 36 jugador
- Screen: renderiza `zinc_smelter_gui.png` de fondo
- Llama animada sobre fuel slot cuando `litTime > 0`
- Flecha de progreso proporcional a `cookingProgress / cookingTotalTime`

### BER (BlockEntityRenderer)
- Chimenea (y > 16): renderizar cuboides cobre + tapa latón
- Partículas cuando `lit=true`
- Glow naranja en mirilla cuando `lit=true`

---

## 6. Estructura de archivos

```
assets/enchanttransfer/
├── blockstates/
│   └── zinc_smelter.json
├── models/
│   ├── block/
│   │   ├── zinc_smelter.json
│   │   └── zinc_smelter_lit.json
│   └── item/
│       └── zinc_smelter.json
└── textures/
    ├── block/
    │   ├── smelter_copper.png
    │   ├── smelter_brass.png
    │   ├── smelter_iron.png
    │   ├── smelter_lava.png
    │   └── smelter_glass.png
    ├── item/
    │   ├── brass_ingot.png
    │   ├── zinc_oxide.png
    │   └── zinc_sheet.png
    └── gui/container/
        └── zinc_smelter_gui.png

elements/  (GUI sueltos, para montar manualmente)
├── slot_input_copper_18x18.png
├── slot_output_brass_18x18.png
├── slot_fuel_lava_18x18.png
├── progress_arrow_80x18.png
├── progress_arrow_filled_80x18.png
└── flame_indicator_16x16.png

reference/
├── zinc-smelter-final.html    (visor 3D + GUI + items)
└── zinc-smelter-variants.html (comparativa 3 variantes)
```
