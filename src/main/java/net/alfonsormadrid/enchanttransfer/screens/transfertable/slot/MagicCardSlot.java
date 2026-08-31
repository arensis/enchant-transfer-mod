package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MagicCardSlot extends Slot {
    public MagicCardSlot(Container inventory, Integer index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        // Any MagicCardItem instance — base or coloured — is accepted.
        return stack.getItem() instanceof MagicCardItem;
    }
}
