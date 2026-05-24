package net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Slot of the Infusion Coil that accepts only Magic Cards as input for the
 * infusion process.
 */
public class CardInputSlot extends Slot {
    public CardInputSlot(Inventory inventory, int index, SlotPosition position) {
        super(inventory, index, position.positionX, position.positionY);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.isOf(EnchantTransferMod.MAGIC_CARD_ITEM);
    }
}
