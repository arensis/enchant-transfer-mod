# Transfer Table — Handoff para Claude Code

Bloque central del sistema Hub & Spoke (Fabric 1.21.1, `net.alfonsormadrid.enchanttransfer`). Cubo 16³ con **embudo escalonado** excavado en cada cara, convergiendo hacia un **núcleo de diamante azul emisivo**.

---

## 1. Estructura visual (de fuera hacia dentro)

| Capa | Material | Color textura | Inset | Grosor |
|---|---|---|---|---|
| 1 | Oro | `#d2962a` | 0 | 1.5 px |
| 2 | Redstone | `#a01a14` | 1.5 | 1.5 px |
| 3 | Carbón (negro) | `#22222a` | 3 | 1.5 px |
| Núcleo | Diamante azul | `#46afeb` → `#fff` | 5 | 6³ (emisivo) |

> La receta de crafteo coincide con la estructura: fila de **3 oro**, fila de **3 redstone**, y abajo **carbón–diamante–carbón** (los inputs y el núcleo).

---

## 2. Qué es estático vs. código

| Parte | Cómo | Archivo |
|---|---|---|
| Capas del embudo (oro/redstone/carbón) | **Modelo JSON estático** | `models/block/transfer_table.json` ✅ |
| Texturas de cada capa | **PNG 16×16** | `textures/block/table_*.png` ✅ |
| **Brillo del núcleo** (cara azul iluminada por completo desde cualquier ángulo) | **Código** — `BlockEntityRenderer` con planos emisivos full-bright | `TransferTableRenderer.java` |

> **Clave:** el modelo JSON ya dibuja el núcleo como cubo sólido azul. Para que **cada cara se vea totalmente iluminada desde cualquier dirección** (y no como una raya plana), el `TransferTableRenderer` dibuja un **plano emisivo (full-bright) en el fondo de cada uno de los 6 embudos**, orientado hacia afuera de su cara. Esto reproduce el efecto del preview.

---

## 3. Archivos incluidos

```
src/main/resources/assets/enchanttransfer/
├── blockstates/transfer_table.json        ✅ (variante única)
├── models/block/transfer_table.json       ✅ (embudo, 37 elementos)
├── models/item/transfer_table.json        ✅ (parent → block)
└── textures/block/
    ├── table_gold.png                      ✅
    ├── table_redstone.png                  ✅
    ├── table_carbon.png                     ✅
    └── table_core.png                       ✅ (azul emisivo)
```

---

## 4. BlockEntityRenderer — núcleo emisivo

```java
public class TransferTableRenderer implements BlockEntityRenderer<TransferTableBlockEntity> {
    @Override
    public void render(TransferTableBlockEntity be, float tickDelta, MatrixStack m,
                       VertexConsumerProvider vcp, int light, int overlay) {
        int FULL = LightmapTextureManager.MAX_LIGHT_COORDINATE; // 0xF000F0 (full-bright)

        // Plano emisivo en el fondo de cada uno de los 6 embudos.
        // Cada plano ≈ 6×6 px, centrado en su cara, en z del fondo del embudo (~5/16).
        for (Direction dir : Direction.values()) {
            m.push();
            // orientar el plano hacia 'dir' y empujarlo al fondo del embudo
            orientToFace(m, dir);            // rota para mirar hacia afuera de la cara
            float pulse = 0.85f + 0.15f * MathHelper.sin((be.getWorld().getTime()+tickDelta)/8f);
            drawEmissiveQuad(m, vcp, 6/16f, 6/16f, CORE_TEXTURE, FULL, pulse);
            m.pop();
        }
    }
}
```

Registro (cliente):
```java
BlockEntityRendererFactories.register(ModBlockEntities.TRANSFER_TABLE, TransferTableRenderer::new);
```

**Notas:**
- Usa `RenderLayer.getText(table_core)` o un layer emisivo (`RenderLayer.getEyes(...)` / `getBeaconBeam`) para que brille en la oscuridad.
- El pulso (`sin(time)`) da el latido suave del núcleo. Opcional.
- Como el modelo ya tiene el cubo-núcleo sólido, los planos emisivos solo añaden el "lit" full-bright sobre las caras visibles del embudo.

---

## 5. Geometría del modelo (referencia)

El embudo se aproxima con **anillos concéntricos** (arriba/abajo) + **paredes laterales** por capa, dejando el hueco escalonado hacia el centro. El núcleo es un cubo `[5,5,5]→[11,11,11]`. Todo está en `transfer_table.json` (37 elementos) — Claude Code puede afinar UVs en Blockbench si quiere.

> Si prefieres una malla más limpia/curva, este modelo es importable en **Blockbench** para retoques. Pero tal cual ya es válido para el juego.

---

## 6. Checklist de registro

- [ ] `ModBlocks.TRANSFER_TABLE` (Block + BlockItem) — ya existe en tu mod; actualiza su modelo/blockstate con estos assets
- [ ] `ModBlockEntities.TRANSFER_TABLE` (si no lo tiene ya, para el renderer del núcleo)
- [ ] `BlockEntityRendererFactories.register(...)` → `TransferTableRenderer`
- [ ] Render layer translucent/emisivo para el núcleo
- [ ] Copiar blockstate + modelos + texturas (✅ incluidos)
- [ ] `lang`: `block.enchanttransfer.transfer_table`
- [ ] Receta: 3 oro / 3 redstone / carbón-diamante-carbón

---

## 7. Relación con los módulos

Esta mesa es el **Hub (L2/Core)**. Los módulos (Infusor Coil, etc.) se acoplan a sus 6 caras. El Selector (GUI L1) muestra los sockets alrededor de este núcleo. Ver handoff del Infusor Coil para el flujo completo.
