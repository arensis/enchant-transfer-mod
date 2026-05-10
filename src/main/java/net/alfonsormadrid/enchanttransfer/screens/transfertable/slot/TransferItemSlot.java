package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransferItemSlot extends TransferSlot {
    private final Inventory itemContentInventory;

    public TransferItemSlot(Inventory inventory, Inventory itemContentInventory, SlotPosition slotPosition) {
        super(inventory, 0, slotPosition);
        this.itemContentInventory = itemContentInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return !itemIsMagicCard(stack) && (stack.isEnchantable()
                || !stack.getEnchantments().isEmpty()
                || isEnchantedBook(stack.getItem())
                || stack.getItem() == Items.BOOK);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        removeAllItemContentInventoryStacks();
        super.onTakeItem(player, stack);
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        removeAllItemContentInventoryStacks();
        List<ItemStack> magicCards = buildMagicCardsFromEnchants(getEffectiveEnchantments(itemStack));
        IntStream.range(0, magicCards.size())
                .forEach(index -> this.itemContentInventory.setStack(index, magicCards.get(index)));
    }

    private void removeAllItemContentInventoryStacks() {
        IntStream.range(0, this.itemContentInventory.size()).forEach(this.itemContentInventory::removeStack);
    }

    private List<ItemStack> buildMagicCardsFromEnchants(ItemEnchantmentsComponent enchants) {
        return enchants.getEnchantments().stream().map(entry -> {
            ItemStack magicCardEnchanted = new ItemStack(EnchantTransferMod.MAGIC_CARD_ITEM);
            magicCardEnchanted.addEnchantment(entry, enchants.getLevel(entry));
            return magicCardEnchanted;
        }).collect(Collectors.toList());
    }
}
