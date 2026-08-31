package net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot of the Infusion Coil that accepts any Magic Card variant — base or
 * any of the 6 coloured types — as input for the infusion process.  The
 * coil burns the card for its XP value regardless of its category.
 */
public class CardInputSlot extends Slot {
    public CardInputSlot(Container inventory, int index, SlotPosition position) {
        super(inventory, index, position.positionX, position.positionY);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return stack.getItem() instanceof MagicCardItem;
    }
}
