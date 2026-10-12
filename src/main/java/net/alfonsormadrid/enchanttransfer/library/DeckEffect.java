package net.alfonsormadrid.enchanttransfer.library;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * What a kind of deck does to a nearby enchanting table.
 * <p>
 * Each {@code DeckKind} is composed with one effect, so the selector and the
 * wear logic never switch on the kind: they ask every effect present in the
 * {@link LibraryProfile}.  A new kind of deck is a new implementation of this
 * interface, with no change to the enchanting code.
 * <p>
 * All hooks have neutral defaults, so an effect only overrides what it changes.
 */
public interface DeckEffect {

    /** Which cards a deck with this effect stores. */
    boolean accepts(Holder<Enchantment> enchantment);

    /**
     * Factor applied to a candidate's weight when the table picks
     * enchantments.  Factors from every effect present are multiplied.
     */
    default double weightMultiplier(EnchantmentInstance candidate, LibraryProfile profile) {
        return 1.0;
    }

    /** Whether applying {@code applied} can wear decks with this effect. */
    default boolean wearsOn(EnchantmentInstance applied) {
        return true;
    }

    /**
     * Share of the full wear (see {@code CardWear#wearFor}) these decks pay
     * for their part of an influenced {@code applied}: 1 for an exact
     * enchantment bonus, ½ for a category-like one.
     */
    default double wearRate(EnchantmentInstance applied, LibraryProfile profile) {
        return 0.5;
    }
}
