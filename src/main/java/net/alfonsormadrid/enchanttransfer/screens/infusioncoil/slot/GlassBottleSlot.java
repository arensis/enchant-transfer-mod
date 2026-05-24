package net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;

/**
 * Slot of the Infusion Coil that accepts only empty glass bottles. They are
 * filled with XP from the tank and emerge as experience bottles in the
 * {@link ExperienceBottleOutputSlot}.
 */
public class GlassBottleSlot extends Slot {
    public GlassBottleSlot(Inventory inventory, int index, SlotPosition position) {
        super(inventory, index, position.positionX, position.positionY);
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return stack.isOf(Items.GLASS_BOTTLE);
    }
}
