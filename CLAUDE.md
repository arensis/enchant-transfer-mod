# enchant-transfer-mod — Claude context

Fabric mod for Minecraft **26.2**.  
**Mojang (official) mappings** · Fabric API `0.158.0+26.2` · Fabric Loom `1.17` · Java **25**

> Build/run with JDK 25 as `JAVA_HOME` (the daemon default `java` may be newer and
> unsupported by Gradle 9.5):
> `JAVA_HOME=…/jdk-25… ./gradlew build` / `runClient`.
> Migrated from 1.21.11 + Yarn — see the `feature/Update26_2` history. Yarn→Mojmap
> remap was done with Loom's `migrateMappings` task.

---

## Architecture

**Hub & Spoke**: `TransferTableBlock` (core) + `InfusionCoilBlock` (first module).  
Modules are placed adjacent to the table; the server scans neighbours each tick.

### Key classes
| Class | Notes |
|---|---|
| `EnchantTransferMod` | Registry (blocks, items, block entities, screen handlers) |
| `EnchantTransferClientMod` | BER registration, screen registration, S2C listeners |
| `TransferTableBlockEntity` | Hub – attaches/detaches modules, `attachModule(face, module)` |
| `InfusionCoilBlockEntity` | Module – consumes Magic Cards → XP tank → glass bottles |
| `InfusionCoilRenderer` | BER: fluid fill, cap glow, copper nozzle stub |
| `TransferTableRenderer` | BER: pulsing blue core box |
| `SelectorScreen` | L1 GUI (plain `Screen`); opened via `OpenSelectorPayload` S2C |
| `InfusionCoilScreen` | L2 GUI (`AbstractContainerScreen`) |
| `TransferTableScreen` | L2 GUI (`AbstractContainerScreen`) |

---

## Critical 26.2 API notes

Mojang mappings. 26.2 replaced the immediate-mode render/GUI APIs with a **deferred
render-state** pipeline — this is the crux of most of the code here.

- **BER (`BlockEntityRenderer<T, S>`)** — override `createRenderState()`,
  `extractRenderState(entity, state, tickDelta, cameraPos, crumbling)` (game thread),
  and `submit(state, PoseStack, SubmitNodeCollector, CameraRenderState)` (render thread).
  Geometry via `queue.submitCustomGeometry(matrices, layer, (entry, vc) → ...)`.
  `CameraRenderState` lives in `net.minecraft.client.renderer.state.level`.
- **Render types** — `RenderTypes` (package `net.minecraft.client.renderer.rendertype`):
  `debugFilledBox()` for `POSITION_COLOR` boxes; `entityCutout(id)` is the **no-cull**
  cutout (culled variant is `entityCutoutCull`).
- **GUI (`Screen` / `AbstractContainerScreen`)** — no more `GuiGraphics`; draw via
  **`GuiGraphicsExtractor`**. Override points: `extractRenderState(gfx, mx, my, delta)`
  (was `render`), `extractBackground(gfx, mx, my, delta)` (was `renderBg`, **call
  `super`**), `extractLabels(gfx, mx, my)` (was `renderLabels`), `extractTooltip(gfx,
  mx, my)` (was `renderTooltip`). Methods: `text()` (was `drawString`), `item()` (was
  `renderItem`); `blit`, `fill`, `setTooltipForNextFrame`, `pose()` (a 2D
  `Matrix3x2fStack`) unchanged. Tooltips render in a deferred final pass, so drawing
  extra overlays in `extractRenderState` after `super` still sits under them.
- **`imageWidth`/`imageHeight` are `final`** — pass them to
  `super(handler, inv, title, w, h)` in the constructor, don't assign in `init()`.
