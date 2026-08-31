package net.alfonsormadrid.enchanttransfer.services;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class CombineCardService {
    ItemStack card1;
    ItemStack card2;

    public CombineCardService(ItemStack card1, ItemStack card2) {
        this.card1 = card1;
        this.card2 = card2;
    }

    public ItemStack combineCards() {
        ItemEnchantments enchants1 = card1.getEnchantments();
        Optional<Holder<Enchantment>> enchantmentEntry = enchants1.keySet().stream().findFirst();

        if (enchantmentEntry.isPresent()) {
            int currentLevel = enchants1.getLevel(enchantmentEntry.get());
            int resultCount = Math.min(card1.getCount(), card2.getCount());
            // Preserve the input cards' colour: both inputs must share the
            // same enchantment to be combinable, so they're already the same
            // CardType — reusing card1.getItem() avoids a separate lookup.
            ItemStack outputCard = new ItemStack(card1.getItem(), resultCount);
            outputCard.enchant(enchantmentEntry.get(), currentLevel + 1);
            return outputCard;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public boolean cardsCanCombine() {
        return twoEnchantedCardsInserted() && cardsHaveSameEnchant() && noMaxLevelCards();
    }

    private boolean noMaxLevelCards() {
        ItemEnchantments enchants = card1.getEnchantments();
        Optional<Holder<Enchantment>> enchantmentEntry = enchants.keySet().stream().findFirst();

        return enchantmentEntry.isPresent()
                && enchants.getLevel(enchantmentEntry.get()) < enchantmentEntry.get().value().getMaxLevel();
    }

    private boolean twoEnchantedCardsInserted() {
        return !card1.isEmpty() && !card2.isEmpty()
                && !card1.getEnchantments().isEmpty()
                && !card2.getEnchantments().isEmpty();
    }

    public void setCard1(ItemStack card1) {
        this.card1 = card1;
    }

    public void setCard2(ItemStack card2) {
        this.card2 = card2;
    }

    private boolean cardsHaveSameEnchant() {
        ItemEnchantments enchants1 = card1.getEnchantments();
        ItemEnchantments enchants2 = card2.getEnchantments();

        if (enchants1.keySet().size() != enchants2.keySet().size()) {
            return false;
        }

        return enchants1.keySet().stream().allMatch(entry -> {
            int level1 = enchants1.getLevel(entry);
            int level2 = enchants2.getLevel(entry);
            return level1 > 0 && level1 == level2;
        });
    }
}
