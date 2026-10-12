package net.alfonsormadrid.enchanttransfer.library;

import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.minecraft.core.Holder;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Rarity wildcard: stores Curse cards (curses never come out of the table,
 * so they have no category to favor) and favors the table's low-weight
 * enchantments.
 * <pre>
 *   factor = 1 + 2·strength·(10 − weight) / 9
 * </pre>
 * Only rare enchantments (vanilla rarity "rare" or "very rare", weight ≤
 * {@value #RARE_WEIGHT}) wear its cards.
 */
public class RarityDeckEffect extends WildcardDeckEffect {

    private static final double MAX_BIAS = 2.0;

    /** Highest weight that counts as rare: Fortune, Looting (2), Silk Touch, Thorns (1)… */
    private static final int RARE_WEIGHT = 2;

    @Override
    protected DeckKind kind() {
        return DeckKind.RARE;
    }

    @Override
    public boolean accepts(Holder<Enchantment> enchantment) {
        return enchantment.is(EnchantmentTags.CURSE);
    }

    @Override
    public double weightMultiplier(EnchantmentInstance candidate, LibraryProfile profile) {
        double strength = strength(profile);
        if (strength <= 0) return 1.0;
        int weight = Math.clamp(candidate.weight(), 1, 10);
        return 1.0 + MAX_BIAS * strength * (10 - weight) / 9.0;
    }

    @Override
    public boolean wearsOn(EnchantmentInstance applied) {
        return applied.weight() <= RARE_WEIGHT;
    }
}
