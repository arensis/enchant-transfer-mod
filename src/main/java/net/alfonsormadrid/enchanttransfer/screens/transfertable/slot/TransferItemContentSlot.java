package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransferItemContentSlot extends TransferSlot {
    private Inventory itemInventory;

    public TransferItemContentSlot(Inventory inventory, Inventory itemInventory, int index, SlotPosition positions) {
        super(inventory, index, positions);
        this.itemInventory = itemInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return itemInventoryIsNotEmpty() && itemIsMagicCard(stack) && !containsTheSameEnchant(stack) && !containsIncompatibleEnchant(stack);
    }

    @Override
    public ItemStack onTakeItem(PlayerEntity player, ItemStack stack) {
        removeEnchantFromItemInventory(stack);
        return super.onTakeItem(player, stack);
    }

    @Override
    public void setStack(ItemStack itemStack) {
        super.setStack(itemStack);
        Map<Enchantment, Integer> enchants = EnchantmentHelper.get(itemStack);
        updateItemInventoryStack(enchants, false);
    }

    private boolean itemInventoryIsNotEmpty() { return !this.itemInventory.getStack(0).isEmpty(); }

    private boolean containsTheSameEnchant(ItemStack stack) {
        return mapItemStacksToEnchants(getAllItemContentInventoryStacks())
                .entrySet()
                .stream()
                .anyMatch(entry-> containsEnchantment(stack, entry.getKey()));
    }

    private boolean containsIncompatibleEnchant(ItemStack stack) {
        //TODO: Implement logic for incompatible enchants
        return false;
    }

    private void removeEnchantFromItemInventory(ItemStack stack) {
        updateItemInventoryStack(itemInventoryEnchantsFilteredBy(stack), true);
    }

    private Map<Enchantment, Integer> mapItemStacksToEnchants(List<ItemStack> stacks) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        stacks.forEach(item -> EnchantmentHelper.get(item).forEach(enchants::put));
        return enchants;
    }

    private List<ItemStack> getAllItemContentInventoryStacks() {
        return IntStream.range(0, this.inventory.size())
                .mapToObj(this.inventory::getStack)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean containsEnchantment(ItemStack stack, Enchantment enchantment) {
        return EnchantmentHelper.get(stack).containsKey(enchantment);
    }

    private Map<Enchantment, Integer> itemInventoryEnchantsFilteredBy(ItemStack stack) {
        return EnchantmentHelper.get(this.itemInventory.getStack(0))
                .entrySet()
                .stream()
                .filter(entry -> !containsEnchantment(stack, entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    protected void updateItemInventoryStack(Map<Enchantment, Integer> enchants, boolean onTakeItem) {
        ItemStack newItemStack = createCopyItemInventoryWith(enchants, onTakeItem);
        this.itemInventory.setStack(0, newItemStack);
    }

    private ItemStack createCopyItemInventoryWith(Map<Enchantment, Integer> enchants, boolean onTakeItem) {
        Map<Enchantment, Integer> allEnchantments = onTakeItem ? getItemInventoryEnchantmentsWith(enchants) : enchants;
        Item itemInventoryType = getItemType(allEnchantments);
        ItemStack newItemStack = new ItemStack(itemInventoryType);

        if(!isEnchantedBook(itemInventoryType) && !isBookItem(itemInventoryType)) {
            Text oldText = this.itemInventory.getStack(0).getName();
            newItemStack.setCustomName(oldText);
        }

        EnchantmentHelper.set(allEnchantments, newItemStack);
        return newItemStack;
    }

    private Map<Enchantment, Integer> getItemInventoryEnchantmentsWith(Map<Enchantment, Integer> enchants) {
        ItemStack itemInventoryStack = this.itemInventory.getStack(0);
        if(itemInventoryStack.hasEnchantments()) {
            enchants.putAll(EnchantmentHelper.get(itemInventoryStack));
        }

        return enchants;
    }

    private Item getItemType(Map<Enchantment, Integer> enchants) {
        Item itemInventoryType = this.itemInventory.getStack(0).getItem();

        if(enchants.isEmpty() && isEnchantedBook(itemInventoryType)) {
            return Items.BOOK;
        }

        if(!enchants.isEmpty() && isBookItem(itemInventoryType)) {
            return Items.ENCHANTED_BOOK;
        }

        return itemInventoryType;
    }

    protected boolean isBookItem(Item item) { return item == Items.BOOK; }
}
