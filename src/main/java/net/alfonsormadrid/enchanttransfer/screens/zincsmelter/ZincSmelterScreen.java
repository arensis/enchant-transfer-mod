package net.alfonsormadrid.enchanttransfer.screens.zincsmelter;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI screen for the Zinc Smelter.
 * <p>
 * Renders the steampunk-themed background ({@code zinc_smelter_gui.png})
 * and dynamic overlays for the flame indicator (when lit) and the
 * progress arrow (while smelting).
 */
@Environment(EnvType.CLIENT)
public class ZincSmelterScreen extends AbstractContainerScreen<ZincSmelterScreenHandler> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "textures/gui/container/zinc_smelter_gui.png");

    // Background content size inside the 256×256 PNG.
    // The GUI image spans 176×190 px (NOT the standard 166).
    private static final int BG_W = 176, BG_H = 190;

    // ── Flame indicator (above fuel slot, bottom-up fill) ─────────────────
    // The flame icon lives just above the fuel slot border at (42,62).
    // Flame pixels span (46,52) to (48,60) — 3 px wide, 9 px tall.
    // We draw a bright orange rectangle that grows upward as fuel burns.
    private static final int FLAME_X = 46, FLAME_Y = 52;
    private static final int FLAME_W = 3,  FLAME_H = 9;
    private static final int FLAME_COLOR = 0xFFFF6600; // bright orange

    // ── Progress arrow (left-to-right fill, shape-clipped) ──────────────
    // The fill is a single rectangle per row from x=70 to the arrowhead's
    // outer diagonal.  No body/triangle split — just one continuous fill
    // whose right edge follows the ">" shape.
    //
    // The arrowhead diagonal goes from (120,34) to (136,40) on top, and
    // from (120,47) to (136,41) on bottom, creating the pointed tip.
    // RIGHT_EDGE[row] (1px inset) defines the max fill x at each row.
    private static final int ARROW_LEFT = 70;
    private static final int ARROW_TOP  = 36;
    private static final int ARROW_BOT  = 46;   // exclusive (12 rows: 35..46)
    private static final int[] RIGHT_EDGE = {119, 120, 121, 122, 123, 123, 122, 121, 120, 119};
    private static final int ARROW_TOTAL_W = 123 - ARROW_LEFT + 1; // 54 px (widest row at centre)
    private static final int ARROW_COLOR = 0xFFD4983C; // warm amber

    // Title colour — dark brown ink readable against the brass title bar.
    private static final int TITLE_COL = 0xFF3A2008;

    public ZincSmelterScreen(ZincSmelterScreenHandler handler,
                             Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        imageWidth  = BG_W;
        imageHeight = BG_H;
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void renderLabels(GuiGraphics ctx, int mouseX, int mouseY) {
        ctx.drawString(font, title, titleLabelX, titleLabelY, TITLE_COL, false);
    }

    @Override
    protected void renderBg(GuiGraphics ctx, float delta, int mouseX, int mouseY) {
        int gx = (width  - imageWidth)  / 2;
        int gy = (height - imageHeight) / 2;
        ctx.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE, gx, gy, 0f, 0f, imageWidth, imageHeight, 256, 256);

        // ── Flame indicator (burns down as fuel depletes) ────────────────
        if (menu.isLit()) {
            float lit = menu.getLitProgress();
            int filledH = Math.max(1, Math.round(FLAME_H * lit));
            int yOffset = FLAME_H - filledH;
            ctx.fill(gx + FLAME_X, gy + FLAME_Y + yOffset,
                     gx + FLAME_X + FLAME_W, gy + FLAME_Y + FLAME_H,
                     FLAME_COLOR);
        }

        // ── Progress arrow (fills left-to-right, clipped to arrow shape) ─
        // One fill rectangle per row.  The right edge follows the ">"
        // diagonal so the fill conforms to the arrowhead silhouette.
        float cook = menu.getCookProgress();
        if (cook > 0f) {
            int fillRight = ARROW_LEFT + Math.max(1, Math.round(ARROW_TOTAL_W * cook));
            for (int row = 0; row < ARROW_BOT - ARROW_TOP; row++) {
                int y = ARROW_TOP + row;
                int rowRight = Math.min(fillRight, RIGHT_EDGE[row] + 1);
                if (rowRight > ARROW_LEFT) {
                    ctx.fill(gx + ARROW_LEFT, gy + y,
                             gx + rowRight, gy + y + 1,
                             ARROW_COLOR);
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        super.render(ctx, mouseX, mouseY, delta);
        renderTooltip(ctx, mouseX, mouseY);
    }
}
