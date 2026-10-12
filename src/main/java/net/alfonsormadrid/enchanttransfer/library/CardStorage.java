package net.alfonsormadrid.enchanttransfer.library;

import java.util.function.Predicate;
import net.minecraft.world.item.ItemStack;

/**
 * Abstraction for any component that can store Magic Cards.
 * <p>
 * Same shape as {@link net.alfonsormadrid.enchanttransfer.energy.ExperienceStorage}:
 * capacity is measured in card points (a level N card is worth
 * {@code 2^(N-1)}, see {@code MagicCardItem.pointsOf}) rather than in card
 * count, so a full storage holds the same "amount of enchantment" whether
 * its cards are combined or not.
 */
public interface CardStorage {

    /** Whether this storage takes this kind of card at all (ignores capacity). */
    boolean accepts(ItemStack card);

    /**
     * Attempt to insert cards from {@code card}.  The given stack is never
     * modified: the caller removes the returned amount from its source.
     *
     * @param simulate if {@code true} no state changes are applied
     * @return number of cards actually inserted (0 if not accepted or full)
     */
    int insert(ItemStack card, boolean simulate);

    /**
     * Attempt to take out the most recently inserted stack.
     *
     * @param simulate if {@code true} no state changes are applied
     * @return the extracted stack, or {@link ItemStack#EMPTY} if empty
     */
    ItemStack extractLast(boolean simulate);

    /**
     * Wear the stored cards by {@code units} (see {@code CardWear}): a worn
     * card first, else the lowest-level one; cards that run out are removed.
     * Used by library wear.
     *
     * @return the wear that didn't fit (0 if all of it was applied)
     */
    int wear(int units);

    /** Reserve left across the stored cards, in {@code CardWear} units. */
    int getReserve();

    int getStoredPoints();

    int getCapacityPoints();

    default int getAvailablePoints() {
        return getCapacityPoints() - getStoredPoints();
    }

    default boolean isEmpty() {
        return getStoredPoints() <= 0;
    }

    default boolean isFull() {
        return getStoredPoints() >= getCapacityPoints();
    }
}
