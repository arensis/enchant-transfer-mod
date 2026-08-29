package net.alfonsormadrid.enchanttransfer.screens.selector;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.alfonsormadrid.enchanttransfer.network.RequestOpenGuiPayload;
import net.alfonsormadrid.enchanttransfer.screens.NavDotRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Selector screen (L1) — shown when the player right-clicks the Transfer Table.
 *
 * <p>Renders the hub at the centre, six module sockets around it, golden wires
 * between hub and connected sockets, and direction labels next to each socket
 * so the player can identify which face each one represents.  Hovered
 * clickable elements get a white ring as a hover affordance.
 */
@Environment(EnvType.CLIENT)
public class SelectorScreen extends Screen {

    private static final Identifier BG_TEXTURE =
            Identifier.of(EnchantTransferMod.MOD_ID, "textures/gui/container/selector_gui.png");

    /**
     * Actual block items used as the icon inside the disks.  Constructed
     * once and reused every frame — drawItem does its own model lookup so
     * we don't need to hold any sprite identifiers ourselves.
     */
    private static final ItemStack TABLE_ITEM =
            new ItemStack(EnchantTransferMod.TRANSFER_TABLE_BLOCK);
    private static final ItemStack COIL_ITEM  =
            new ItemStack(EnchantTransferMod.INFUSION_COIL_BLOCK);

    // Background content dimensions
    private static final int BG_W = 200, BG_H = 200;

    // Hub centre in content-local coords and hit-radius
    private static final int HUB_CX = 100, HUB_CY = 108, HUB_R = 16;

    // Socket centres (content-local) — keyed by the face they represent
    private static final Map<Direction, int[]> SOCKET = new EnumMap<>(Direction.class);
    static {
        SOCKET.put(Direction.NORTH, new int[]{100, 48});
        SOCKET.put(Direction.EAST,  new int[]{155, 76});
        SOCKET.put(Direction.UP,    new int[]{155, 140});
        SOCKET.put(Direction.SOUTH, new int[]{100, 168});
        SOCKET.put(Direction.DOWN,  new int[]{45,  140});
        SOCKET.put(Direction.WEST,  new int[]{45,  76});
    }

    private static final int SOCKET_R    = 16;  // click radius per socket
    private static final int ICON_SIZE   = 32;  // icon bounding box (32×32)
    private static final int ICON_RADIUS = 15;  // diameter 31, 1-px padding

    // Ocre / dark-gold tone sampled from selector_gui.png at the hub ring
    // (PNG pixel 0xB88A3A).  Used both for the wires AND the hub border so
    // they read as one continuous golden network instead of clashing tones.
    private static final int WIRE_COLOR     = 0xFFB88A3A;

    // Hub palette — dark-violet disk (slightly darker than the empty bg)
    // with an ocre-gold border matching the PNG ring + the actual Transfer
    // Table block rendered as the icon.
    private static final int HUB_BG_COL     = 0xFF0F0418;
    private static final int HUB_BORDER_COL = 0xFFB88A3A;

    // Empty-socket palette — sampled from the original socket_empty sprite:
    //   dark violet base   0xFF1A0A26
    //   lighter desaturated border 0xFF3A2E4A  ← visible against bg, less saturated than the plus
    //   purple "+" glyph   0xFFB46AC8
    private static final int EMPTY_BG_COL     = 0xFF1A0A26;
    private static final int EMPTY_BORDER_COL = 0xFF3A2E4A;
    private static final int EMPTY_PLUS_COL   = 0xFFB46AC8;

    // Occupied-socket palette — the disk INTERIOR is a single dark muted
    // purple ("pastel oscuro") shared by both idle and processing so the
    // unfilled portion of the tank reads as empty space rather than a
    // saturated green/amber wash.  State is still conveyed by the BORDER
    // colour (green = idle, amber = processing) and by the live fill.
    private static final int TANK_EMPTY_COL       = 0xFF3D2A52;
    private static final int MODULE_IDLE_BORDER   = 0xFF1F8044;
    private static final int MODULE_ACTIVE_BORDER = 0xFF8B6914;
    private static final int FLUID_COLOR          = 0xFF50F03C;

    // Hover ring colour
    private static final int HOVER_COL = 0xFFFFFFFF;

