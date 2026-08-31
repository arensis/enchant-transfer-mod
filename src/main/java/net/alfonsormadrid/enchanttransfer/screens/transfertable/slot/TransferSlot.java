package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;


public class TransferSlot extends Slot {
    public TransferSlot(Container inventory, int index, SlotPosition positions) {
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

    protected ItemEnchantments getEffectiveEnchantments(ItemStack stack) {
        if (isEnchantedBook(stack.getItem())) {
            ItemEnchantments stored = stack.get(DataComponents.STORED_ENCHANTMENTS);
            return stored != null ? stored : ItemEnchantments.EMPTY;
        }
        return stack.getEnchantments();
    }
}
