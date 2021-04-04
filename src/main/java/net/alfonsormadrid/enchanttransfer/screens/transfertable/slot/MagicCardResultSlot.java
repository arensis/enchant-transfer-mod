package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

import java.util.stream.IntStream;

public class MagicCardResultSlot extends Slot {
    private final Inventory combineCardsInput;

    public MagicCardResultSlot(Inventory inventory, Inventory combineCardsInput, Integer index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
        this.combineCardsInput = combineCardsInput;
    }

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
        IntStream.range(0, this.combineCardsInput.size()).forEach(this.combineCardsInput::removeStack);
    }
}
