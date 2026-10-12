package net.alfonsormadrid.enchanttransfer.item;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Cards stored in a Card Deck — the value of the {@code deck_contents}
 * data component.
 * <p>
 * Immutable, like any component value: every operation returns a new copy.
 * Stores {@link ItemStackTemplate}s, the same way 26.2's bundle contents do.
 * Capacity rules live in {@link net.alfonsormadrid.enchanttransfer.library.DeckCardStorage};
 * this record only knows how to add, remove and value cards.
 */
public record DeckContents(List<ItemStackTemplate> cards) {

    public static final DeckContents EMPTY = new DeckContents(List.of());

    public static final Codec<DeckContents> CODEC =
            ItemStackTemplate.CODEC.listOf().xmap(DeckContents::new, DeckContents::cards);

    public static final StreamCodec<RegistryFriendlyByteBuf, DeckContents> STREAM_CODEC =
            ItemStackTemplate.STREAM_CODEC.apply(ByteBufCodecs.list()).map(DeckContents::new, DeckContents::cards);

    public DeckContents {
        cards = List.copyOf(cards);
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /** Total value in card points (see {@link MagicCardItem#pointsOf}). */
    public int points() {
        return stacks().stream().mapToInt(s -> MagicCardItem.pointsOf(s) * s.getCount()).sum();
    }

    /** Copies of the stored stacks. */
    public List<ItemStack> stacks() {
        return cards.stream().map(ItemStackTemplate::create).toList();
    }

    /** Adds {@code card}, merging into equal stacks up to their max stack size. */
    public DeckContents with(ItemStack card) {
        if (card.isEmpty()) return this;
        List<ItemStackTemplate> out = new ArrayList<>(cards);
        int remaining = card.getCount();
        for (int i = 0; i < out.size() && remaining > 0; i++) {
            ItemStackTemplate t = out.get(i);
            ItemStack existing = t.create();
            if (!ItemStack.isSameItemSameComponents(existing, card)) continue;
            int moved = Math.min(existing.getMaxStackSize() - t.count(), remaining);
            if (moved > 0) {
                out.set(i, t.withCount(t.count() + moved));
                remaining -= moved;
            }
        }
        if (remaining > 0) {
            out.add(ItemStackTemplate.fromNonEmptyStack(card.copyWithCount(remaining)));
        }
        return new DeckContents(out);
    }

    /** The most recently stored stack, if any. */
    public Optional<ItemStack> last() {
        return cards.isEmpty() ? Optional.empty() : Optional.of(cards.getLast().create());
    }

    public DeckContents withoutLast() {
        return cards.isEmpty() ? this : new DeckContents(cards.subList(0, cards.size() - 1));
    }

    /** Result of {@link #wear}: the new contents and the wear left to apply elsewhere. */
    public record Worn(DeckContents contents, int leftover) {
    }

    /** Reserve left across the stored cards, in {@link CardWear} units. */
    public int reserve() {
        return stacks().stream().mapToInt(s -> CardWear.remainingOf(s) * s.getCount()).sum();
    }

    /**
     * Wears {@code units} off the stored cards, one card at a time: first a
     * card already worn, else the lowest-level one.  A card whose reserve
     * runs out is removed and the rest moves on to the next card.
     */
    public Worn wear(int units) {
        List<ItemStackTemplate> out = new ArrayList<>(cards);
        while (units > 0) {
            int i = nextToWear(out);
            if (i < 0) break;
            ItemStackTemplate t = out.get(i);
            ItemStack card = t.create().copyWithCount(1);
            int left = CardWear.remainingOf(card);
            if (t.count() > 1) out.set(i, t.withCount(t.count() - 1));
            else out.remove(i);
            if (units >= left) {
                units -= left;
                continue;
            }
            CardWear.setWorn(card, CardWear.wornOf(card) + units);
            units = 0;
            // Back in the same place, so the deck keeps its insertion order.
            out.add(i, ItemStackTemplate.fromNonEmptyStack(card));
        }
        return new Worn(new DeckContents(out), units);
    }

    private static int nextToWear(List<ItemStackTemplate> cards) {
        int best = -1;
        boolean bestWorn = false;
        int bestPoints = Integer.MAX_VALUE;
        for (int i = 0; i < cards.size(); i++) {
            ItemStack card = cards.get(i).create();
            if (CardWear.remainingOf(card) <= 0) continue;
            boolean worn = CardWear.wornOf(card) > 0;
            int points = MagicCardItem.pointsOf(card);
            if (best < 0 || (worn && !bestWorn) || (worn == bestWorn && points < bestPoints)) {
                best = i;
                bestWorn = worn;
                bestPoints = points;
            }
        }
        return best;
    }

    /** Stacks ordered from most to least valuable, for the tooltip. */
    public List<ItemStack> sortedForDisplay() {
        return stacks().stream()
                .sorted(Comparator.comparingInt((ItemStack s) -> -MagicCardItem.pointsOf(s) * s.getCount()))
                .toList();
    }
}
