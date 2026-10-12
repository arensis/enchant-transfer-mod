package net.alfonsormadrid.enchanttransfer.screens.carddeck;

import java.util.ArrayList;
import java.util.List;
import net.alfonsormadrid.enchanttransfer.item.CardWear;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.alfonsormadrid.enchanttransfer.library.DeckCardStorage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI screen for a Card Deck held in hand ({@link CardDeckMenu}): the deck's
 * cards in a 9×3 grid, a points bar and a reserve bar, and the player's
 * inventory.  Drawn with the Deck Lectern screen's palette, no texture.
 */
@Environment(EnvType.CLIENT)
public class CardDeckScreen extends AbstractContainerScreen<CardDeckMenu> {

    // Deck Lectern GUI palette (scripts/gen_deck_lectern_assets.py)
    private static final int FRAME = 0xFF8A6030, INNER = 0xFF5A3A18, BG = 0xFF1E160E;
    private static final int TITLE = 0xFFC89030, TITLE_LINE = 0xFF8A6020;
    private static final int SLOT_BORDER = 0xFF3A2810, SLOT_BG = 0xFF0E0A06;
    private static final int TITLE_COL = 0xFF3A2008;

    // "Contribution" | "Reserve" labelled bars under the title, each half the width
    private static final int ROW_Y = 20, BAR_H = 5, LABEL_GAP = 3;
    private static final int POINTS_X = 8, RESERVE_X = 90, HALF_W = 78;
    private static final int LABEL_COL = 0xFFC8B090;

    // Bar tooltips wrap at this width so long translations don't run off-screen
    private static final int TOOLTIP_MAX_WIDTH = 200;

    public CardDeckScreen(CardDeckMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, CardDeckMenu.IMAGE_WIDTH, CardDeckMenu.IMAGE_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        ctx.text(font, title, titleLabelX, titleLabelY, TITLE_COL, false);
        ctx.text(font, pointsLabel(), POINTS_X, ROW_Y, LABEL_COL, false);
        ctx.text(font, reserveLabel(), RESERVE_X, ROW_Y, LABEL_COL, false);
    }

    private static Component pointsLabel() {
        return Component.translatable("enchanttransfer.deck.gui.points");
    }

    private static Component reserveLabel() {
        return Component.translatable("enchanttransfer.deck.gui.reserve");
    }

    /** Bar to the right of its label, filling the rest of its half. */
    private int barX(int halfX, Component label) {
        return halfX + font.width(label) + LABEL_GAP;
    }

    private int barW(int halfX, Component label) {
        return Math.max(8, halfX + HALF_W - barX(halfX, label));
    }

    private int barY() {
        return ROW_Y + (font.lineHeight - 1 - BAR_H) / 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta) {
        super.extractBackground(ctx, mouseX, mouseY, delta);
        int x = leftPos, y = topPos;

        ctx.fill(x, y, x + imageWidth, y + imageHeight, FRAME);
        ctx.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, INNER);
        ctx.fill(x + 3, y + 3, x + imageWidth - 3, y + imageHeight - 3, BG);
        ctx.fill(x + 5, y + 4, x + imageWidth - 5, y + 16, TITLE);
        ctx.fill(x + 5, y + 16, x + imageWidth - 5, y + 17, TITLE_LINE);

        DeckKind kind = menu.getKind();
        int points = menu.storedPoints();
        int fullReserve = points * CardWear.UNITS_PER_POINT;
        bar(ctx, x + barX(POINTS_X, pointsLabel()), y + barY(), barW(POINTS_X, pointsLabel()),
                points / (float) DeckCardStorage.CAPACITY_POINTS,
                kind != null ? 0xFF000000 | kind.barColor() : TITLE);
        float reserveLeft = fullReserve <= 0 ? 0 : menu.reserve() / (float) fullReserve;
        bar(ctx, x + barX(RESERVE_X, reserveLabel()), y + barY(), barW(RESERVE_X, reserveLabel()), reserveLeft,
                0xFF000000 | Mth.hsvToRgb(reserveLeft / 3.0F, 1.0F, 1.0F));

        for (int row = 0; row < CardDeckMenu.ROWS; row++) {
            for (int col = 0; col < CardDeckMenu.COLS; col++) {
                slot(ctx, x + CardDeckMenu.GRID_X + col * 18, y + CardDeckMenu.GRID_Y + row * 18);
            }
        }

        int sepY = y + CardDeckMenu.INV_Y - 9;
        ctx.fill(x + 8, sepY, x + imageWidth - 8, sepY + 1, SLOT_BORDER);
        ctx.fill(x + imageWidth / 2 - 2, sepY - 1, x + imageWidth / 2 + 2, sepY + 2, TITLE);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) slot(ctx, x + 8 + col * 18, y + CardDeckMenu.INV_Y + row * 18);
        }
        for (int col = 0; col < 9; col++) slot(ctx, x + 8 + col * 18, y + CardDeckMenu.HOTBAR_Y);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor ctx, int mouseX, int mouseY) {
        super.extractTooltip(ctx, mouseX, mouseY);
        int points = menu.storedPoints();
        if (overHalf(mouseX, mouseY, POINTS_X)) {
            ctx.setTooltipForNextFrame(font, wrapped(
                    Component.translatable("enchanttransfer.deck.gui.points_value", points, DeckCardStorage.CAPACITY_POINTS),
                    Component.translatable("enchanttransfer.deck.gui.points_hint").withStyle(ChatFormatting.GRAY)),
                    mouseX, mouseY);
        } else if (overHalf(mouseX, mouseY, RESERVE_X)) {
            ctx.setTooltipForNextFrame(font, wrapped(
                    Component.translatable("enchanttransfer.deck.gui.reserve_value",
                            CardWear.format(menu.reserve()), CardWear.format(points * CardWear.UNITS_PER_POINT)),
                    Component.translatable("enchanttransfer.deck.gui.reserve_hint").withStyle(ChatFormatting.GRAY)),
                    mouseX, mouseY);
        }
    }

    /** Tooltip lines wrapped at {@link #TOOLTIP_MAX_WIDTH}, like the Deck Lectern's gem tooltip. */
    private List<FormattedCharSequence> wrapped(Component... lines) {
        List<FormattedCharSequence> out = new ArrayList<>();
        for (Component line : lines) out.addAll(font.split(line, TOOLTIP_MAX_WIDTH));
        return out;
    }

    /** Over a label or its bar. */
    private boolean overHalf(int mouseX, int mouseY, int halfX) {
        int hx = leftPos + halfX, hy = topPos + ROW_Y;
        return mouseX >= hx && mouseX < hx + HALF_W && mouseY >= hy - 1 && mouseY < hy + font.lineHeight;
    }

    private static void bar(GuiGraphicsExtractor ctx, int x, int y, int width, float fill, int color) {
        ctx.fill(x - 1, y - 1, x + width + 1, y + BAR_H + 1, SLOT_BORDER);
        ctx.fill(x, y, x + width, y + BAR_H, SLOT_BG);
        int w = Math.round(width * Mth.clamp(fill, 0.0F, 1.0F));
        if (w > 0) ctx.fill(x, y, x + w, y + BAR_H, color);
    }

    private static void slot(GuiGraphicsExtractor ctx, int x, int y) {
        ctx.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
        ctx.fill(x, y, x + 16, y + 16, SLOT_BG);
    }
}
