# enchant-transfer-mod — Claude context

Fabric mod for Minecraft **1.21.11**.  
Yarn mappings `1.21.11+build.3` · Fabric API `0.141.4+1.21.11`

---

## Architecture

**Hub & Spoke**: `TransferTableBlock` (core) + `InfusionCoilBlock` (first module).  
Modules are placed adjacent to the table; the server scans neighbours each tick.

### Key classes
| Class | Notes |
|---|---|
| `EnchantTransferMod` | Registry (blocks, items, block entities, screen handlers) |
| `EnchantTransferClientMod` | BER registration, render layers, S2C listeners |
| `TransferTableBlockEntity` | Hub – attaches/detaches modules, `attachModule(face, module)` |
| `InfusionCoilBlockEntity` | Module – consumes Magic Cards → XP tank → glass bottles |
| `InfusionCoilRenderer` | BER: fluid fill, cap glow, copper nozzle stub |
| `TransferTableRenderer` | BER: pulsing blue core box |
| `SelectorScreen` | L1 GUI (plain `Screen`); opened via `OpenSelectorPayload` S2C |
| `InfusionCoilScreen` | L2 GUI (`HandledScreen`) |
| `TransferTableScreen` | L2 GUI (`HandledScreen`) |

---

## Critical 1.21.11 API notes

- **BER API** uses `BlockEntityRenderer<T, S>` with two type params.  
  - `updateRenderState()` runs on game thread; `render()` on render thread.  
  - Geometry submitted via `OrderedRenderCommandQueue.submitCustom(matrices, layer, (entry, vc) → ...)`.
- **Render layer for colored boxes**: `RenderLayers.debugFilledBox()` — uses `POSITION_COLOR` shader, no UV/Normal/Overlay needed. Avoids Apple Silicon shader warnings from `eyes` layer.
- **Screen blur crash**: `Screen.renderWithTooltip()` in 1.21.11 applies blur before `render()`. Never call `renderBackground()` inside `render()` on a plain `Screen` subclass — only from `HandledScreen`.
- **`BlockPos.PACKET_CODEC`** used with `ExtendedScreenHandlerType` to send coil position to client at screen-open time.
- **`Screen.mouseClicked(Click click, boolean down)`** — use `click.x()`, `click.y()`, `click.button()`.

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

| Block | Layer | Reason |
|---|---|---|
| `INFUSION_COIL_BLOCK` | `TRANSLUCENT` | `infusor_glass.png` has 196 semi-transparent pixels; CUTOUT renders them opaque hiding the fluid |
| `TRANSFER_TABLE_BLOCK` | default (SOLID) | |

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
  The client BER scans adjacent block states directly in `updateRenderState()`.
- The `InfusionCoilBlock` must have `.nonOpaque()` in settings to prevent Minecraft culling the top face of the block it sits on (inset model has corner gaps).
