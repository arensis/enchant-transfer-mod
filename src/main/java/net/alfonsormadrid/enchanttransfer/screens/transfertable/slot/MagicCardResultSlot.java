package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class MagicCardResultSlot extends Slot {
    private final Container combineCardsInput;

    public MagicCardResultSlot(Container inventory, Container combineCardsInput, Integer index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
        this.combineCardsInput = combineCardsInput;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        decrementCombineCardsInput(stack.getCount());
        super.onTake(player, stack);
    }

    private void decrementCombineCardsInput(int amount) {
        for (int i = 0; i < this.combineCardsInput.getContainerSize(); i++) {
            this.combineCardsInput.getItem(i).shrink(amount);
        }
        this.combineCardsInput.setChanged();
    }
}
