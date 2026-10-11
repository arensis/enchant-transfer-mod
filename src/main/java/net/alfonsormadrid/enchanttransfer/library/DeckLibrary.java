package net.alfonsormadrid.enchanttransfer.library;

import net.minecraft.world.item.ItemStack;

/**
 * Contract any block (entity) must satisfy to hold Card Decks for a nearby
 * enchanting table.
 * <p>
 * Same role as {@link net.alfonsormadrid.enchanttransfer.modules.TransferTableModule}
 * for the Transfer Table: the enchanting logic only knows libraries through
 * this interface.  Vanilla's Chiseled Bookshelf is wrapped by
 * {@link ContainerDeckLibrary}; the mod's own library block will implement
 * it directly.
 */
public interface DeckLibrary {

    int getSlotCount();

    /** Item in a slot — may be empty or something other than a deck. */
    ItemStack getStack(int slot);

    /** Replace a slot's stack (used to write back a worn deck). */
    void setStack(int slot, ItemStack stack);
}
