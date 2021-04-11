package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.CombineCardSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferTableInventorySlots;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.stream.IntStream;

public class MagicCardResultSlot extends Slot {
//    private final Inventory combineCardsInput;

    public MagicCardResultSlot(Inventory inventory) {
        super(inventory, TransferTableInventorySlots.OUTPUT_CARD_SLOT.getSlotIndex(), CombineCardSlotPositions.resultCard.positionX, CombineCardSlotPositions.resultCard.positionY);
    }

//    public MagicCardResultSlot(Inventory inventory, Inventory inputInventory) {
//        super(inventory, 0, CombineCardSlotPositions.resultCard.positionX, CombineCardSlotPositions.resultCard.positionY);
//        this.combineCardsInput = inputInventory;
//    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        removeCombineCardsInputStack();
        return super.onTakeItem(player, stack);
    }

    private void removeCombineCardsInputStack() {
        this.inventory.removeStack(TransferTableInventorySlots.FIRST_MAGIC_CARD_SLOT.getSlotIndex());
        this.inventory.removeStack(TransferTableInventorySlots.SECOND_MAGIC_CARD_SLOT.getSlotIndex());
//        IntStream.range(0, this.combineCardsInput.size()).forEach(this.combineCardsInput::removeStack);
    }
}
