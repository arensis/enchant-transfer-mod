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
 *   factor = 1 + 2·R·(½·categoryShare + ½·enchantmentShare)
 *   R      = min(1, category points / 128)
 * </pre>
 * Shares are relative to all category decks around the table, so a library
 * with every category in equal parts barely changes anything, while a
 * specialized one makes its enchantments up to ×3 more likely.
 */
public class CategoryDeckEffect implements DeckEffect {

    public static final int FULL_RESONANCE_POINTS = 128;
    private static final double MAX_BIAS = 2.0;

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
        int total = profile.getCategoryTotal();
        if (total <= 0 || CardType.forEnchantment(candidate.enchantment()) != category) return 1.0;

        double resonance = Math.min(1.0, total / (double) FULL_RESONANCE_POINTS);
        double categoryShare = profile.getCategoryPoints(category) / (double) total;
        double enchantmentShare = profile.getEnchantmentPoints(candidate.enchantment()) / (double) total;
        return 1.0 + MAX_BIAS * resonance * (0.5 * categoryShare + 0.5 * enchantmentShare);
    }

    @Override
    public boolean wearsOn(EnchantmentInstance applied) {
        return CardType.forEnchantment(applied.enchantment()) == category;
    }
}
