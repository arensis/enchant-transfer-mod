package net.alfonsormadrid.enchanttransfer.services;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;

import java.util.Optional;

public class CombineCardService {
    ItemStack card1;
    ItemStack card2;

    public CombineCardService(ItemStack card1, ItemStack card2) {
        this.card1 = card1;
        this.card2 = card2;
    }

    public ItemStack combineCards() {
        Optional<Integer> card1Level = EnchantmentHelper.get(card1).values().stream().findFirst();
        Optional<Integer> card2Level = EnchantmentHelper.get(card2).values().stream().findFirst();
        Optional<Enchantment> enchantment = EnchantmentHelper.get(card1).keySet().stream().findFirst();

        if(card1Level.isPresent() && card2Level.isPresent()) {
            ItemStack outputCard = new ItemStack(EnchantTransferMod.MAGIC_CARD_ITEM);
            Integer updatedLevel = card1Level.get() + 1;
            outputCard.addEnchantment(enchantment.get(), updatedLevel);
            return outputCard;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public boolean cardsCanCombine() {
        return twoEnchantedCardsInserted() && cardsHaveSameEnchant() && noMaxLevelCards();
    }

    private boolean noMaxLevelCards() {
        Optional<Integer> card1Level = EnchantmentHelper.get(card1).values().stream().findFirst();
        Optional<Enchantment> enchantment = EnchantmentHelper.get(card1).keySet().stream().findFirst();

        return card1Level.isPresent() && enchantment.isPresent() && card1Level.get() < enchantment.get().getMaxLevel();
    }

    private boolean twoEnchantedCardsInserted() {
        return !card1.isEmpty() && !card2.isEmpty() && card1.hasEnchantments() && card2.hasEnchantments();
    }

    private boolean cardsHaveSameEnchant() {
        return card1.getEnchantments().containsAll(card2.getEnchantments());
    }
}
