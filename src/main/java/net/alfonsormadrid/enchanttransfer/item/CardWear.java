package net.alfonsormadrid.enchanttransfer.item;

import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Card wear in Card Decks (see DESIGN.md, "Desgaste de cartas").
 * <p>
 * A card's <em>reserve</em> equals the points it takes in a deck
 * ({@link MagicCardItem#pointsOf}), so a full deck always holds 64 points
 * of reserve.  Wear is stored per card in the {@code card_wear} component,
 * in {@value #UNITS_PER_POINT} units per point so fractions like 0.2 stay exact.
 */
public final class CardWear {

    public static final int UNITS_PER_POINT = 1000;

    /** Share of a max-level card's reserve one influenced max-level enchantment costs. */
    private static final double WEAR_SHARE = 0.2;

    private CardWear() {
    }

    /** Total reserve of one card, in units. */
    public static int reserveOf(ItemStack card) {
        return MagicCardItem.pointsOf(card) * UNITS_PER_POINT;
    }

    public static int wornOf(ItemStack card) {
        return card.getOrDefault(ModDataComponents.CARD_WEAR, 0);
    }

    /** Reserve one card has left, in units. */
    public static int remainingOf(ItemStack card) {
        return Math.max(0, reserveOf(card) - wornOf(card));
    }

    /** Sets the wear of every card in {@code card} (removes the component at 0). */
    public static void setWorn(ItemStack card, int worn) {
        if (worn > 0) card.set(ModDataComponents.CARD_WEAR, worn);
        else card.remove(ModDataComponents.CARD_WEAR);
    }

    /** Whether the card holds its enchantment at the enchantment's max level. */
    public static boolean isMaxLevel(ItemStack card) {
        return MagicCardItem.enchantmentOf(card)
                .map(e -> MagicCardItem.levelOf(card) >= e.value().getMaxLevel())
                .orElse(false);
    }

    /**
     * Wear one influenced enchantment obtained at {@code level} costs at the
     * full (exact) rate: 20 % of a max-level card's reserve, scaled linearly
     * by the level relative to the max — {@code 0.2 · 2^(M-1) · level / M}.
     */
    public static int wearFor(Holder<Enchantment> enchantment, int level) {
        int max = Math.max(1, enchantment.value().getMaxLevel());
        int maxCardPoints = 1 << Math.clamp(max - 1, 0, 10);
        return (int) Math.round(WEAR_SHARE * maxCardPoints * Math.min(level, max) / max * UNITS_PER_POINT);
    }

    /** Units as card points, for display ("12.8"). */
    public static String format(int units) {
        double points = units / (double) UNITS_PER_POINT;
        return points == Math.rint(points) ? Integer.toString((int) points) : String.format(Locale.ROOT, "%.1f", points);
    }
}
