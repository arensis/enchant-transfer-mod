package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferTableInventorySlots;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public abstract class MagicCardSlot extends Slot {

//    protected final Inventory outputInventory;

    public MagicCardSlot(Inventory inventory, Integer index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
    }

//    public MagicCardSlot(Inventory inventory, Inventory outputInventory, Integer index, SlotPosition positions) {
//        super(inventory, index, positions.positionX, positions.positionY);
//        this.outputInventory = outputInventory;
//    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return itemIsMagicCard(stack);
    }

    @Override
    public void setStack(ItemStack stack) {
        updateCombineCardsOutput();
        super.setStack(stack);
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        updateCombineCardsOutput();
        return super.onTakeItem(player, stack);
    }

    protected void updateCombineCardsOutput() {
        if (cardsCanCombine()) {
            this.inventory.setStack(TransferTableInventorySlots.OUTPUT_CARD_SLOT.getSlotIndex(), combineCards());
//            this.outputInventory.setStack(0, combineCards());
        } else {
            this.inventory.setStack(TransferTableInventorySlots.OUTPUT_CARD_SLOT.getSlotIndex(), ItemStack.EMPTY);
//            this.outputInventory.setStack(0, ItemStack.EMPTY);
        }
    }

    protected boolean itemIsMagicCard(ItemStack stack) {
        return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
    }

//    abstract protected Integer currentSlot();
//    abstract protected Integer otherCardSlot();
    abstract protected boolean cardsCanCombine();
    abstract protected ItemStack combineCards();
}
