package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransferItemContentSlot extends TransferSlot {
    private final Container itemInventory;

    public TransferItemContentSlot(Container inventory, Container itemInventory, int index, SlotPosition positions) {
        super(inventory, index, positions);
        this.itemInventory = itemInventory;
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return itemInventoryIsNotEmpty() &&
                itemIsMagicCard(stack) &&
                !containsTheSameEnchant(stack) &&
                !containsIncompatibleEnchant(stack) &&
                isEnchantmentAcceptableForItem(stack);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        removeEnchantFromItemInventory(stack);
        super.onTake(player, stack);
    }

    @Override
    public void setByPlayer(ItemStack itemStack) {
        ItemStack current = this.getItem();
        if (!current.isEmpty()) {
            removeEnchantFromItemInventory(current);
        }
        if (!itemStack.isEmpty()) {
            addEnchantToItemInventory(itemStack);
        }
        super.setByPlayer(itemStack);
    }

    private boolean itemInventoryIsNotEmpty() {
        return !this.itemInventory.getItem(0).isEmpty();
    }

    private boolean containsTheSameEnchant(ItemStack stack) {
        return mapItemStacksToEnchants(getAllItemContentInventoryStacks())
                .keySet()
                .stream()
                .anyMatch(entry -> containsEnchantment(stack, entry));
    }

    private boolean containsIncompatibleEnchant(ItemStack stack) {
        ItemStack itemInventoryStack = this.itemInventory.getItem(0);

        if (!isBook(itemInventoryStack.getItem())) {
            Set<Holder<Enchantment>> existingEnchants = itemInventoryStack.getEnchantments().keySet();
            return stack.getEnchantments().keySet()
                    .stream()
                    .anyMatch(entry -> !EnchantmentHelper.isEnchantmentCompatible(existingEnchants, entry));
        }

        return false;
    }

    private void removeEnchantFromItemInventory(ItemStack stack) {
        ItemStack newItemStack = createCopyItemInventoryWith(itemInventoryEnchantsFilteredBy(stack));
        this.itemInventory.setItem(0, newItemStack);
    }

    private void addEnchantToItemInventory(ItemStack stack) {
        ItemStack newItemStack = createCopyItemInventoryWith(mergeItemInventoryEnchantmentsWith(stack));
        this.itemInventory.setItem(0, newItemStack);
    }

    private Map<Holder<Enchantment>, Integer> mapItemStacksToEnchants(List<ItemStack> stacks) {
        Map<Holder<Enchantment>, Integer> enchants = new HashMap<>();
        stacks.forEach(item -> item.getEnchantments().keySet()
                .forEach(entry -> enchants.put(entry, item.getEnchantments().getLevel(entry))));
        return enchants;
    }

    private List<ItemStack> getAllItemContentInventoryStacks() {
        return IntStream.range(0, this.container.getContainerSize())
                .mapToObj(this.container::getItem)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toList());
    }

    private boolean containsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return stack.getEnchantments().getLevel(enchantment) > 0;
    }

    private Map<Holder<Enchantment>, Integer> itemInventoryEnchantsFilteredBy(ItemStack stack) {
        ItemEnchantments component = getEffectiveEnchantments(this.itemInventory.getItem(0));
        return component.keySet().stream()
                .filter(entry -> !containsEnchantment(stack, entry))
                .collect(Collectors.toMap(entry -> entry, component::getLevel));
    }

    private ItemStack createCopyItemInventoryWith(Map<Holder<Enchantment>, Integer> enchants) {
        Item itemInventoryType = getItemType(enchants);
        ItemStack newItemStack = new ItemStack(itemInventoryType);

        if (!isBook(itemInventoryType)) {
            Component customName = this.itemInventory.getItem(0).get(DataComponents.CUSTOM_NAME);
            int originalItemDamage = this.itemInventory.getItem(0).getDamageValue();
            if (customName != null) {
                newItemStack.set(DataComponents.CUSTOM_NAME, customName);
            }
            newItemStack.setDamageValue(originalItemDamage);
        }

        if (isBook(itemInventoryType)) {
            ItemEnchantments.Mutable builder = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
            enchants.forEach(builder::upgrade);
            newItemStack.set(DataComponents.STORED_ENCHANTMENTS, builder.toImmutable());
        } else {
            enchants.forEach(newItemStack::enchant);
        }
        return newItemStack;
    }

    private Map<Holder<Enchantment>, Integer> mergeItemInventoryEnchantmentsWith(ItemStack stack) {
        Map<Holder<Enchantment>, Integer> merged = new HashMap<>();
        ItemEnchantments current = getEffectiveEnchantments(this.itemInventory.getItem(0));
        current.keySet().forEach(entry -> merged.put(entry, current.getLevel(entry)));

        ItemEnchantments newEnchants = stack.getEnchantments();
        newEnchants.keySet().forEach(entry -> merged.put(entry, newEnchants.getLevel(entry)));

        return merged;
    }

    private Item getItemType(Map<Holder<Enchantment>, Integer> enchants) {
        Item itemInventoryType = this.itemInventory.getItem(0).getItem();

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
        ItemStack itemInventoryStack = this.itemInventory.getItem(0);

        if (!isBook(itemInventoryStack.getItem())) {
            return enchantmentStack.getEnchantments().keySet()
                    .stream()
                    .allMatch(entry -> entry.value().canEnchant(itemInventoryStack));
        }

        return true;
    }
}
