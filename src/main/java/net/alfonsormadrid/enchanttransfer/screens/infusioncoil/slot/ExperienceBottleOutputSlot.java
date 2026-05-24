package net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/**
 * Output-only slot: the player cannot insert into it, only extract.
 * The infusion process places filled experience bottles here.
 */
public class ExperienceBottleOutputSlot extends Slot {
    public ExperienceBottleOutputSlot(Inventory inventory, int index, SlotPosition position) {
        super(inventory, index, position.positionX, position.positionY);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return false;
    }
}
