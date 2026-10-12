package net.alfonsormadrid.enchanttransfer.item;

import java.util.List;
import java.util.function.Consumer;
import net.alfonsormadrid.enchanttransfer.library.CardStorage;
import net.alfonsormadrid.enchanttransfer.library.DeckCardStorage;
import net.alfonsormadrid.enchanttransfer.screens.carddeck.CardDeckMenu;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

/**
 * Card Deck: a brass-bound case that stores Magic Cards of one
 * {@link DeckKind}.  The kind is fixed at crafting time (the recipe consumes
 * a card of that kind).
 * <p>
 * Storage goes through {@link CardStorage} ({@link #getStorage}), the same
 * way modules use {@code ExperienceStorage}.  Bundle-style interaction:
 * <ul>
 *   <li>Deck on the cursor, right-click a card → stores it; right-click an
 *       empty slot → takes the last stack out.</li>
 *   <li>Card on the cursor, right-click the deck → stores it; empty cursor
 *       → takes the last stack out onto the cursor.</li>
 * </ul>
 * Right-clicking with the deck in hand opens its own screen
 * ({@link CardDeckMenu}) to lay the cards out and move them at a glance.
 * <p>
 * Placed in a library near an enchanting table, it changes what the table
 * offers (see {@code LibraryResonanceService}).
 */
public class CardDeckItem extends Item {

    private static final int MAX_TOOLTIP_ENTRIES = 6;
    private static final int BAR_WIDTH = 13;

    private final DeckKind kind;

    public CardDeckItem(ResourceKey<Item> registryKey, DeckKind kind) {
        super(new Item.Properties()
                .setId(registryKey)
                .stacksTo(1)
                .component(ModDataComponents.DECK_CONTENTS, DeckContents.EMPTY));
        this.kind = kind;
    }

    public DeckKind getKind() {
        return kind;
    }

    /** Card storage view over {@code deck}; writes go to the stack's component. */
    public DeckCardStorage getStorage(ItemStack deck) {
        return new DeckCardStorage(deck, kind);
    }

    // ── Deck screen ──────────────────────────────────────────────────────

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack deck = player.getItemInHand(hand);
            player.openMenu(new ExtendedMenuProvider<InteractionHand>() {
                @Override
                public InteractionHand getScreenOpeningData(ServerPlayer serverPlayer) {
                    return hand;
                }

                @Override
                public Component getDisplayName() {
                    return deck.getHoverName();
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inventory, Player p) {
                    return new CardDeckMenu(syncId, inventory, hand);
                }
            });
        }
        return InteractionResult.SUCCESS;
    }

    // ── Inventory interaction (bundle-style) ─────────────────────────────

    @Override
    public boolean overrideStackedOnOther(ItemStack deck, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;
        CardStorage storage = getStorage(deck);
        ItemStack other = slot.getItem();

        if (other.isEmpty()) {
            ItemStack extracted = storage.extractLast(false);
            if (extracted.isEmpty()) return false;
            ItemStack rest = slot.safeInsert(extracted);
            if (!rest.isEmpty()) storage.insert(rest, false);
            playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
            return true;
        }

        if (!storage.accepts(other)) return false;
        int accepted = storage.insert(other, true);
        if (accepted <= 0) {
            playSound(player, SoundEvents.BUNDLE_INSERT_FAIL);
            return true;
        }
        storage.insert(slot.safeTake(other.getCount(), accepted, player), false);
        playSound(player, SoundEvents.BUNDLE_INSERT);
        return true;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack deck, ItemStack other, Slot slot, ClickAction action,
                                            Player player, SlotAccess carried) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;
        CardStorage storage = getStorage(deck);

        if (other.isEmpty()) {
            ItemStack extracted = storage.extractLast(false);
            if (extracted.isEmpty()) return false;
            carried.set(extracted);
            playSound(player, SoundEvents.BUNDLE_REMOVE_ONE);
            return true;
        }

        if (!storage.accepts(other)) return false;
        int accepted = storage.insert(other, false);
        if (accepted <= 0) {
            playSound(player, SoundEvents.BUNDLE_INSERT_FAIL);
            return true;
        }
        other.shrink(accepted);
        playSound(player, SoundEvents.BUNDLE_INSERT);
        return true;
    }

    private static void playSound(Entity entity, SoundEvent sound) {
        entity.playSound(sound, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    // ── Fill bar & tooltip ───────────────────────────────────────────────

    @Override
    public boolean isBarVisible(ItemStack deck) {
        return !getStorage(deck).isEmpty();
    }

    @Override
    public int getBarWidth(ItemStack deck) {
        CardStorage storage = getStorage(deck);
        return Math.min(BAR_WIDTH, Math.round(BAR_WIDTH * (float) storage.getStoredPoints() / storage.getCapacityPoints()));
    }

    @Override
    public int getBarColor(ItemStack deck) {
        return kind.barColor();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack deck, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        DeckCardStorage storage = getStorage(deck);
        tooltip.accept(Component.translatable("enchanttransfer.deck.effect." + kind.id())
                .withStyle(ChatFormatting.DARK_AQUA));
        tooltip.accept(Component.translatable("enchanttransfer.deck.points",
                storage.getStoredPoints(), storage.getCapacityPoints()).withStyle(ChatFormatting.GRAY));
        // Reserve only differs from the points once a card is worn.
        int fullReserve = storage.getStoredPoints() * CardWear.UNITS_PER_POINT;
        if (storage.getReserve() < fullReserve) {
            tooltip.accept(Component.translatable("enchanttransfer.card.reserve",
                    CardWear.format(storage.getReserve()), CardWear.format(fullReserve)).withStyle(ChatFormatting.GRAY));
        }

        if (storage.isEmpty()) {
            tooltip.accept(Component.translatable("enchanttransfer.deck.hint." + kind.id())
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
            return;
        }

        List<ItemStack> stacks = storage.getContents().sortedForDisplay();
        for (int i = 0; i < Math.min(stacks.size(), MAX_TOOLTIP_ENTRIES); i++) {
            ItemStack card = stacks.get(i);
            MagicCardItem.enchantmentOf(card).ifPresent(e -> {
                var line = Component.literal("  ")
                        .append(Enchantment.getFullname(e, MagicCardItem.levelOf(card)))
                        .append(Component.literal(" ×" + card.getCount()).withStyle(ChatFormatting.DARK_GRAY));
                if (CardWear.wornOf(card) > 0) {
                    line.append(Component.literal(" (" + CardWear.format(CardWear.remainingOf(card))
                            + "/" + CardWear.format(CardWear.reserveOf(card)) + ")").withStyle(ChatFormatting.DARK_GRAY));
                }
                tooltip.accept(line);
            });
        }
        if (stacks.size() > MAX_TOOLTIP_ENTRIES) {
            tooltip.accept(Component.translatable("enchanttransfer.deck.more", stacks.size() - MAX_TOOLTIP_ENTRIES)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
