package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;
import net.alfonsormadrid.enchanttransfer.item.CardType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TransferItemSlot extends TransferSlot {
    private final Container itemContentInventory;

    public TransferItemSlot(Container inventory, Container itemContentInventory, SlotPosition slotPosition) {
        super(inventory, 0, slotPosition);
        this.itemContentInventory = itemContentInventory;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !itemIsMagicCard(stack) && (stack.isEnchantable()
                || !stack.getEnchantments().isEmpty()
                || isEnchantedBook(stack.getItem())
                || stack.getItem() == Items.BOOK);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        removeAllItemContentInventoryStacks();
        super.onTake(player, stack);
    }

    @Override
    public void setByPlayer(ItemStack itemStack) {
        super.setByPlayer(itemStack);
        removeAllItemContentInventoryStacks();
        List<ItemStack> magicCards = buildMagicCardsFromEnchants(getEffectiveEnchantments(itemStack));
        IntStream.range(0, magicCards.size())
                .forEach(index -> this.itemContentInventory.setItem(index, magicCards.get(index)));
    }

    private void removeAllItemContentInventoryStacks() {
        IntStream.range(0, this.itemContentInventory.getContainerSize()).forEach(this.itemContentInventory::removeItemNoUpdate);
    }

    private List<ItemStack> buildMagicCardsFromEnchants(ItemEnchantments enchants) {
        return enchants.keySet().stream().map(entry -> {
            // Look up which category this enchantment belongs to.  If it's a
            // modded enchantment we don't know about (or a vanilla one we
            // haven't categorised yet), fall back to the blank/base card so
            // the system degrades gracefully — the player still gets a card
            // with the enchantment, just without its colour identity.
            ItemStack card = new ItemStack(cardItemFor(cardTypeFor(entry)));
            card.enchant(entry, enchants.getLevel(entry));
            return card;
        }).collect(Collectors.toList());
    }

    private static @Nullable CardType cardTypeFor(Holder<Enchantment> entry) {
        ResourceKey<Enchantment> key = entry.unwrapKey().orElse(null);
        return key != null ? CardType.forEnchantment(key) : null;
    }

    /**
     * Maps a {@link CardType} to the registered {@code MagicCardItem}
     * variant.  Kept as a switch (rather than a Map field on CardType) to
     * avoid the cyclical dependency between CardType and EnchantTransferMod's
     * static item fields.
     */
    private static Item cardItemFor(@Nullable CardType type) {
        if (type == null) return EnchantTransferMod.MAGIC_CARD_ITEM;
        return switch (type) {
            case BLUE   -> EnchantTransferMod.MAGIC_CARD_BLUE;
            case GREEN  -> EnchantTransferMod.MAGIC_CARD_GREEN;
            case RED    -> EnchantTransferMod.MAGIC_CARD_RED;
            case YELLOW -> EnchantTransferMod.MAGIC_CARD_YELLOW;
            case PURPLE -> EnchantTransferMod.MAGIC_CARD_PURPLE;
            case BLACK  -> EnchantTransferMod.MAGIC_CARD_BLACK;
        };
    }
}
