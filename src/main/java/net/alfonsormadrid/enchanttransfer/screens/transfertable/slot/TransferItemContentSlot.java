package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.SlotPosition;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransferItemContentSlot extends TransferSlot {
    private final Inventory itemInventory;

    public TransferItemContentSlot(Inventory inventory, Inventory itemInventory, int index, SlotPosition positions) {
        super(inventory, index, positions);
        this.itemInventory = itemInventory;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return itemInventoryIsNotEmpty() &&
                itemIsMagicCard(stack) &&
                !containsTheSameEnchant(stack) &&
                !containsIncompatibleEnchant(stack) &&
                isEnchantmentAcceptableForItem(stack);
    }

    @Override
    public void onTakeItem(PlayerEntity player, ItemStack stack) {
        removeEnchantFromItemInventory(stack);
        super.onTakeItem(player, stack);
    }

    @Override
    public void setStack(ItemStack itemStack) {
        addEnchantToItemInventory(itemStack);
        super.setStack(itemStack);
    }

    private boolean itemInventoryIsNotEmpty() {
        return !this.itemInventory.getStack(0).isEmpty();
    }

    private boolean containsTheSameEnchant(ItemStack stack) {
        return mapItemStacksToEnchants(getAllItemContentInventoryStacks())
                .keySet()
                .stream()
                .anyMatch(entry -> containsEnchantment(stack, entry));
    }

    private boolean containsIncompatibleEnchant(ItemStack stack) {
        ItemStack itemInventoryStack = this.itemInventory.getStack(0);

        if (!isBook(itemInventoryStack.getItem())) {
            Set<RegistryEntry<Enchantment>> existingEnchants = itemInventoryStack.getEnchantments().getEnchantments();
            return stack.getEnchantments().getEnchantments()
                    .stream()
                    .anyMatch(entry -> !EnchantmentHelper.isCompatible(existingEnchants, entry));
        }

        return false;
    }

    private void removeEnchantFromItemInventory(ItemStack stack) {
        ItemStack newItemStack = createCopyItemInventoryWith(itemInventoryEnchantsFilteredBy(stack));
        this.itemInventory.setStack(0, newItemStack);
    }

    private void addEnchantToItemInventory(ItemStack stack) {
        ItemStack newItemStack = createCopyItemInventoryWith(mergeItemInventoryEnchantmentsWith(stack));
        this.itemInventory.setStack(0, newItemStack);
    }

    private Map<RegistryEntry<Enchantment>, Integer> mapItemStacksToEnchants(List<ItemStack> stacks) {
        Map<RegistryEntry<Enchantment>, Integer> enchants = new HashMap<>();
        stacks.forEach(item -> item.getEnchantments().getEnchantments()
                .forEach(entry -> enchants.put(entry, item.getEnchantments().getLevel(entry))));
        return enchants;
    }

    private List<ItemStack> getAllItemContentInventoryStacks() {
        return IntStream.range(0, this.inventory.size())
                .mapToObj(this.inventory::getStack)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean containsEnchantment(ItemStack stack, RegistryEntry<Enchantment> enchantment) {
        return stack.getEnchantments().getLevel(enchantment) > 0;
    }

    private Map<RegistryEntry<Enchantment>, Integer> itemInventoryEnchantsFilteredBy(ItemStack stack) {
        ItemEnchantmentsComponent component = this.itemInventory.getStack(0).getEnchantments();
        return component.getEnchantments().stream()
                .filter(entry -> !containsEnchantment(stack, entry))
                .collect(Collectors.toMap(entry -> entry, component::getLevel));
    }

    private ItemStack createCopyItemInventoryWith(Map<RegistryEntry<Enchantment>, Integer> enchants) {
        Item itemInventoryType = getItemType(enchants);
        ItemStack newItemStack = new ItemStack(itemInventoryType);

        if (!isBook(itemInventoryType)) {
            Text customName = this.itemInventory.getStack(0).get(DataComponentTypes.CUSTOM_NAME);
            int originalItemDamage = this.itemInventory.getStack(0).getDamage();
            if (customName != null) {
                newItemStack.set(DataComponentTypes.CUSTOM_NAME, customName);
            }
            newItemStack.setDamage(originalItemDamage);
        }

        enchants.forEach(newItemStack::addEnchantment);
        return newItemStack;
    }

    private Map<RegistryEntry<Enchantment>, Integer> mergeItemInventoryEnchantmentsWith(ItemStack stack) {
        Map<RegistryEntry<Enchantment>, Integer> merged = new HashMap<>();
        ItemEnchantmentsComponent current = this.itemInventory.getStack(0).getEnchantments();
        current.getEnchantments().forEach(entry -> merged.put(entry, current.getLevel(entry)));

        ItemEnchantmentsComponent newEnchants = stack.getEnchantments();
        newEnchants.getEnchantments().forEach(entry -> merged.put(entry, newEnchants.getLevel(entry)));

        return merged;
    }

    private Item getItemType(Map<RegistryEntry<Enchantment>, Integer> enchants) {
        Item itemInventoryType = this.itemInventory.getStack(0).getItem();

        if (enchants.isEmpty() && isEnchantedBook(itemInventoryType)) {
            return Items.BOOK;
        }

        if (!enchants.isEmpty() && isUnEnchantedBookItem(itemInventoryType)) {
            return Items.ENCHANTED_BOOK;
        }

        return itemInventoryType;
    }

    private boolean isBook(Item item) {
        return isEnchantedBook(item) || isUnEnchantedBookItem(item);
    }

    private boolean isUnEnchantedBookItem(Item item) {
        return item == Items.BOOK;
    }

    private boolean isEnchantmentAcceptableForItem(ItemStack enchantmentStack) {
        ItemStack itemInventoryStack = this.itemInventory.getStack(0);

        if (!isBook(itemInventoryStack.getItem())) {
            return enchantmentStack.getEnchantments().getEnchantments()
                    .stream()
                    .allMatch(entry -> entry.value().isAcceptableItem(itemInventoryStack));
        }

        return true;
    }
}
