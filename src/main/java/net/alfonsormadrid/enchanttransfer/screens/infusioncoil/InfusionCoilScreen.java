package net.alfonsormadrid.enchanttransfer.screens.infusioncoil;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.alfonsormadrid.enchanttransfer.gui.infusioncoil.InfusionCoilSlotPositions;
import net.alfonsormadrid.enchanttransfer.gui.infusioncoil.InfusionCoilGuiMetrics;
import net.alfonsormadrid.enchanttransfer.network.RequestOpenGuiPayload;
import net.alfonsormadrid.enchanttransfer.screens.NavDotRenderer;
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
import org.jetbrains.annotations.Nullable;

/**
 * Client-side screen for the Infusion Coil.
 * <p>
 * Renders the static background (infusor_gui.png) and then draws the
 * dynamic elements on top:
 * <ul>
 *   <li>Progress arrow (card → XP) filled left-to-right in cyan/violet.</li>
 *   <li>XP tank filled bottom-to-top in XP-green.</li>
 *   <li>Nav row identical to {@link net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreen}:
 *       hub (golden) + all connected modules; clicking navigates via the
 *       {@link RequestOpenGuiPayload} C2S packet.</li>
 * </ul>
 */
public class InfusionCoilScreen extends HandledScreen<InfusionCoilScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.of(EnchantTransferMod.MOD_ID, "textures/gui/container/infusor_gui.png");

    // Progress-arrow fill colours (violet left-half → cyan right-half)
    private static final int ARROW_COL_LEFT  = 0xFF7B00FF; // violet
    private static final int ARROW_COL_RIGHT = 0xFF00FFEE; // cyan

    // XP-green fluid colour (ARGB)
    private static final int XP_GREEN = 0xFF4CAF50;

    // ── Nav row — fixed 7-slot positions matching the PNG circle backgrounds ──
    // Positions: x = NAV_X0 + i*NAV_STEP  for i in 0..6  (GUI-local coords)
    private static final int NAV_Y       = 21;
    private static final int NAV_SLOTS   = 7;
    private static final int NAV_STEP    = 12;
    private static final int NAV_X0      = 52;   // GUI-local x of hub (i=0)
    private static final int NAV_DOT     = 3;    // half-size of filled dot
    private static final int NAV_RING    = 5;    // half-size of active ring

    // Non-current-coil palette — matches the Selector and TransferTable navbars
    private static final int COL_MODULE_BORDER_IDLE   = 0xFF1F8044;
    private static final int COL_MODULE_BORDER_ACTIVE = 0xFF8B6914;
    private static final int COL_TANK_EMPTY           = 0xFF3D2A52;
    private static final int COL_FLUID                = 0xFF50F03C;
    // Current-coil identity (this screen) keeps its solid-white dot so the
    // player immediately spots which coil they're inside.
    private static final int COL_ACTIVE               = 0xFFFFFFFF;
    private static final int COL_TANK_FILL_ACTIVE     = 0xCC50F03C; // semi over white
    private static final int COL_RING_CYAN            = 0xFF00FFEE;

    // Hub "target" colours, matching TransferTableScreen
    private static final int COL_HUB_GOLD  = 0xFFFFD700;
    private static final int COL_HUB_RED   = 0xFFCC2030;
    private static final int COL_HUB_BLACK = 0xFF101020;
    private static final int COL_HUB_BLUE  = 0xFF2860E0;
    // Empty-socket palette — matches Selector + TransferTable navrow.
    // Border lighter than bg but less saturated than the plus.
    private static final int EMPTY_BG_COL        = 0xFF1A0A26;
    private static final int EMPTY_BORDER_COL    = 0xFF3A2E4A;
    private static final int EMPTY_PLUS_COL      = 0xFFB46AC8;
    // Hover ring colour for clickable dots
    private static final int HOVER_COL           = 0xFFFFFFFF;
    // Direction-label colour — desaturated dark purple derived from the plus
    private static final int LABEL_COL           = 0xFF8240A0;

    // Small "+" glyph: 3×3 px with 1 px padding inside the 7-px dot
    private static final int PLUS_ARM_RADIUS = 1;
    private static final int PLUS_THICKNESS  = 1;

    // Direction-label font scale (0.5 → ~3-4 px tall, clean 2:1 downsample)
    private static final float LABEL_SCALE = 0.5f;
    // Pixel gap between the dot's bottom edge and the label's top edge.
    private static final int   LABEL_GAP   = 4;

    public InfusionCoilScreen(InfusionCoilScreenHandler handler,
                               PlayerInventory inventory,
                               Text title) {
        super(handler, inventory, title);
        backgroundWidth  = InfusionCoilGuiMetrics.BACKGROUND_WIDTH;
        backgroundHeight = InfusionCoilGuiMetrics.BACKGROUND_HEIGHT;
    }

    /** Parchment-brown ink colour shared with the other screens. */
    private static final int TITLE_COL = 0xFF4A2811;

    // Ghost-icon hint shown on empty input slots: a faded silhouette of the
    // item the slot expects.  Replaced by the real item the moment the
    // player drops one in.
    private static final Identifier GHOST_CARD =
            Identifier.of(EnchantTransferMod.MOD_ID, "textures/item/magic_card_item.png");
    private static final Identifier GHOST_BOTTLE =
            Identifier.of("minecraft", "textures/item/glass_bottle.png");
    /** ARGB tint for ghost icons — 38 % white opacity preserves the silhouette
     *  while clearly reading as a hint rather than an actual item. */
    private static final int GHOST_TINT = 0x60FFFFFF;

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
    }

    /**
     * Override the vanilla foreground draw so only the parchment-brown
     * screen title is rendered.  The default "Inventory" label is omitted
     * because the Infusion Coil GUI doesn't have a spare row for it.
     */
    @Override
    protected void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(textRenderer, title, titleX, titleY, TITLE_COL, false);
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (width  - backgroundWidth)  / 2;
        int y = (height - backgroundHeight) / 2;

        // Static background
        context.drawTexture(RenderPipelines.GUI_TEXTURED,
                TEXTURE, x, y, 0.0f, 0.0f,
                backgroundWidth, backgroundHeight, 256, 256);

        // Ghost icons hint what each input slot accepts.  Drawn before the
        // progress / tank overlays so they sit behind everything dynamic
        // and (most importantly) the real item the slot shows once filled.
        drawGhostsForEmptyInputs(context, x, y);

        drawProgressArrow(context, x, y);
        drawXpTank(context, x, y);
    }

    /**
     * Paints the faded card / glass-bottle hints over the empty card and
     * glass-bottle input slots.  Output slot intentionally not touched.
     */
    private void drawGhostsForEmptyInputs(DrawContext ctx, int guiLeft, int guiTop) {
        if (!handler.slots.get(InfusionCoilBlockEntity.SLOT_CARD_IN).hasStack()) {
            drawGhostIcon(ctx, GHOST_CARD,
                    guiLeft + InfusionCoilSlotPositions.cardIn.positionX,
                    guiTop  + InfusionCoilSlotPositions.cardIn.positionY);
        }
        if (!handler.slots.get(InfusionCoilBlockEntity.SLOT_BOTTLE_IN).hasStack()) {
            drawGhostIcon(ctx, GHOST_BOTTLE,
                    guiLeft + InfusionCoilSlotPositions.glassIn.positionX,
                    guiTop  + InfusionCoilSlotPositions.glassIn.positionY);
        }
    }

    private static void drawGhostIcon(DrawContext ctx, Identifier texture, int x, int y) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, texture,
                x, y, 0f, 0f, 16, 16, 16, 16, GHOST_TINT);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        // Nav row before tooltip so hover popups float above the dots.
        drawNavRow(context, mouseX, mouseY);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }

    /**
     * Fills the progress arrow left-to-right proportional to the infusion
     * progress stored in the handler's PropertyDelegate.
     * Always shows at least 1 pixel once progress > 0 so the player gets
     * immediate visual feedback when a card starts infusing.
     */
    private void drawProgressArrow(DrawContext context, int guiLeft, int guiTop) {
        int progress    = handler.getProgress();
        int maxProgress = handler.getMaxProgress();
        if (maxProgress <= 0 || progress <= 0) return;

        // Math.max ensures at least 1 px is painted on the very first tick
        int fillWidth = Math.max(1,
                (int) (InfusionCoilGuiMetrics.PROGRESS_WIDTH * ((float) progress / maxProgress)));
        if (fillWidth <= 0) return;

        int arrowX = guiLeft + InfusionCoilGuiMetrics.progressArrow.positionX;
        int arrowY = guiTop  + InfusionCoilGuiMetrics.progressArrow.positionY;
        int arrowBottom = arrowY + InfusionCoilGuiMetrics.PROGRESS_HEIGHT;

        // Two-tone fill: violet on the left half, cyan on the right half.
        // No sprite exists at u=176 in infusor_gui.png, so we draw with fill().
        int half = fillWidth / 2;
        if (half > 0) {
            context.fill(arrowX, arrowY, arrowX + half, arrowBottom, ARROW_COL_LEFT);
        }
        context.fill(arrowX + half, arrowY, arrowX + fillWidth, arrowBottom, ARROW_COL_RIGHT);
    }

    /**
     * Fills the XP tank bottom-to-top proportional to the stored XP.
     */
    private void drawXpTank(DrawContext context, int guiLeft, int guiTop) {
        int stored   = handler.getStoredXp();
        int capacity = handler.getTankCapacity();
        if (capacity <= 0) return;

        float fillRatio = (float) stored / capacity;
        int fillHeight  = (int) (InfusionCoilGuiMetrics.TANK_HEIGHT * fillRatio);
        if (fillHeight <= 0) return;

        int tankX  = guiLeft + InfusionCoilGuiMetrics.xpTank.positionX;
        int tankY  = guiTop  + InfusionCoilGuiMetrics.xpTank.positionY
                     + (InfusionCoilGuiMetrics.TANK_HEIGHT - fillHeight);

        context.fill(tankX, tankY,
                tankX + InfusionCoilGuiMetrics.TANK_WIDTH,
                tankY + fillHeight,
                XP_GREEN);
    }

    // ── Nav row ───────────────────────────────────────────────────────────────

    /**
     * Finds the Transfer Table adjacent to this coil by scanning the 6 neighbours.
     * Returns {@code null} if the world is not loaded or the coil is disconnected.
     */
    @Nullable
    private BlockPos findTablePos() {
        if (client == null || client.world == null) return null;
        BlockPos coilPos = handler.getCoilPos();
        for (Direction dir : Direction.values()) {
            BlockPos nb = coilPos.offset(dir);
            if (client.world.getBlockState(nb).isOf(EnchantTransferMod.TRANSFER_TABLE_BLOCK)) {
                return nb;
            }
        }
        return null;
    }

    private void drawNavRow(DrawContext ctx, int mouseX, int mouseY) {
        BlockPos tablePos = findTablePos();
        BlockPos coilPos  = handler.getCoilPos();

        int gx = (width  - backgroundWidth)  / 2;
        int gy = (height - backgroundHeight) / 2;
        int hy = gy + NAV_Y;

        // i=0 → hub.  Clickable when connected to a Transfer Table.
        int hubX = gx + NAV_X0;
        if (tablePos != null) {
            drawHubDot(ctx, hubX, hy, /*active=*/false, /*hoverable=*/true, mouseX, mouseY);
        } else {
            drawNavEmptyDot(ctx, hubX, hy);
        }

        // i=1..6 → module slots.
        for (int i = 1; i < NAV_SLOTS; i++) {
            int cx = gx + NAV_X0 + i * NAV_STEP;
            Direction dir = Direction.values()[i - 1];
            boolean rendered = false;
            if (tablePos != null && client != null && client.world != null) {
                BlockPos nb = tablePos.offset(dir);
                if (client.world.getBlockState(nb).isOf(EnchantTransferMod.INFUSION_COIL_BLOCK)) {
                    float  fillRatio = 0f;
                    boolean processing = false;
                    var be = client.world.getBlockEntity(nb);
                    if (be instanceof InfusionCoilBlockEntity coil) {
                        fillRatio  = coil.getFillRatio();
                        processing = coil.isProcessing();
                    }
                    boolean isThisCoil = nb.equals(coilPos);
                    // Border tone applies in both modes — the selected coil
                    // also picks it up when the player hovers and the dot
                    // swaps to the empty/full palette.
                    int border = processing ? COL_MODULE_BORDER_ACTIVE : COL_MODULE_BORDER_IDLE;
                    // Hover ring enabled even for THIS coil: visual feedback
                    // only — the click handler still skips the current coil.
                    drawNavDot(ctx, cx, hy, border, isThisCoil, fillRatio,
                            /*hoverable=*/true, mouseX, mouseY);
                    rendered = true;
                }
            }
            if (!rendered) drawNavEmptyDot(ctx, cx, hy);
            drawDirLabel(ctx, cx, hy, dir);
        }
    }

    /**
     * Module dot.  Default state (any coil, selected or not): dark-purple
     * interior + state-coloured border + opaque lime fluid fill.  Hovering
     * swaps to a white disk + semi-translucent lime fill — the player can
     * tell at a glance "this is the dot under my cursor".
     *
     * <p>The cyan identity ring marks the coil whose screen is currently
     * open; it stays on top of whichever palette is active so the
     * "you're here" signal never disappears.
     */
    private void drawNavDot(DrawContext ctx, int cx, int cy, int borderColor, boolean active,
                            float fillRatio, boolean hoverable, int mouseX, int mouseY) {
        boolean isHovered = hoverable && hovered(mouseX, mouseY, cx, cy, NAV_RING + 2);

        if (isHovered) {
            // Hover palette: white + lime
            NavDotRenderer.disk(ctx, cx, cy, NAV_DOT, COL_ACTIVE);
            if (fillRatio > 0f) {
                NavDotRenderer.diskFillFromBottom(ctx, cx, cy, NAV_DOT, fillRatio, COL_TANK_FILL_ACTIVE);
            }
        } else {
            // Default palette: dark purple + state border + lime
            NavDotRenderer.diskWithBorder(ctx, cx, cy, NAV_DOT, COL_TANK_EMPTY, borderColor);
            if (fillRatio > 0f) {
                NavDotRenderer.diskFillFromBottom(ctx, cx, cy, NAV_DOT - 1, fillRatio, COL_FLUID);
            }
        }

        if (active) NavDotRenderer.ring(ctx, cx, cy, NAV_RING, COL_RING_CYAN);
        if (isHovered) NavDotRenderer.ring(ctx, cx, cy, NAV_RING + 1, HOVER_COL);
    }

    private void drawNavEmptyDot(DrawContext ctx, int cx, int cy) {
        NavDotRenderer.diskWithBorder(ctx, cx, cy, NAV_DOT, EMPTY_BG_COL, EMPTY_BORDER_COL);
        NavDotRenderer.plus(ctx, cx, cy, PLUS_ARM_RADIUS, PLUS_THICKNESS, EMPTY_PLUS_COL);
    }

    /**
     * Hub-style concentric-target nav dot — same look as the one in the
     * Transfer-Table screen so the player recognises it instantly across
     * both GUIs.
     */
    private void drawHubDot(DrawContext ctx, int cx, int cy, boolean active,
                            boolean hoverable, int mouseX, int mouseY) {
        NavDotRenderer.disk(ctx, cx, cy, NAV_DOT,     COL_HUB_GOLD);
        NavDotRenderer.disk(ctx, cx, cy, NAV_DOT - 1, COL_HUB_RED);
        NavDotRenderer.disk(ctx, cx, cy, NAV_DOT - 2, COL_HUB_BLACK);
        ctx.fill(cx, cy, cx + 1, cy + 1, COL_HUB_BLUE);
        if (active) NavDotRenderer.ring(ctx, cx, cy, NAV_RING, COL_RING_CYAN);
        if (hoverable && hovered(mouseX, mouseY, cx, cy, NAV_RING + 2)) {
            NavDotRenderer.ring(ctx, cx, cy, NAV_RING + 1, HOVER_COL);
        }
    }

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

    @Override
    public boolean mouseClicked(Click click, boolean down) {
        if (click.button() == 0 && client != null && client.world != null) {
            BlockPos tablePos = findTablePos();
            int gx = (width  - backgroundWidth)  / 2;
            int gy = (height - backgroundHeight) / 2;
            int hy = gy + NAV_Y;
            int hitR = NAV_RING + 2;

            // i=0 → hub: open Transfer Table core screen
            if (tablePos != null) {
                int hx = gx + NAV_X0;
                int dx = (int) click.x() - hx, dy = (int) click.y() - hy;
                if (dx * dx + dy * dy <= hitR * hitR) {
                    ClientPlayNetworking.send(new RequestOpenGuiPayload(tablePos));
                    return true;
                }
            }

            // i=1..6 → module slots
            if (tablePos != null) {
                BlockPos coilPos = handler.getCoilPos();
                for (int i = 1; i < NAV_SLOTS; i++) {
                    Direction dir = Direction.values()[i - 1];
                    BlockPos nb = tablePos.offset(dir);
                    if (!client.world.getBlockState(nb).isOf(EnchantTransferMod.INFUSION_COIL_BLOCK)) continue;
                    if (nb.equals(coilPos)) continue; // skip current screen
                    int cx = gx + NAV_X0 + i * NAV_STEP;
                    int dx = (int) click.x() - cx, dy = (int) click.y() - hy;
                    if (dx * dx + dy * dy <= hitR * hitR) {
                        ClientPlayNetworking.send(new RequestOpenGuiPayload(nb));
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(click, down);
    }

    // ── Tooltips ─────────────────────────────────────────────────────────────

    @Override
    protected void drawMouseoverTooltip(DrawContext context, int mouseX, int mouseY) {
        super.drawMouseoverTooltip(context, mouseX, mouseY);

        int x = (width  - backgroundWidth)  / 2;
        int y = (height - backgroundHeight) / 2;

        // Show "X / 1000 XP" tooltip when hovering over the tank
        int tankX = x + InfusionCoilGuiMetrics.xpTank.positionX;
        int tankY = y + InfusionCoilGuiMetrics.xpTank.positionY;
        if (mouseX >= tankX && mouseX < tankX + InfusionCoilGuiMetrics.TANK_WIDTH
                && mouseY >= tankY && mouseY < tankY + InfusionCoilGuiMetrics.TANK_HEIGHT) {
            context.drawTooltip(textRenderer,
                    Text.literal(handler.getStoredXp() + " / " + handler.getTankCapacity() + " XP"),
                    mouseX, mouseY);
            return;
        }

        // Nav-row hover tooltips
        if (client == null || client.world == null) return;
        int hy = y + NAV_Y;
        int hitR = NAV_RING + 2;
        BlockPos tablePos = findTablePos();
        BlockPos thisCoilPos = handler.getCoilPos();

        // Hub dot
        int hubX = x + NAV_X0;
        if (sq(mouseX - hubX) + sq(mouseY - hy) <= hitR * hitR) {
            context.drawTooltip(textRenderer,
                    tablePos != null
                            ? Text.translatable("block.enchanttransfer.transfer_table_block")
                            : Text.literal("§7Disconnected"),
                    mouseX, mouseY);
            return;
        }

        // Module dots
        for (int i = 1; i < NAV_SLOTS; i++) {
            int cx = x + NAV_X0 + i * NAV_STEP;
            if (sq(mouseX - cx) + sq(mouseY - hy) > hitR * hitR) continue;
            Direction dir = Direction.values()[i - 1];
            String dirName = dir.name();
            if (tablePos == null) {
                context.drawTooltip(textRenderer,
                        Text.literal("§7Empty (" + dirName + ")"),
                        mouseX, mouseY);
                return;
            }
            BlockPos nb = tablePos.offset(dir);
            if (!client.world.getBlockState(nb).isOf(EnchantTransferMod.INFUSION_COIL_BLOCK)) {
                context.drawTooltip(textRenderer,
                        Text.literal("§7Empty (" + dirName + ")"),
                        mouseX, mouseY);
                return;
            }
            String state  = "§aIdle";
            String levelS = "?";
            String suffix = nb.equals(thisCoilPos) ? " §6(this)" : "";
            var be = client.world.getBlockEntity(nb);
            if (be instanceof InfusionCoilBlockEntity coil) {
                state  = coil.isProcessing() ? "§eInfusing" : "§aIdle";
                levelS = coil.getStored() + " / " + coil.getCapacity() + " XP";
            }
            context.drawTooltip(textRenderer, java.util.List.of(
                    Text.literal("§fInfusion Coil §7(" + dirName + ")" + suffix),
                    Text.literal("§7" + levelS),
                    Text.literal(state)
            ), mouseX, mouseY);
            return;
        }
    }

    private static int sq(int v) { return v * v; }
}
