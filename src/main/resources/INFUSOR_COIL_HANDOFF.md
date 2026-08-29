# Infusor Coil — Handoff para Claude Code

Módulo nuevo para **enchant-transfer-mod** (Fabric 1.21.1). El Infusor Coil consume Magic Cards, las convierte en XP almacenado en un tanque, y permite extraer ese XP rellenando botellas de cristal. Es el primer módulo del sistema Hub & Spoke.

---

## 1. Qué es estático vs. dinámico

| Parte | Cómo se hace | Archivo |
|---|---|---|
| Cuerpo, tubo, tapón, tiras, aros | **Modelo JSON estático** | `models/block/infusor_coil.json` |
| Texturas de caras (cobre/latón/vidrio) | **PNG 16×16** | `textures/block/infusor_*.png` |
| **Nivel del tanque** (líquido que sube/baja) | **Código** — `BlockEntityRenderer` | `InfusorCoilRenderer.java` |
| **Luz del tapón** (emisiva al funcionar) | **Código** — `BlockEntityRenderer` | idem |
| **Conector** hacia la mesa (direccional) | **Código** — `BlockEntityRenderer` | idem |
| Goteo opcional (animación interna) | **Código** — `BlockEntityRenderer` | idem |

> **Clave de diseño:** el matraz queda **siempre vertical** para las 6 caras de acople. La cara a la que apunta se guarda en el `BlockEntity` (no en rotación del blockstate), y el renderer dibuja el conector hacia esa cara. Así el matraz nunca se inclina.

---

## 2. Árbol de archivos (assets incluidos en este paquete)

```
src/main/resources/assets/enchanttransfer/
├── blockstates/
│   └── infusor_coil.json            ✅ incluido (variante única, matraz vertical)
├── models/
│   ├── block/infusor_coil.json      ✅ incluido (3 cuboides + tiras)
│   └── item/infusor_coil.json       ✅ incluido (parent → block model)
└── textures/block/
    ├── infusor_copper.png           ✅ incluido (16×16)
    ├── infusor_brass.png            ✅ incluido (16×16)
    └── infusor_glass.png            ✅ incluido (16×16, con alpha)
```

Falta crear en código (ver secciones 4–6):
```
src/main/java/net/alfonsormadrid/enchanttransfer/
├── block/InfusorCoilBlock.java
├── block/entity/InfusorCoilBlockEntity.java
├── client/render/InfusorCoilRenderer.java
├── screen/infusor/InfusorCoilScreenHandler.java
└── screen/infusor/InfusorCoilScreen.java
```

---

## 3. Modelo 3D (geometría, en píxeles de Minecraft 0–16)

3 cuboides principales + adornos. Ya está todo en `models/block/infusor_coil.json`:

| Pieza | from | to | Textura |
|---|---|---|---|
| ① Cuerpo | `3.5, 0, 3.5` | `12.5, 9, 12.5` | vidrio |
| Aro inferior | `3, 0, 3` | `13, 1.5, 13` | cobre |
| Aro superior | `3, 7.5, 3` | `13, 9, 13` | cobre |
| Tiras verticales ×4 | esquinas | (1×9×1) | cobre |
| ② Tubo (cuello) | `6.5, 9, 6.5` | `9.5, 15, 9.5` | cobre |
| Collares ×2 | `6,9,6`→`10,10,10` / `6,14,6`→`10,15,10` | | latón |
| ③ Tapón | `5.5, 15, 5.5` | `10.5, 17, 10.5` | latón |
| Pomo | `7, 17, 7` | `9, 18, 9` | latón |
| Brackets de tapón ×4 | esquinas | (1×2×1) | cobre |

El cuerpo es de vidrio translúcido → registra el bloque en el render layer **translucent** o **cutout**:
```java
BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.INFUSOR_COIL, RenderLayer.getTranslucent());
```

---

## 4. BlockState direccional

El bonded face NO rota el modelo (el matraz queda vertical). Guárdalo como propiedad para la lógica/renderer:

```java
public static final DirectionProperty BONDED_FACE = DirectionProperty.of("bonded_face");
// en appendProperties: builder.add(BONDED_FACE);
// al colocar: world.setBlockState(pos, state.with(BONDED_FACE, faceTowardTable));
```

El `blockstates/infusor_coil.json` incluido usa una sola variante (sin rotación). El renderer lee `BONDED_FACE` para orientar el conector.

---

## 5. BlockEntity — datos

```java
public class InfusorCoilBlockEntity extends BlockEntity {
    private int storedXp = 0;
    public static final int MAX_XP = 1000;     // capacidad del tanque
    private int infuseProgress = 0;            // 0..INFUSE_TIME
    public static final int INFUSE_TIME = 200; // ticks por carta (≈10s) — escala con coste del enchant
    private Direction bondedFace = Direction.NORTH;

    // Inventario: 3 slots
    //  0 = card-in   (solo Magic Card)
    //  1 = glass-in  (solo botella de cristal vacía)
    //  2 = xp-out    (salida, solo extracción)

    public float getFillRatio() { return storedXp / (float) MAX_XP; }
    public boolean isInfusing()  { return infuseProgress > 0; }
    public float getProgress()   { return infuseProgress / (float) INFUSE_TIME; }
    // nbt: readNbt/writeNbt para storedXp, infuseProgress, bondedFace, inventario
}
```

