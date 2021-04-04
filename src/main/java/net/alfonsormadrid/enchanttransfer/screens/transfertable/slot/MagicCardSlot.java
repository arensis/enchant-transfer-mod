package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class MagicCardSlot extends Slot {
    public MagicCardSlot(Inventory inventory, Integer index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return itemIsMagicCard(stack);
    }

    private boolean itemIsMagicCard(ItemStack stack) {
        return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
    }
}
