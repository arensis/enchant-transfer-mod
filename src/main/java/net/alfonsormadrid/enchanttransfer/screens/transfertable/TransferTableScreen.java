package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.alfonsormadrid.enchanttransfer.network.RequestOpenGuiPayload;
import net.alfonsormadrid.enchanttransfer.screens.NavDotRenderer;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.MagicCardSlot;
import net.minecraft.screen.slot.Slot;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import java.util.List;

/**
 * Core/L2 screen for the Transfer Table.
 *
 * <p>Renders the static background ({@code core_gui.png}) and a dynamic nav row
 * that shows the hub (always present) plus one dot per attached module, in the
 * order they appear by Direction ordinal.  Clicking a module dot sends a
 * {@link RequestOpenGuiPayload} to the server to open that module's own screen.
 */
@Environment(EnvType.CLIENT)
public class TransferTableScreen extends HandledScreen<TransferTableScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(EnchantTransferMod.MOD_ID, "textures/gui/container/core_gui.png");

    // Background content size (176×200 inside a 256×256 PNG)
    private static final int BG_W = 176, BG_H = 200;

    // ── Nav row ───────────────────────────────────────────────────────────────
    // The PNG background bakes exactly 7 circle outlines at fixed GUI-local x
    // positions: 52, 64, 76, 88, 100, 112, 124 (y=21, r=4, spacing=12).
    // i=0 is always the hub; i=1..6 map to Direction ordinals 0..5 (module slots).
    private static final int NAV_Y       = 21;
    private static final int NAV_SLOTS   = 7;
    private static final int NAV_STEP    = 12;
    private static final int NAV_X0      = 52;   // GUI-local x of the first circle (i=0 hub)
    private static final int NAV_DOT     = 3;    // half-size of filled dot (7×7 px fits inside r=4 circle)
    private static final int NAV_RING    = 5;    // half-size of active ring border

    // colours
    // Module dot palette — matches the Selector screen so the same coil is
    // read identically in both GUIs:
    //   • interior  = dark pastel purple (empty tank)
    //   • fluid     = opaque lime (filled tank), bottom-up
    //   • border    = darker green (idle) or darker amber (processing)
    private static final int COL_MODULE_BORDER_IDLE   = 0xFF1F8044;
    private static final int COL_MODULE_BORDER_ACTIVE = 0xFF8B6914;
    private static final int COL_TANK_EMPTY           = 0xFF3D2A52; // dark pastel purple
    private static final int COL_FLUID                = 0xFF50F03C; // opaque lime
    private static final int COL_RING_CYAN            = 0xFF00FFEE; // cyan ring — active screen
    // Hover palette — inverts the dot to white with a translucent lime fill.
    // Matches InfusionCoilScreen so both navbars feel identical on hover.
    private static final int COL_ACTIVE                = 0xFFFFFFFF;
    private static final int COL_TANK_FILL_ACTIVE      = 0xCC50F03C;

    // Hub "target" colours — concentric rings (outermost → centre):
    //   gold border, then red, black, blue.  Differentiates the hub at a
    //   glance from the module dots without needing a sprite at 7×7 px.
    private static final int COL_HUB_GOLD  = 0xFFFFD700;
    private static final int COL_HUB_RED   = 0xFFCC2030;
    private static final int COL_HUB_BLACK = 0xFF101020;
    private static final int COL_HUB_BLUE  = 0xFF2860E0;
    // Empty-socket palette — sampled from the original socket_empty sprite,
    // matches Selector screen.  Border is lighter than the bg and less
    // saturated than the plus, so the disk reads against the GUI background
    // without dominating the plus glyph.
    private static final int EMPTY_BG_COL        = 0xFF1A0A26;
    private static final int EMPTY_BORDER_COL    = 0xFF3A2E4A;
    private static final int EMPTY_PLUS_COL      = 0xFFB46AC8;
    // Hover ring colour for clickable dots
    private static final int HOVER_COL           = 0xFFFFFFFF;
    // Direction label colour — desaturated dark purple derived from the plus
    private static final int LABEL_COL           = 0xFF8240A0;

    // Small "+" glyph: 3×3 px with 1 px padding inside the 7-px dot
    private static final int PLUS_ARM_RADIUS = 1;
    private static final int PLUS_THICKNESS  = 1;

    // Direction-label font scale (0.5 → ~3-4 px tall, clean 2:1 downsample)
    private static final float LABEL_SCALE = 0.5f;
    // Pixel gap between the dot's bottom edge and the label's top edge.
    private static final int   LABEL_GAP   = 4;

    public TransferTableScreen(TransferTableScreenHandler handler,
                               PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    /** Parchment-brown ink colour shared with the other screens. */
    private static final int TITLE_COL = 0xFF4A2811;

    @Override
    protected void init() {
        backgroundWidth  = BG_W;
        backgroundHeight = BG_H;
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    /**
     * Override the vanilla foreground draw so:
     *   • only the screen title is rendered (the default "Inventory" label
     *     is omitted because there's no spare row for it in either GUI),
     *   • the title uses parchment-brown ink to match the scroll-banner
     *     aesthetic of the background PNG.
     */
    @Override
    protected void drawForeground(DrawContext ctx, int mouseX, int mouseY) {
        ctx.drawText(textRenderer, title, titleX, titleY, TITLE_COL, false);
    }

    /** Ghost-icon hint shown on empty MagicCard inputs (combine slots). */
    private static final Identifier GHOST_CARD =
            Identifier.of(EnchantTransferMod.MOD_ID, "textures/item/magic_card_item.png");
    /** ARGB tint for ghost icons — 38 % white opacity, matches InfusionCoilScreen. */
    private static final int GHOST_TINT = 0x60FFFFFF;

    @Override
    protected void drawBackground(DrawContext ctx, float delta, int mouseX, int mouseY) {
        int gx = (width  - backgroundWidth)  / 2;
        int gy = (height - backgroundHeight) / 2;
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED,
                TEXTURE, gx, gy, 0f, 0f, backgroundWidth, backgroundHeight, 256, 256);

        // Faded card silhouettes on the empty combine inputs.  Done in
        // drawBackground so super.render() can later paint a real card on
        // top once the player drops one in.  Iterating handler.slots and
        // filtering by class avoids hardcoded slot indices — adding more
        // MagicCardSlot inputs in the future is automatic.
        for (Slot slot : handler.slots) {
            if (slot instanceof MagicCardSlot && !slot.hasStack()) {
                ctx.drawTexture(RenderPipelines.GUI_TEXTURED, GHOST_CARD,
                        gx + slot.x, gy + slot.y,
                        0f, 0f, 16, 16, 16, 16, GHOST_TINT);
            }
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        // Draw nav row BEFORE the tooltip so the hover tooltip floats on top
        // of the dots instead of being painted over by them.
        drawNavRow(ctx, mouseX, mouseY);
        drawMouseoverTooltip(ctx, mouseX, mouseY);
    }

    // ── Nav row ───────────────────────────────────────────────────────────────

    /**
     * Checks which of the 6 Direction slots have a connected Infusion Coil.
     * Returns a boolean array indexed by {@link Direction#ordinal()}.
     */
    private boolean[] connectedSlots() {
        boolean[] result = new boolean[6];
        if (client == null || client.world == null) return result;
        BlockPos tablePos = handler.getTablePos();
        for (Direction dir : Direction.values()) {
            if (client.world.getBlockState(tablePos.offset(dir)).getBlock()
                    == EnchantTransferMod.INFUSION_COIL_BLOCK) {
                result[dir.ordinal()] = true;
            }
        }
        return result;
    }

    private void drawNavRow(DrawContext ctx, int mouseX, int mouseY) {
        int gx = (width  - backgroundWidth)  / 2;
        int gy = (height - backgroundHeight) / 2;
        int hy = gy + NAV_Y;

        boolean[] connected = connectedSlots();
        BlockPos tablePos = handler.getTablePos();

        // i=0 → hub: this screen is active so it gets the cyan ring; not
        // clickable from itself so we don't draw a hover halo.
        int hubX = gx + NAV_X0;
        drawHubDot(ctx, hubX, hy, true, /*hoverable=*/false, mouseX, mouseY);

        // i=1..6 → module slots.  Each connected coil's state is read live
        // from the client-side BE.
        for (int i = 1; i < NAV_SLOTS; i++) {
            int cx = gx + NAV_X0 + i * NAV_STEP;
            Direction dir = Direction.values()[i - 1];
            if (!connected[i - 1]) {
                drawNavEmptyDot(ctx, cx, hy);
            } else {
                BlockPos coilPos = tablePos.offset(dir);
                float  fillRatio = 0f;
                boolean processing = false;
                if (client != null && client.world != null) {
                    var be = client.world.getBlockEntity(coilPos);
                    if (be instanceof InfusionCoilBlockEntity coil) {
                        fillRatio  = coil.getFillRatio();
                        processing = coil.isProcessing();
                    }
                }
                int border = processing ? COL_MODULE_BORDER_ACTIVE : COL_MODULE_BORDER_IDLE;
                drawNavDot(ctx, cx, hy, border, false, fillRatio, /*hoverable=*/true, mouseX, mouseY);
            }
            drawDirLabel(ctx, cx, hy, dir);
        }
    }

    /**
     * Module dot — dark-purple interior with a state-coloured border and an
     * opaque lime fluid-fill clipped to the disk shape.  Matches the
     * Selector screen vocabulary, so the same coil reads identically in
     * both GUIs (empty bit clearly dark, filled bit clearly green).
     * Adds the hover halo when the mouse is over a clickable dot.
     */
    private void drawNavDot(DrawContext ctx, int cx, int cy, int borderColor, boolean active,
                            float fillRatio, boolean hoverable, int mouseX, int mouseY) {
        boolean isHovered = hoverable && hovered(mouseX, mouseY, cx, cy, NAV_RING + 2);
        // Hover → white + translucent lime.  Default (selected or not) →
        // dark purple + state-coloured border + opaque lime fluid.
        if (isHovered) {
            NavDotRenderer.disk(ctx, cx, cy, NAV_DOT, COL_ACTIVE);
            if (fillRatio > 0f) {
                NavDotRenderer.diskFillFromBottom(ctx, cx, cy, NAV_DOT, fillRatio, COL_TANK_FILL_ACTIVE);
            }
        } else {
            NavDotRenderer.diskWithBorder(ctx, cx, cy, NAV_DOT, COL_TANK_EMPTY, borderColor);
            if (fillRatio > 0f) {
                NavDotRenderer.diskFillFromBottom(ctx, cx, cy, NAV_DOT - 1, fillRatio, COL_FLUID);
            }
        }
        if (active) NavDotRenderer.ring(ctx, cx, cy, NAV_RING, COL_RING_CYAN);
        if (isHovered) NavDotRenderer.ring(ctx, cx, cy, NAV_RING + 1, HOVER_COL);
    }

    /**
     * Hub-style nav dot: 4 concentric layers — gold border, red, black,
     * blue — drawn over the standard 7-px disk to differentiate the hub
     * from the green/amber module dots at a glance.  Adds the active ring
     * + hover halo according to flags.
     */
    private void drawHubDot(DrawContext ctx, int cx, int cy, boolean active,
                            boolean hoverable, int mouseX, int mouseY) {
        NavDotRenderer.disk(ctx, cx, cy, NAV_DOT,     COL_HUB_GOLD);  // r=3 (outer)
        NavDotRenderer.disk(ctx, cx, cy, NAV_DOT - 1, COL_HUB_RED);   // r=2
        NavDotRenderer.disk(ctx, cx, cy, NAV_DOT - 2, COL_HUB_BLACK); // r=1
        ctx.fill(cx, cy, cx + 1, cy + 1, COL_HUB_BLUE);               // 1-px centre
        if (active) NavDotRenderer.ring(ctx, cx, cy, NAV_RING, COL_RING_CYAN);
        if (hoverable && hovered(mouseX, mouseY, cx, cy, NAV_RING + 2)) {
            NavDotRenderer.ring(ctx, cx, cy, NAV_RING + 1, HOVER_COL);
        }
    }

    /**
     * Empty-socket marker — purple-bordered circle with a tiny "+" glyph,
     * consistent with the Selector screen's vocabulary.
     */
    private void drawNavEmptyDot(DrawContext ctx, int cx, int cy) {
        NavDotRenderer.diskWithBorder(ctx, cx, cy, NAV_DOT, EMPTY_BG_COL, EMPTY_BORDER_COL);
        NavDotRenderer.plus(ctx, cx, cy, PLUS_ARM_RADIUS, PLUS_THICKNESS, EMPTY_PLUS_COL);
    }

    /**
     * Direction tag (N/S/E/W/↑/↓) centred just below a dot.
     *
     * <p>Important: we compute the scaled text width and the centred world-X
     * offset as INTEGERS first, then translate by that integer position and
     * only THEN apply the scale.  Doing the centering inside the scaled
     * matrix (the previous approach) put the text at a sub-pixel position
     * because {@code drawCenteredTextWithShadow} divides {@code tw} by 2
     * before the scale is applied — MC then rounds the resulting fractional
     * coordinates inconsistently and the glyph looked left-aligned.
     */
    private void drawDirLabel(DrawContext ctx, int cx, int dotCy, Direction dir) {
        if (client == null) return;
        Text label   = NavDotRenderer.dirLabel(dir);
        int tw       = client.textRenderer.getWidth(label);
        int scaledTw = Math.round(tw * LABEL_SCALE);
        int worldX   = cx - scaledTw / 2;
        int worldY   = dotCy + NAV_DOT + LABEL_GAP;

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(worldX, worldY);
        m.scale(LABEL_SCALE, LABEL_SCALE);
        ctx.drawTextWithShadow(client.textRenderer, label, 0, 0, LABEL_COL);
        m.popMatrix();
    }

    private static boolean hovered(int mx, int my, int cx, int cy, int r) {
        int dx = mx - cx, dy = my - cy;
        return dx * dx + dy * dy <= r * r;
    }

    // ── Tooltips ──────────────────────────────────────────────────────────────

    @Override
    protected void drawMouseoverTooltip(DrawContext ctx, int mouseX, int mouseY) {
        super.drawMouseoverTooltip(ctx, mouseX, mouseY);
        if (client == null || client.world == null) return;

        int gx = (width  - backgroundWidth)  / 2;
        int gy = (height - backgroundHeight) / 2;
        int hy = gy + NAV_Y;
        int hitR = NAV_RING + 2;
        BlockPos tablePos = handler.getTablePos();

        // Hub dot (i=0)
        int hubX = gx + NAV_X0;
        if (sq(mouseX - hubX) + sq(mouseY - hy) <= hitR * hitR) {
            ctx.drawTooltip(textRenderer,
                    Text.translatable("block.enchanttransfer.transfer_table_block"),
                    mouseX, mouseY);
            return;
        }
        // Module dots (i=1..6)
        boolean[] connected = connectedSlots();
        for (int i = 1; i < NAV_SLOTS; i++) {
            int cx = gx + NAV_X0 + i * NAV_STEP;
            if (sq(mouseX - cx) + sq(mouseY - hy) > hitR * hitR) continue;
            Direction dir = Direction.values()[i - 1];
            String dirName = dir.name();
            if (!connected[i - 1]) {
                ctx.drawTooltip(textRenderer,
                        Text.literal("§7Empty (" + dirName + ")"),
                        mouseX, mouseY);
                return;
            }
            BlockPos coilPos = tablePos.offset(dir);
            String state  = "Idle";
            String levelS = "?";
            var be = client.world.getBlockEntity(coilPos);
            if (be instanceof InfusionCoilBlockEntity coil) {
                state  = coil.isProcessing() ? "§eInfusing" : "§aIdle";
                int stored   = coil.getStored();
                int capacity = coil.getCapacity();
                levelS = stored + " / " + capacity + " XP";
            }
            ctx.drawTooltip(textRenderer, List.of(
                    Text.literal("§fInfusion Coil §7(" + dirName + ")"),
                    Text.literal("§7" + levelS),
                    Text.literal(state)
            ), mouseX, mouseY);
            return;
        }
    }

    private static int sq(int v) { return v * v; }

    @Override
    public boolean mouseClicked(Click click, boolean down) {
        if (click.button() == 0 && client != null && client.world != null) {
            int gx = (width  - backgroundWidth)  / 2;
            int gy = (height - backgroundHeight) / 2;
            int hy = gy + NAV_Y;
            boolean[] connected = connectedSlots();
            int hitR = NAV_RING + 2;

            // i=0 → hub: skip (already on this screen)
            // i=1..6 → module slots
            for (int i = 1; i < NAV_SLOTS; i++) {
                if (!connected[i - 1]) continue;
                int cx = gx + NAV_X0 + i * NAV_STEP;
                int dx = (int) click.x() - cx, dy = (int) click.y() - hy;
                if (dx * dx + dy * dy <= hitR * hitR) {
                    Direction dir = Direction.values()[i - 1];
                    BlockPos modulePos = handler.getTablePos().offset(dir);
                    ClientPlayNetworking.send(new RequestOpenGuiPayload(modulePos));
                    return true;
                }
            }
        }
        return super.mouseClicked(click, down);
    }
}
