package net.alfonsormadrid.enchanttransfer.library;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * Adapter that exposes any vanilla {@link Container} as a {@link DeckLibrary}.
 * Used for the Chiseled Bookshelf, whose {@code setItem} already updates its
 * block state and marks it dirty.
 */
public class ContainerDeckLibrary implements DeckLibrary {

    private final Container container;

    public ContainerDeckLibrary(Container container) {
        this.container = container;
    }

    @Override
    public int getSlotCount() {
        return container.getContainerSize();
    }

    @Override
    public ItemStack getStack(int slot) {
        return container.getItem(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        container.setItem(slot, stack);
        container.setChanged();
    }
}
