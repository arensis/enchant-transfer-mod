package net.alfonsormadrid.enchanttransfer.energy;

import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

/**
 * In-memory implementation of {@link ExperienceStorage} with NBT serialization.
 * <p>
 * Thread-safety is not enforced: all access is expected from the server thread,
 * mirroring vanilla {@code BlockEntity} conventions.
 */
public class SimpleExperienceTank implements ExperienceStorage {

    private static final String NBT_STORED = "Stored";

    private final int capacity;
    private int stored;

    public SimpleExperienceTank(int capacity) {
        this(capacity, 0);
    }

    public SimpleExperienceTank(int capacity, int initialStored) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity must be non-negative");
        }
        this.capacity = capacity;
        this.stored = Math.max(0, Math.min(initialStored, capacity));
    }

    @Override
    public int insert(int points, boolean simulate) {
        if (points <= 0) return 0;
        int accepted = Math.min(points, getAvailableSpace());
        if (!simulate) stored += accepted;
        return accepted;
    }

    @Override
    public int extract(int points, boolean simulate) {
        if (points <= 0) return 0;
        int removed = Math.min(points, stored);
        if (!simulate) stored -= removed;
        return removed;
    }

    @Override
    public int getStored() {
        return stored;
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    /** Directly overwrite the stored amount. Used for client-side sync via PropertyDelegate. */
    public void setStored(int value) {
        this.stored = Math.max(0, Math.min(value, capacity));
    }

    public void writeData(WriteView view) {
        view.putInt(NBT_STORED, stored);
    }

    public void readData(ReadView view) {
        this.stored = Math.max(0, Math.min(view.getInt(NBT_STORED, 0), capacity));
    }
}
