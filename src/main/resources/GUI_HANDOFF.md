# GUIs — Handoff para Claude Code

**Referencia visual:** `selector-and-modules.html` secciones 02, 04, 05.
Los PNGs de este paquete son **fondos estáticos limpios**. Todo lo dinámico (módulos, nivel de tanque, progreso) lo dibuja el Screen por código.

---

## Lo que es ESTÁTICO (PNG) vs DINÁMICO (código)

| Elemento | Dónde va |
|---|---|
| Panel de fondo, estrellitas, título parchment | PNG |
| Marcos de slots (combine, item, cards, inventory) | PNG |
| Frame vacío del progress arrow, tank, tap | PNG |
| Hub central (siempre existe) | PNG |
| Sockets vacíos (fondo oscuro + círculo dim) | PNG |
| **Iconos de módulo** (qué hay en cada socket) | **Código** |
| **Wires** entre hub y sockets conectados | **Código** |
| **Estado del icono** (idle/active) | **Código** |
| **Nivel del tanque** (fill del tank) | **Código** |
| **Progreso de infusión** (fill del arrow + arc en socket) | **Código** |
| **Nav row** — qué círculo está activo/conectado | **Código** |
| **Progress arrow fill** (violeta→cian) | **Código** |

---

## 1. Selector (L1) — `selector_gui.png` · 256×256 (contenido 200×200)

PNG contiene: panel + título + hub dorado + 6 sockets vacíos (fondo oscuro, borde dim).

### Posiciones de los 6 sockets (coordenadas del centro, en px del contenido)

| Socket | cx, cy | Cara física |
|---|---|---|
| N  | 100, 48  | North  |
| E  | 155, 76  | East   |
| ↑  | 155, 140 | Up     |
| S  | 100, 168 | South  |
| ↓  | 45, 140  | Down   |
| W  | 45, 76   | West   |

Radio de cada socket: **16 px**.

### Render dinámico por código

```java
for (Direction dir : Direction.values()) {
    Vec2i pos = socketPos(dir);    // lookup table arriba
    BlockState bs = world.getBlockState(tablePos.offset(dir));
    if (bs.getBlock() instanceof ModuleBlock mod) {
        // dibujar wire desde hub edge hasta socket
        drawWire(ctx, hubPos, pos, mod.accentColor());
        // dibujar icono del módulo con su estado actual
        ModuleState st = mod.getClientState(world, tablePos.offset(dir));
        float tankFill = mod.getTankFill();   // 0..1
        float progress = mod.getProgress();   // 0..1
        drawModuleIcon(ctx, pos, mod.iconTexture(), st, tankFill, progress);
    } else {
        // socket vacío — dibujar "+" encima del fondo del PNG
        drawSocketEmpty(ctx, pos);
    }
}
```

### Sprites de módulo (sprites/module_infusor.png — 32×96, 3 frames)

El frame base no incluye fluid ni arc — el renderer los dibuja encima:

| Frame y | Estado base |
|---|---|
| 0 | disconnected |
| 32 | idle (sin fluid ni arc) |
| 64 | active (sin fluid ni arc) |

Encima del frame el renderer dibuja:
- **Fluid fill** (clip circular, bottom-up): `fill = tankFill * circleRadius * 2`
- **Progress arc** (si active): arco cian desde -90° cubriendo `progress * 360°`

---

## 2. Core (L2) — `core_gui.png` · 256×256 (contenido 176×200)

PNG contiene: panel + título parchment + frames de slot (combine, item, cards) + inventory. Nav row vacía (solo fondos de círculos).

### Nav row (y=21, centrada en x=88)

7 círculos, r=4, spacing=12. **El código dibuja**:
- Hub (i=0): pictograma hex dorado + glow cian si es la pantalla activa
- Módulo conectado (i=1..6): pictograma del módulo + borde color del módulo
- Vacío: punto dim
- Activo: anillo exterior cian

### Slots

| Elemento | X, Y |
|---|---|
| combine input 1 | 13, 39 |
| combine input 2 | 13, 62 |
| combine result  | 42, 50 |
| item slot       | 75, 50 |
| cards 4×3       | 97–151, 32–68 |
| inventory       | 8, 118 |
| hotbar          | 8, 176 |

---

## 3. Infusor Coil (L2) — `infusor_gui.png` · 256×256 (contenido 176×200)

PNG contiene: panel + título + nav row vacía + frames de slots + frame vacío del tank + inventory.

### Slots

| Slot | X, Y | Acepta |
|---|---|---|
| card-in | 26, 38 | Magic Card |
| glass-in | 26, 66 | botella vacía |
| xp-out | 70, 66 | salida |

### El código dibuja encima

- **Progress arrow fill** en (57–77, 44): `fillWidth = 20 * (infuseProgress/INFUSE_TIME)`, color violeta→cian
- **XP tank fill** en (131–142, 32–87): `fillHeight = 55 * (storedXp/MAX_XP)`, bottom-up, color xp-green
- **Tank readout** texto: `"234 / 1000"`
- **Nav row** dinámica (E = activo)

---

## 4. Texturas incluidas

```
textures/gui/container/
├── selector_gui.png   ✅ fondo limpio (200×200 en 256×256)
├── core_gui.png       ✅ fondo limpio (176×200 en 256×256)
└── infusor_gui.png    ✅ fondo limpio (176×200 en 256×256)
textures/gui/sprites/
├── module_infusor.png ✅ 3 frames base (32×96) — fluid y arc por código
└── socket_empty.png   ✅ socket vacío (32×32)
```

**Referencia visual completa:** `reference/selector-and-modules.html` (mostrar al equipo para ver estados dinámicos de ejemplo).
