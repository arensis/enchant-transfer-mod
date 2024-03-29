package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.Map;
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
        return !itemIsMagicCard(stack) && (stack.isEnchantable() || stack.hasEnchantments() || isEnchantedBook(stack.getItem()));
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        removeAllItemContentInventoryStacks();
        return super.onTakeItem(player, stack);
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(itemStack);
        updateItemContentInventory(enchants);
    }

    private void removeAllItemContentInventoryStacks() {
        IntStream.range(0, this.itemContentInventory.size()).forEach(this.itemContentInventory::removeStack);
    }

    private void updateItemContentInventory(Map<Enchantment, Integer> enchants) {
        addMagiCardsToItemContentInventory(buildMagicCardsFromEnchants(enchants));
    }

    private void addMagiCardsToItemContentInventory(List<ItemStack> magicCards) {
        if (this.itemContentInventory.isEmpty()) {
            IntStream.range(0, magicCards.size())
                    .forEach(index -> this.itemContentInventory.setStack(index, magicCards.get(index)));
        }
    }

    private List<ItemStack> buildMagicCardsFromEnchants(Map<Enchantment, Integer> enchants) {
        return enchants.entrySet().stream().map(enchantmentEntry -> {
            ItemStack magicCardEnchanted = new ItemStack(EnchantTransferMod.MAGIC_CARD_ITEM);
            magicCardEnchanted.addEnchantment(enchantmentEntry.getKey(), enchantmentEntry.getValue());
            return magicCardEnchanted;
        }).collect(Collectors.toList());
    }
}