**Tick (servidor):**
1. Si hay Magic Card en slot 0 y `storedXp < MAX_XP` → `infuseProgress++`.
2. Al llegar a `INFUSE_TIME`: consume 1 carta, `storedXp += xpCost(enchant)` (clamp a MAX_XP), reset progress, marca dirty + sync.
3. Si hay botella de cristal en slot 1 y `storedXp >= XP_PER_BOTTLE` y slot 2 admite output → cada `BOTTLE_TIME` ticks: consume 1 cristal, `storedXp -= XP_PER_BOTTLE`, añade 1 Bottle o' Enchanting al slot 2.

Sugerencias: `XP_PER_BOTTLE = 7` (como vanilla), `BOTTLE_TIME = 20`.

---

## 6. BlockEntityRenderer — fluido, luz, conector

```java
public class InfusorCoilRenderer implements BlockEntityRenderer<InfusorCoilBlockEntity> {
    @Override
    public void render(InfusorCoilBlockEntity be, float tickDelta, MatrixStack m,
                       VertexConsumerProvider vcp, int light, int overlay) {
        // (A) NIVEL DEL TANQUE — caja de fluido dentro del cuerpo (vidrio)
        float fill = be.getFillRatio();           // 0..1
        // cuerpo interior ≈ x[4..12] y[1..8] z[4..12]  (px/16)
        float yTop = 1f + fill * 7f;              // altura del líquido
        drawBox(m, vcp, 4/16f, 1/16f, 4/16f, 12/16f, yTop/16f, 12/16f,
                0xA8E030, 0xFF, /*emissive*/ false);

        // (B) LUZ DEL TAPÓN — solo al funcionar, emisiva (full-bright)
        if (be.isInfusing()) {
            int bright = LightmapTextureManager.MAX_LIGHT_COORDINATE; // 0xF000F0
            float pulse = 0.6f + 0.4f * MathHelper.sin((be.getWorld().getTime()+tickDelta)/4f);
            drawGlowingOrb(m, vcp, 8/16f, 18.5f/16f, 8/16f, 0.18f * pulse, 0xD8FF60, bright);
        }

        // (C) CONECTOR direccional — tubo de cobre desde la base hacia la cara bonded
        Direction face = be.getBondedFace();
        drawConnector(m, vcp, face, light, overlay);   // nozzle en la base, lado `face`

        // (D) opcional: goteo XP cuando isInfusing()
    }
}
```

Registro del renderer (cliente):
```java
BlockEntityRendererFactories.register(ModBlockEntities.INFUSOR_COIL, InfusorCoilRenderer::new);
```

**Notas de render:**
- Fluido: cuádrica translúcida, color `#A8E030`, cara superior un punto más clara (menisco).
- Luz tapón: usa `RenderLayer` emisivo / full-bright (`0xF000F0`) para que brille en la oscuridad. Pulso suave con `sin(time)`.
- Conector: nozzle corto de cobre en la base, en el lado de `bondedFace` (los bloques son adyacentes, así que basta un tramo corto donde se tocan).

---

## 7. GUI del módulo (176×200) — ya diseñada

La pantalla (slots + tanque + barra de progreso) está especificada en el documento de diseño. Slots:

| Slot | X, Y | Acepta |
|---|---|---|
| card-in | `26, 38` | Magic Card |
| glass-in | `26, 66` | botella de cristal vacía |
| xp-out | `70, 66` | (solo salida) |

Indicadores (render-only en el `Screen`): progress arrow `56,42` (22×8), XP tank `130,32` (14×56) con lectura `storedXp/MAX_XP`. Inventario jugador `8,118`, hotbar `8,176`.

> Reutiliza el patrón de tu `TransferTableScreenHandler`/`Screen` ya existentes. La textura de fondo `infusor_gui.png` (256×256) está en el paquete de texturas anterior.

---

## 8. Checklist de registro

- [ ] `ModBlocks.INFUSOR_COIL` (Block + BlockItem)
- [ ] `ModBlockEntities.INFUSOR_COIL` (BlockEntityType, asociado al bloque)
- [ ] `ModScreenHandlers.INFUSOR_COIL` (ScreenHandlerType)
- [ ] `HandledScreens.register(...)` para `InfusorCoilScreen`
- [ ] `BlockEntityRendererFactories.register(...)` para `InfusorCoilRenderer`
- [ ] `BlockRenderLayerMap` → translucent/cutout (por el vidrio)
- [ ] blockstate + modelos + texturas (✅ en este paquete)
- [ ] `lang/en_us.json` + `es_es.json`: `block.enchanttransfer.infusor_coil`
- [ ] loot table del bloque (que dropee el item)
- [ ] receta de crafteo (opcional)

---

## 9. Conexión con el sistema Hub & Spoke

- El Infusor es un **bloque propio** adyacente a la Transfer Table.
- Su `bonded_face` apunta a la cara de la mesa a la que está pegado.
- En el **Selector** (GUI L1 de la mesa) aparece su icono en la posición correspondiente (E en los mockups) con su status en vivo (nivel de tanque + progreso).
- Click en ese icono abre `InfusorCoilScreen` (equivale a click derecho en el bloque).

Ver el documento `selector-and-modules.html` para el layout completo del Selector y la nav row.
