package net.alfonsormadrid.enchanttransfer.screens.carddeck;

import java.util.ArrayList;
import java.util.List;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.item.CardDeckItem;
import net.alfonsormadrid.enchanttransfer.item.CardWear;
import net.alfonsormadrid.enchanttransfer.item.DeckContents;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.item.ModDataComponents;
import net.alfonsormadrid.enchanttransfer.library.DeckCardStorage;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

/**
 * Screen handler for a Card Deck held in hand: its cards laid out in a 9×3
 * grid, followed by 27 inventory + 9 hotbar = 36 player slots.
 * <p>
 * The grid is a view over the deck's {@code deck_contents} component: it's
 * loaded on open and written back on every change.  The 64-point capacity
 * still rules ({@link CardSlot#getMaxStackSize(ItemStack)}); the grid is just
 * how the cards are shown.  The deck's own hotbar slot is locked while open.
 */
public class CardDeckMenu extends AbstractContainerMenu {

    public static final int COLS = 9, ROWS = 3, CARD_SLOTS = COLS * ROWS;
    public static final int GRID_X = 8, GRID_Y = 32;
    public static final int INV_Y = 102, HOTBAR_Y = 160;
    public static final int IMAGE_WIDTH = 176, IMAGE_HEIGHT = 184;

    private static final int PLAYER_INV_START = CARD_SLOTS;
    private static final int HOTBAR_END = PLAYER_INV_START + 36;

    private final Player player;
    private final InteractionHand hand;
    private final @Nullable DeckKind kind;
    /** Hotbar index of the deck when held in the main hand, else -1. */
    private final int lockedHotbar;
    private final SimpleContainer cards = new SimpleContainer(CARD_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            save();
        }
    };
    /** Stacks beyond the grid (only with many unstackable worn cards); kept as they are. */
    private final List<ItemStack> overflow = new ArrayList<>();
    private boolean loading;

    public CardDeckMenu(int syncId, Inventory playerInventory, InteractionHand hand) {
        super(EnchantTransferMod.CARD_DECK_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;
        this.hand = hand;
        ItemStack deck = deck();
        this.kind = deck.getItem() instanceof CardDeckItem item ? item.getKind() : null;
        this.lockedHotbar = hand == InteractionHand.MAIN_HAND ? playerInventory.getSelectedSlot() : -1;
        load(deck);

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                addSlot(new CardSlot(row * COLS + col, GRID_X + col * 18, GRID_Y + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(col == lockedHotbar
                    ? new LockedSlot(playerInventory, col, 8 + col * 18, HOTBAR_Y)
                    : new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    public ItemStack deck() {
        return player.getItemInHand(hand);
    }

    public @Nullable DeckKind getKind() {
        return kind;
    }

    // ── Grid ⇄ deck component ───────────────────────────────────────────

    private void load(ItemStack deck) {
        loading = true;
        // Re-merge equal stacks so the grid shows each kind of card once.
        DeckContents merged = DeckContents.EMPTY;
        for (ItemStack stack : deck.getOrDefault(ModDataComponents.DECK_CONTENTS, DeckContents.EMPTY).stacks()) {
            merged = merged.with(stack);
        }
        List<ItemStack> stacks = merged.stacks();
        for (int i = 0; i < stacks.size(); i++) {
            if (i < CARD_SLOTS) cards.setItem(i, stacks.get(i));
            else overflow.add(stacks.get(i));
        }
        loading = false;
    }

    private void save() {
        if (loading) return;
        ItemStack deck = deck();
        if (!(deck.getItem() instanceof CardDeckItem)) return;
        List<ItemStackTemplate> out = new ArrayList<>();
        for (int i = 0; i < CARD_SLOTS; i++) {
            ItemStack stack = cards.getItem(i);
            if (!stack.isEmpty()) out.add(ItemStackTemplate.fromNonEmptyStack(stack));
        }
        for (ItemStack stack : overflow) out.add(ItemStackTemplate.fromNonEmptyStack(stack));
        deck.set(ModDataComponents.DECK_CONTENTS, new DeckContents(out));
    }

    /** Points of every card in the deck (grid and overflow). */
    public int storedPoints() {
        int points = 0;
        for (int i = 0; i < CARD_SLOTS; i++) points += pointsOf(cards.getItem(i));
        for (ItemStack stack : overflow) points += pointsOf(stack);
        return points;
    }

    /** Wear reserve left across the deck, in {@link CardWear} units. */
    public int reserve() {
        int reserve = 0;
        for (int i = 0; i < CARD_SLOTS; i++) reserve += CardWear.remainingOf(cards.getItem(i)) * cards.getItem(i).getCount();
        for (ItemStack stack : overflow) reserve += CardWear.remainingOf(stack) * stack.getCount();
        return reserve;
    }

    private static int pointsOf(ItemStack stack) {
        return MagicCardItem.pointsOf(stack) * stack.getCount();
    }

    // ── Menu ────────────────────────────────────────────────────────────

    @Override
    public boolean stillValid(Player player) {
        return kind != null && deck().getItem() instanceof CardDeckItem item && item.getKind() == kind;
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput input, Player player) {
        // The deck can't leave the hand while open: no number-key or offhand swap onto it.
        if (input == ContainerInput.SWAP
                && ((lockedHotbar >= 0 && button == lockedHotbar)
                || (hand == InteractionHand.OFF_HAND && button == Inventory.SLOT_OFFHAND))) {
            return;
        }
        super.clicked(slotId, button, input, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (slotIndex < CARD_SLOTS) {
            if (!moveItemStackTo(original, PLAYER_INV_START, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else if (kind == null || !kind.accepts(original)
                || !moveItemStackTo(original, 0, CARD_SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    /** A grid slot: only cards of the deck's type, and only as many as the points allow. */
    private class CardSlot extends Slot {

        CardSlot(int index, int x, int y) {
            super(cards, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return kind != null && kind.accepts(stack) && getMaxStackSize(stack) > 0;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            int perCard = MagicCardItem.pointsOf(stack);
            if (perCard <= 0) return 0;
            int others = storedPoints() - pointsOf(getItem());
            int fit = (DeckCardStorage.CAPACITY_POINTS - others) / perCard;
            return Math.max(0, Math.min(stack.getMaxStackSize(), fit));
        }
    }

    /** The hotbar slot holding the open deck: can't be picked up or replaced. */
    private static class LockedSlot extends Slot {

        LockedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
