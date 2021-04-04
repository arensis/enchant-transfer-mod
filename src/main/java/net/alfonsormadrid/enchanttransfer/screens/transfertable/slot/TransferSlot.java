package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;


public class TransferSlot extends Slot {
    public TransferSlot(Inventory inventory, int index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
    }

    protected boolean isEnchantedBook(Item item) {
        return item == Items.ENCHANTED_BOOK;
    }

    protected boolean itemIsMagicCard(ItemStack stack) {
        return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
    }
}