    // Infusion-progress arc colour — clean energetic blue.  Drawn as a 2-px
    // thick ring just OUTSIDE the disk, sweeping clockwise from 12 o'clock
    // proportional to the coil's progress / maxProgress ratio.
    private static final int PROGRESS_COL = 0xFF40A0FF;
    // Inner / outer half-extents of the progress ring (measured from the
    // socket centre).  The disk ends at ICON_RADIUS = 15, so the ring sits
    // at 16-17 and reads as a thin halo around the icon.
    private static final int PROGRESS_R_OUTER = 17;
    private static final int PROGRESS_R_INNER = 16;

    // "+" glyph dimensions inside the empty socket (smaller than before)
    private static final int PLUS_ARM_RADIUS = 5;
    private static final int PLUS_THICKNESS  = 2;

    // Radial offset of direction labels from each socket centre.  The label is
    // placed OUTSIDE the socket in the direction away from the hub so it
    // doesn't overlap the disk or its hover ring.
    private static final int LABEL_OFFSET = SOCKET_R + 5;

    private final BlockPos tablePos;
    private int bgX, bgY;  // top-left of background in screen coords

    /** Title text — shown in the parchment banner at the top of the screen. */
    private static final Text TITLE_TEXT =
            Text.translatable("enchanttransfer.gui.selector.title");
    /** Parchment-brown ink colour shared by every screen's title text. */
    private static final int TITLE_COL = 0xFF4A2811;
    /** Y offset (content-local) of the title inside the parchment banner. */
    private static final int TITLE_Y = 6;

    public SelectorScreen(BlockPos tablePos) {
        super(TITLE_TEXT);
        this.tablePos = tablePos;
    }

    @Override
    protected void init() {
        bgX = (width  - BG_W) / 2;
        bgY = (height - BG_H) / 2;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // 1 — Static background (hub + 6 socket circle outlines baked into PNG)
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED,
                BG_TEXTURE, bgX, bgY, 0f, 0f, BG_W, BG_H, 256, 256);

        // Parchment-brown title centred in the banner of the background.
        if (client != null) {
            int tw = client.textRenderer.getWidth(TITLE_TEXT);
            ctx.drawText(client.textRenderer, TITLE_TEXT,
                    bgX + (BG_W - tw) / 2, bgY + TITLE_Y, TITLE_COL, false);
        }

        if (client == null || client.world == null) {
            super.render(ctx, mouseX, mouseY, delta);
            return;
        }

        // 2 — Wires (only between hub and occupied sockets)
        for (Map.Entry<Direction, int[]> e : SOCKET.entrySet()) {
            int[] s = e.getValue();
            BlockPos nbPos = tablePos.offset(e.getKey());
            if (client.world.getBlockState(nbPos).getBlock() == EnchantTransferMod.INFUSION_COIL_BLOCK) {
                drawWireHubToSocket(ctx, s[0], s[1]);
            }
        }

        // 3 — Hub disk on top of wires
        drawHub(ctx, mouseX, mouseY);

        // 4 — Socket icons
        for (Map.Entry<Direction, int[]> e : SOCKET.entrySet()) {
            int[] s = e.getValue();
            BlockPos nbPos = tablePos.offset(e.getKey());
            int iconX = bgX + s[0] - ICON_SIZE / 2;
            int iconY = bgY + s[1] - ICON_SIZE / 2;
            boolean connected =
                    client.world.getBlockState(nbPos).getBlock() == EnchantTransferMod.INFUSION_COIL_BLOCK;
            if (connected) {
                drawModuleIcon(ctx, nbPos, iconX, iconY, mouseX, mouseY);
            } else {
                drawEmptySocket(ctx, iconX, iconY);
            }
        }

        // 5 — Direction labels (drawn last so they sit on top of every disk)
        for (Map.Entry<Direction, int[]> e : SOCKET.entrySet()) {
            int[] s = e.getValue();
            drawDirectionLabel(ctx, e.getKey(), s[0], s[1]);
        }

        super.render(ctx, mouseX, mouseY, delta);

