package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

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
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        decrementCombineCardsInput(stack.getCount());
        super.onTakeItem(player, stack);
    }

    private void decrementCombineCardsInput(int amount) {
        for (int i = 0; i < this.combineCardsInput.size(); i++) {
            this.combineCardsInput.getStack(i).decrement(amount);
        }
        this.combineCardsInput.markDirty();
    }
}
