package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;


public class TransferSlot extends Slot {
    public TransferSlot(Inventory inventory, int index, SlotPosition positions) {
        super(inventory, index, positions.positionX, positions.positionY);
    }

    protected boolean isEnchantedBook(Item item) {
        return item == Items.ENCHANTED_BOOK;
    }

    /**
     * Any {@link MagicCardItem} qualifies — both the base/blank card and
     * the 6 coloured variants.  We check the class rather than a specific
     * instance so adding new card types in {@code EnchantTransferMod}
     * doesn't require updating each slot's predicate.
     */
    protected boolean itemIsMagicCard(ItemStack stack) {
        return stack.getItem() instanceof MagicCardItem;
    }

    protected ItemEnchantmentsComponent getEffectiveEnchantments(ItemStack stack) {
        if (isEnchantedBook(stack.getItem())) {
            ItemEnchantmentsComponent stored = stack.get(DataComponentTypes.STORED_ENCHANTMENTS);
            return stored != null ? stored : ItemEnchantmentsComponent.DEFAULT;
        }
        return stack.getEnchantments();
    }
}
