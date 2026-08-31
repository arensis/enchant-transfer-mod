package net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Output-only slot: the player cannot insert into it, only extract.
 * The infusion process places filled experience bottles here.
 */
public class ExperienceBottleOutputSlot extends Slot {
    public ExperienceBottleOutputSlot(Container inventory, int index, SlotPosition position) {
        super(inventory, index, position.positionX, position.positionY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