- **Block render layer** is data-driven: `"render_type": "minecraft:translucent"` in the
  block model JSON (Fabric's `BlockRenderLayerMap` was removed in 26.2).
- **Screen handlers / menus** — `ExtendedMenuType` + `ExtendedMenuProvider`
  (`net.fabricmc.fabric.api.menu.v1`; replace old `screenhandler.v1`); ship the coil
  `BlockPos` via `BlockPos.STREAM_CODEC` at screen-open time.
- **Creative tab** — `FabricCreativeModeTab.builder()` +
  `CreativeModeTabEvents.modifyOutputEvent(key)` (`creativetab.v1`; replaces
  `itemgroup.v1`).
- **Networking** — `PayloadTypeRegistry.clientboundPlay()` / `.serverboundPlay()`
  (were `playS2C` / `playC2S`). Open a screen client-side with
  `Minecraft.setScreenAndShow(screen)` (was `setScreen`).
- **Misc** — `Vec3.atCenterOf(blockPos)` (was `BlockPos.getCenter()`);
  `Screen.mouseClicked(MouseButtonEvent click, boolean down)` uses
  `click.x()/.y()/.button()`.
- **`depends`** in `fabric.mod.json` is `fabric-api` (the 26.2 bundle id; the legacy
  `fabric` id is gone).

---

## InfusionCoilBlockEntity — mechanics

- 3 slots: `SLOT_CARD_IN=0`, `SLOT_BOTTLE_IN=1`, `SLOT_BOTTLE_OUT=2`
- Tank: 1000 XP capacity
- `TICKS_PER_INFUSION = 200` (~10 s per card)
- `XP_PER_BOTTLE = 7`
- `XpConversionService.convertSingle()`: returns `MIN_POINTS_PER_CARD=5` for unenchanted cards, more for enchanted ones
- `toUpdatePacket()` overridden → sends state to nearby clients  
- Periodic `markDirty()` every 20 ticks while processing → keeps client-side BE in sync for Selector screen

---

## Render layers

Set data-side via `"render_type"` in the block **model** JSON (not code — `BlockRenderLayerMap`
was removed in 26.2).

| Block | `render_type` | Reason |
|---|---|---|
| `INFUSION_COIL_BLOCK` (`infusor_coil.json`, `infusor_coil_active.json`) | `minecraft:translucent` | `infusor_glass.png` has 196 semi-transparent pixels; cutout renders them opaque hiding the fluid |
| `ZINC_SMELTER_BLOCK` (`zinc_smelter.json`, `zinc_smelter_lit.json`) | `minecraft:translucent` | glass mirilla lets the BER fire glow show through |
| `TRANSFER_TABLE_BLOCK` | default (solid) | |

---

## Infusor textures

- `infusor_glass.png`: 16×16, RGBA. 196 semi-transparent pixels, 60 opaque, 0 transparent → must use TRANSLUCENT layer.
- `module_infusor.png`: 32×96 sprite sheet, 3 frames of 32×32. y=0 disconnected, y=32 idle, y=64 active.
- `socket_empty.png`: 32×32.

---

## GUI layouts

### Selector (`selector_gui.png`, 200×200 in 256×256)
Hub centre: `(100, 108)`, r=16.  
Socket centres (content-local): N(100,48) E(155,76) UP(155,140) S(100,168) DOWN(45,140) W(45,76), r=16.  
Wires drawn from hub edge to socket edge (golden). Module icons: `module_infusor.png` + fluid fill overlay. Empty sockets: `socket_empty.png`.

### Infusion Coil (`infusor_gui.png`, 176×200 in 256×256)
Slots: card-in(26,38), glass-in(26,66), xp-out(70,66).  
Progress arrow: pos(56,42), 22×8 px, arrow texture at u=176 v=0 of `infusor_gui.png`.  
XP tank: pos(130,32), 14×56 px, fills bottom-up.

### Nav row (both L2 screens)
**Fixed 7 positions** at GUI-local `x = 52 + i*12`, `y = 21`.  
i=0 = hub (golden), i=1..6 = module slots mapped to `Direction.values()[i-1]`.  
Empty → dim dot. Active screen → filled dot + cyan ring (border at radius 5).

---

## Known architecture constraints

- `cachedCoreFace` in `InfusionCoilBlockEntity` is server-only; never synced to client.  
  The client BER scans adjacent block states directly in `extractRenderState()`.
- The `InfusionCoilBlock` must have `.nonOpaque()` in settings to prevent Minecraft culling the top face of the block it sits on (inset model has corner gaps).
