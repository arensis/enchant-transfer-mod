package net.alfonsormadrid.enchanttransfer.library;

import net.alfonsormadrid.enchanttransfer.item.CardType;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Category deck: stores cards of one {@link CardType} and steers the table
 * toward that category.  Treasure cards (e.g. Mending in Arcane) are stored
 * and add to the category's strength, though the table never rolls them.
 * <pre>
 *   factor = (1 + 2·R·categoryShare) · (1 + 4·R·enchantmentShare)
 *   R      = min(1, √(category points / 64))
 * </pre>
 * The category part favors the category over the others (up to ×3); the
 * exact part favors the enchantment over the rest of its category (up to
 * ×5, so ×15 in all).  Every card counts for the category share; only
 * max-level cards count for the exact one.  Shares are relative to all
 * category decks around the table, so a library with every category in
 * equal parts barely changes anything.  {@code R} grows fast at first, so
 * even a few low-level cards show, and peaks at one full deck.
 */
public class CategoryDeckEffect implements DeckEffect {

    public static final int FULL_RESONANCE_POINTS = 64;
    private static final double CATEGORY_BIAS = 2.0;
    private static final double EXACT_BIAS = 4.0;

    /** Top of each part (full resonance, a single category / enchantment). */
    public static final double MAX_CATEGORY_FACTOR = 1.0 + CATEGORY_BIAS;
    public static final double MAX_EXACT_FACTOR = 1.0 + EXACT_BIAS;

    private final CardType category;

    public CategoryDeckEffect(CardType category) {
        this.category = category;
    }

    public CardType getCategory() {
        return category;
    }

    @Override
    public boolean accepts(Holder<Enchantment> enchantment) {
        return !enchantment.is(EnchantmentTags.CURSE)
                && CardType.forEnchantment(enchantment) == category;
    }

    @Override
    public double weightMultiplier(EnchantmentInstance candidate, LibraryProfile profile) {
        if (CardType.forEnchantment(candidate.enchantment()) != category) return 1.0;
        return categoryFactor(profile) * exactFactor(candidate.enchantment(), profile);
    }

    /** The category part: how much more likely every enchantment of the category is. */
    public double categoryFactor(LibraryProfile profile) {
        int total = profile.getCategoryTotal();
        if (total <= 0) return 1.0;
        return 1.0 + CATEGORY_BIAS * resonance(total) * profile.getCategoryPoints(category) / (double) total;
    }

    /** The exact part for {@code enchantment}, on top of {@link #categoryFactor}. */
    public double exactFactor(Holder<Enchantment> enchantment, LibraryProfile profile) {
        int total = profile.getCategoryTotal();
        if (total <= 0) return 1.0;
        return 1.0 + EXACT_BIAS * resonance(total) * profile.getMaxLevelEnchantmentPoints(enchantment) / (double) total;
    }

    private static double resonance(int total) {
        return Math.min(1.0, Math.sqrt(total / (double) FULL_RESONANCE_POINTS));
    }

    @Override
    public boolean wearsOn(EnchantmentInstance applied) {
        return CardType.forEnchantment(applied.enchantment()) == category;
    }

    /** Full rate when max-level cards favored this exact enchantment, else half (category only). */
    @Override
    public double wearRate(EnchantmentInstance applied, LibraryProfile profile) {
        return profile.getMaxLevelEnchantmentPoints(applied.enchantment()) > 0 ? 1.0 : 0.5;
    }
}
