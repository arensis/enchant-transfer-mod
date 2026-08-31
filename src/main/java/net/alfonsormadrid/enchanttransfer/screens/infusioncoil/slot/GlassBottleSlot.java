package net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Slot of the Infusion Coil that accepts only empty glass bottles. They are
 * filled with XP from the tank and emerge as experience bottles in the
 * {@link ExperienceBottleOutputSlot}.
 */
public class GlassBottleSlot extends Slot {
    public GlassBottleSlot(Container inventory, int index, SlotPosition position) {
        super(inventory, index, position.positionX, position.positionY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.is(Items.GLASS_BOTTLE);
    }
}
