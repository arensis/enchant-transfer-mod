package net.alfonsormadrid.enchanttransfer.services;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;

public class CombineCardService {
    ItemStack card1;
    ItemStack card2;

    public CombineCardService(ItemStack card1, ItemStack card2) {
        this.card1 = card1;
        this.card2 = card2;
    }

    public ItemStack combineCards() {
        ItemEnchantmentsComponent enchants1 = card1.getEnchantments();
        Optional<RegistryEntry<Enchantment>> enchantmentEntry = enchants1.getEnchantments().stream().findFirst();

        if (enchantmentEntry.isPresent()) {
            int currentLevel = enchants1.getLevel(enchantmentEntry.get());
            ItemStack outputCard = new ItemStack(EnchantTransferMod.MAGIC_CARD_ITEM);
            outputCard.addEnchantment(enchantmentEntry.get(), currentLevel + 1);
            return outputCard;
        } else {
            return ItemStack.EMPTY;
        }
    }

    public boolean cardsCanCombine() {
        return twoEnchantedCardsInserted() && cardsHaveSameEnchant() && noMaxLevelCards();
    }

    private boolean noMaxLevelCards() {
        ItemEnchantmentsComponent enchants = card1.getEnchantments();
        Optional<RegistryEntry<Enchantment>> enchantmentEntry = enchants.getEnchantments().stream().findFirst();

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
        ItemEnchantmentsComponent enchants1 = card1.getEnchantments();
        ItemEnchantmentsComponent enchants2 = card2.getEnchantments();

        if (enchants1.getEnchantments().size() != enchants2.getEnchantments().size()) {
            return false;
        }

        return enchants1.getEnchantments().stream().allMatch(entry -> {
            int level1 = enchants1.getLevel(entry);
            int level2 = enchants2.getLevel(entry);
            return level1 > 0 && level1 == level2;
        });
    }
}
