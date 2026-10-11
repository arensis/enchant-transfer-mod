package net.alfonsormadrid.enchanttransfer.screens.decklectern.slot;

import net.alfonsormadrid.enchanttransfer.blocks.decklectern.DeckLecternBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Deck Lectern slot: one Card Deck, nothing else. */
public class DeckSlot extends Slot {

    public DeckSlot(Container inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return DeckLecternBlockEntity.isDeck(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
