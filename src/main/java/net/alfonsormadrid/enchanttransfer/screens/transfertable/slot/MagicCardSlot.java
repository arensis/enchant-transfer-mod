package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MagicCardSlot extends Slot {
    public MagicCardSlot(Inventory inventory, Integer index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        // Any MagicCardItem instance — base or coloured — is accepted.
        return stack.getItem() instanceof MagicCardItem;
    }
}