        // 6 — Hover tooltips for clickable elements
        drawHoverTooltip(ctx, mouseX, mouseY);
    }

    // ── Drawing helpers ───────────────────────────────────────────────────────

    /**
     * Draws a 2-pixel golden wire from the hub edge to the socket edge.
     * Uses the content-local socket centre {@code (sx, sy)}.
     */
    private void drawWireHubToSocket(DrawContext ctx, int sx, int sy) {
        double dx = sx - HUB_CX, dy = sy - HUB_CY;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;

        int x1 = (int) Math.round(bgX + HUB_CX + dx / len * HUB_R);
        int y1 = (int) Math.round(bgY + HUB_CY + dy / len * HUB_R);
        int x2 = (int) Math.round(bgX + sx - dx / len * SOCKET_R);
        int y2 = (int) Math.round(bgY + sy - dy / len * SOCKET_R);

        int steps = Math.max(1, (int) Math.round(Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1))));
        for (int i = 0; i <= steps; i++) {
            int px = x1 + (x2 - x1) * i / steps;
            int py = y1 + (y2 - y1) * i / steps;
            ctx.fill(px - 1, py - 1, px + 1, py + 1, WIRE_COLOR);
        }
    }

    /**
     * Hub disk: dark-violet bg + gold border + the actual Transfer Table
     * block rendered as a 16×16 item icon centred in the disk.  The item
     * model gives the player a faithful preview of the block they'd open
     * by clicking, instead of an abstract glyph.
     */
    private void drawHub(DrawContext ctx, int mouseX, int mouseY) {
        int cx = bgX + HUB_CX;
        int cy = bgY + HUB_CY;
        NavDotRenderer.diskWithBorder(ctx, cx, cy, HUB_R, HUB_BG_COL, HUB_BORDER_COL);
        ctx.drawItem(TABLE_ITEM, cx - 8, cy - 8);
        if (hovered(mouseX, mouseY, cx, cy, HUB_R)) {
            NavDotRenderer.ring(ctx, cx, cy, HUB_R + 1, HOVER_COL);
        }
    }

    /**
     * State-coloured disk + bordered rim + live XP-fill clipped to the disk
     * + the actual Infusion Coil block rendered as a 16×16 item icon in the
     * centre.  The item makes the module type instantly recognisable; the
     * disk colour + fill still convey the live state at a glance.
     */
    private void drawModuleIcon(DrawContext ctx, BlockPos coilPos,
                                int iconX, int iconY, int mouseX, int mouseY) {
        boolean processing = false;
        float fillRatio       = 0f;
        float progressFraction = 0f;
        if (client != null && client.world != null) {
            var be = client.world.getBlockEntity(coilPos);
            if (be instanceof InfusionCoilBlockEntity coil) {
                fillRatio  = coil.getFillRatio();
                processing = coil.isProcessing();
                int p   = coil.getProgress();
                int max = coil.getMaxProgress();
                if (max > 0) progressFraction = (float) p / max;
            }
        }
        int cx = iconX + ICON_SIZE / 2;
        int cy = iconY + ICON_SIZE / 2;
        int border = processing ? MODULE_ACTIVE_BORDER : MODULE_IDLE_BORDER;

        NavDotRenderer.diskWithBorder(ctx, cx, cy, ICON_RADIUS, TANK_EMPTY_COL, border);
        NavDotRenderer.diskFillFromBottom(ctx, cx, cy, ICON_RADIUS - 1, fillRatio, FLUID_COLOR);
        ctx.drawItem(COIL_ITEM, cx - 8, cy - 8);

        // Progress arc — 2-px-thick clockwise ring just outside the disk.
        // Only drawn while the coil is processing AND has actually started
        // the current cycle (progressFraction > 0) so the indicator stays
        // off during empty/idle frames.
        if (processing && progressFraction > 0f) {
            NavDotRenderer.arc(ctx, cx, cy,
                    PROGRESS_R_OUTER, PROGRESS_R_INNER,
                    progressFraction, PROGRESS_COL);
        }

        if (hovered(mouseX, mouseY, cx, cy, SOCKET_R)) {
            NavDotRenderer.ring(ctx, cx, cy, ICON_RADIUS + 1, HOVER_COL);
        }
    }

    /**
     * Empty-socket marker: dark violet disk with a thin contrasting rim and a
     * smaller centred "+" glyph in lighter violet.  Not clickable, so no
     * hover ring.
     */
    private void drawEmptySocket(DrawContext ctx, int iconX, int iconY) {
        int cx = iconX + ICON_SIZE / 2;
        int cy = iconY + ICON_SIZE / 2;
        NavDotRenderer.diskWithBorder(ctx, cx, cy, ICON_RADIUS, EMPTY_BG_COL, EMPTY_BORDER_COL);
        NavDotRenderer.plus(ctx, cx, cy, PLUS_ARM_RADIUS, PLUS_THICKNESS, EMPTY_PLUS_COL);
    }

    /**
     * Draws the direction tag (N/S/E/W/↑/↓) just outside the socket, on
     * the radial axis from the hub.  Uses {@link DrawContext#drawTextWithShadow}
     * so it stays readable on any background.  Colour is a desaturated dark
     * purple derived from the plus glyph so labels share the empty-socket
     * palette without screaming over the dots.
     */
    private void drawDirectionLabel(DrawContext ctx, Direction dir, int sx, int sy) {
        if (client == null) return;
        Text label = NavDotRenderer.dirLabel(dir);
        double dx = sx - HUB_CX, dy = sy - HUB_CY;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1) return;
        int lx = (int) Math.round(bgX + sx + dx / len * LABEL_OFFSET);
        int ly = (int) Math.round(bgY + sy + dy / len * LABEL_OFFSET);
        int tw = client.textRenderer.getWidth(label);
        int fh = client.textRenderer.fontHeight;
        ctx.drawTextWithShadow(client.textRenderer, label,
                lx - tw / 2, ly - fh / 2, 0xFF8240A0);
    }

    private static boolean hovered(int mx, int my, int cx, int cy, int r) {
        int dx = mx - cx, dy = my - cy;
        return dx * dx + dy * dy <= r * r;
    }

    // ── Hover tooltips ────────────────────────────────────────────────────────

    private void drawHoverTooltip(DrawContext ctx, int mouseX, int mouseY) {
        if (client == null || client.world == null) return;
        int hubX = bgX + HUB_CX, hubY = bgY + HUB_CY;

        if (hovered(mouseX, mouseY, hubX, hubY, HUB_R)) {
            ctx.drawTooltip(client.textRenderer,
                    Text.translatable("block.enchanttransfer.transfer_table_block"),
                    mouseX, mouseY);
            return;
        }
        for (Map.Entry<Direction, int[]> e : SOCKET.entrySet()) {
            int[] s = e.getValue();
            int cx = bgX + s[0], cy = bgY + s[1];
            if (!hovered(mouseX, mouseY, cx, cy, SOCKET_R)) continue;
            Direction dir = e.getKey();
            String dirName = dir.name();
            BlockPos nbPos = tablePos.offset(dir);
            if (client.world.getBlockState(nbPos).getBlock() != EnchantTransferMod.INFUSION_COIL_BLOCK) {
                ctx.drawTooltip(client.textRenderer,
                        Text.literal("§7Empty (" + dirName + ")"),
                        mouseX, mouseY);
                return;
            }
            String stateS = "§aIdle";
            String levelS = "?";
            var be = client.world.getBlockEntity(nbPos);
            if (be instanceof InfusionCoilBlockEntity coil) {
                stateS = coil.isProcessing() ? "§eInfusing" : "§aIdle";
                levelS = coil.getStored() + " / " + coil.getCapacity() + " XP";
            }
            ctx.drawTooltip(client.textRenderer, List.of(
                    Text.literal("§fInfusion Coil §7(" + dirName + ")"),
                    Text.literal("§7" + levelS),
                    Text.literal(stateS)
            ), mouseX, mouseY);
            return;
        }
    }

    // ── Input handling ────────────────────────────────────────────────────────

    @Override
    public boolean mouseClicked(Click click, boolean down) {
        if (click.button() != 0 || client == null) return super.mouseClicked(click, down);

        int lx = (int) click.x() - bgX;
        int ly = (int) click.y() - bgY;

        // Hub click → open Transfer Table core screen
        if (dist2(lx, ly, HUB_CX, HUB_CY) <= HUB_R * HUB_R) {
            ClientPlayNetworking.send(new RequestOpenGuiPayload(tablePos));
            close();
            return true;
        }

        // Socket click → open module's screen if occupied
        if (client.world != null) {
            for (Map.Entry<Direction, int[]> e : SOCKET.entrySet()) {
                int[] s = e.getValue();
                if (dist2(lx, ly, s[0], s[1]) <= SOCKET_R * SOCKET_R) {
                    BlockPos nbPos = tablePos.offset(e.getKey());
                    if (client.world.getBlockState(nbPos).getBlock()
                            == EnchantTransferMod.INFUSION_COIL_BLOCK) {
                        ClientPlayNetworking.send(new RequestOpenGuiPayload(nbPos));
                        close();
                        return true;
                    }
                    break;
                }
            }
        }

        return super.mouseClicked(click, down);
    }

    @Override
    public boolean shouldPause() { return false; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static int dist2(int ax, int ay, int bx, int by) {
        int dx = ax - bx, dy = ay - by;
        return dx * dx + dy * dy;
    }
}
