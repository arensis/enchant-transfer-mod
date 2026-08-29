package net.alfonsormadrid.enchanttransfer.energy;

/**
 * Abstraction for any component that can store experience points.
 * <p>
 * Units are raw XP points (not levels) to avoid the non-linear conversion
 * between Minecraft levels and total XP.
 */
public interface ExperienceStorage {

    /**
     * Attempt to insert the given amount of XP points.
     *
     * @param points   amount to insert
     * @param simulate if {@code true} no state changes are applied
     * @return amount actually inserted (may be less than {@code points} if full)
     */
    int insert(int points, boolean simulate);

    /**
     * Attempt to extract the given amount of XP points.
     *
     * @param points   amount to extract
     * @param simulate if {@code true} no state changes are applied
     * @return amount actually extracted (may be less than {@code points} if empty)
     */
    int extract(int points, boolean simulate);

    int getStored();

    int getCapacity();

    default int getAvailableSpace() {
        return getCapacity() - getStored();
    }

    default boolean isEmpty() {
        return getStored() <= 0;
    }

    default boolean isFull() {
        return getStored() >= getCapacity();
    }
}
