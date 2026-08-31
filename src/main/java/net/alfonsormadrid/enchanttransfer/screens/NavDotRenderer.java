package net.alfonsormadrid.enchanttransfer.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

/**
 * Pixel-art helpers for drawing circular GUI dots in the Transfer-Table and
 * Infusion-Coil nav rows.
 *
 * <p>All shapes are rasterised by iterating rows of the bounding box and using
 * {@code dx*dx + dy*dy <= rSq} (with a small inflation) to decide which
 * pixels fall inside the circle.  Inflating {@code rSq} by {@code r} makes the
 * outline corners read as rounded rather than chamfered on very small radii
 * (the 3-px and 5-px dots/rings used by the nav row).
 *
 * <p>Per row only a single {@link GuiGraphics#fill} call is issued, so the
 * cost is O(r) per shape — negligible for a nav row of 7 dots.
 */
@Environment(EnvType.CLIENT)
public final class NavDotRenderer {

    private NavDotRenderer() {}

    /**
     * Localised short label for a Direction.  Backed by the
     * {@code enchanttransfer.direction.*} lang keys so each language can
     * remap them — e.g. Spanish swaps the W for O (Oeste) while the
     * up/down arrow glyphs stay universal.
     */
    public static Component dirLabel(Direction dir) {
        String key = switch (dir) {
            case NORTH -> "north";
            case SOUTH -> "south";
            case EAST  -> "east";
            case WEST  -> "west";
            case UP    -> "up";
            case DOWN  -> "down";
        };
        return Component.translatable("enchanttransfer.direction." + key);
    }

    /** Fills a circular disk centred at ({@code cx}, {@code cy}) with half-extent {@code r}. */
    public static void disk(GuiGraphics ctx, int cx, int cy, int r, int color) {
        int rSq = r * r + r;
        for (int dy = -r; dy <= r; dy++) {
            int rowSpan = (int) Math.sqrt(Math.max(0, rSq - dy * dy));
            ctx.fill(cx - rowSpan, cy + dy, cx + rowSpan + 1, cy + dy + 1, color);
        }
    }

    /**
     * Disk of radius {@code r} with a 1-pixel contrasting border around it:
     * the outer ring is drawn in {@code borderColor} and the inner area
     * (radius {@code r-1}) in {@code bgColor}.  Used to lift a circle off
     * a background of similar tone so its edge stays readable.
     */
    public static void diskWithBorder(GuiGraphics ctx, int cx, int cy, int r,
                                       int bgColor, int borderColor) {
        disk(ctx, cx, cy, r, borderColor);
        if (r > 0) disk(ctx, cx, cy, r - 1, bgColor);
    }

    /**
     * Fills the lower fraction of a circular disk, used as a "tank fill"
     * overlay.  The bar grows from the bottom of the circle upward by a
     * height proportional to {@code fillRatio}, clipped to the disk shape
     * so the bottom edge follows the curve.
     *
     * @param fillRatio 0..1; clamped if outside
     */
    public static void diskFillFromBottom(GuiGraphics ctx, int cx, int cy, int r, float fillRatio, int color) {
        if (fillRatio <= 0f) return;
        if (fillRatio > 1f) fillRatio = 1f;
        int rSq      = r * r + r;
        int diameter = 2 * r + 1;
        int fillH    = Math.max(1, (int) Math.ceil(diameter * fillRatio));
        // Rows from py=fillTopRow downward (inclusive) are filled.
        int fillTopRow = r - fillH + 1;
        for (int dy = -r; dy <= r; dy++) {
            if (dy < fillTopRow) continue;
            int rowSpan = (int) Math.sqrt(Math.max(0, rSq - dy * dy));
            ctx.fill(cx - rowSpan, cy + dy, cx + rowSpan + 1, cy + dy + 1, color);
        }
    }

    /**
     * Clockwise arc inside the ring between half-extents {@code rInner}
     * (exclusive) and {@code rOuter} (inclusive), sweeping from 12 o'clock
     * for {@code fraction} of a full revolution.
     *
     * <p>Used as a furnace-style progress indicator around the module disks
     * in the Selector — at {@code fraction=0.5} we get a half ring filling
     * the right half of the circle (12 → 6 clockwise); at {@code fraction=1}
     * the entire ring is filled.
     *
     * <p>Angle convention: {@code atan2(dx, -dy)} maps 12-up to 0, with
     * angles increasing CLOCKWISE so the visual matches the player's
     * intuition (clock hand sweeping right).
     */
    public static void arc(GuiGraphics ctx, int cx, int cy,
                            int rOuter, int rInner, float fraction, int color) {
        if (fraction <= 0f) return;
        if (fraction > 1f) fraction = 1f;
        int rOuterSq = rOuter * rOuter + rOuter;
        int rInnerSq = rInner * rInner;
        double maxAngle = fraction * 2.0 * Math.PI;
        for (int dy = -rOuter; dy <= rOuter; dy++) {
            for (int dx = -rOuter; dx <= rOuter; dx++) {
                int d2 = dx * dx + dy * dy;
                if (d2 > rOuterSq || d2 < rInnerSq) continue;
                double a = Math.atan2(dx, -dy);
                if (a < 0) a += 2.0 * Math.PI;
                if (a < maxAngle) {
                    ctx.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
                }
            }
        }
    }

    /**
     * Draws a "+" glyph centred at ({@code cx}, {@code cy}).  Each arm
     * extends {@code armRadius} pixels from the centre and is
     * {@code thickness} pixels wide.
     *
     * <p>Used by the Selector screen to mark empty sockets instead of
     * relying on a static sprite.
     */
    public static void plus(GuiGraphics ctx, int cx, int cy, int armRadius, int thickness, int color) {
        int halfT = thickness / 2;
        // Horizontal arm
        ctx.fill(cx - armRadius, cy - halfT,
                 cx + armRadius + 1, cy + halfT + 1, color);
        // Vertical arm
        ctx.fill(cx - halfT, cy - armRadius,
                 cx + halfT + 1, cy + armRadius + 1, color);
    }

    /**
     * Draws a 1-pixel-thick circular outline at half-extent {@code r}.  Used as
     * the "this screen is active" ring around the selected nav dot.
     */
    public static void ring(GuiGraphics ctx, int cx, int cy, int r, int color) {
        int rOuterSq = r * r + r;
        int rInnerSq = (r - 1) * (r - 1) + (r - 1);
        for (int dy = -r; dy <= r; dy++) {
            int outerX = (int) Math.sqrt(Math.max(0, rOuterSq - dy * dy));
            if (dy * dy > rInnerSq) {
                // No inner clipping for this row — the whole horizontal span is ring.
                ctx.fill(cx - outerX, cy + dy, cx + outerX + 1, cy + dy + 1, color);
            } else {
                int innerX = (int) Math.sqrt(rInnerSq - dy * dy);
                if (outerX > innerX) {
                    // Left arc
                    ctx.fill(cx - outerX, cy + dy, cx - innerX, cy + dy + 1, color);
                    // Right arc
                    ctx.fill(cx + innerX + 1, cy + dy, cx + outerX + 1, cy + dy + 1, color);
                }
            }
        }
    }
}
