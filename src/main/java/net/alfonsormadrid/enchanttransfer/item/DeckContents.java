package net.alfonsormadrid.enchanttransfer.item;

import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
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

    /** Removes a single card: the cheapest one matching {@code filter}. */
    public Optional<DeckContents> withoutOneCard(Predicate<ItemStack> filter) {
        int best = -1;
        int bestPoints = Integer.MAX_VALUE;
        for (int i = 0; i < cards.size(); i++) {
            ItemStack card = cards.get(i).create();
            int points = MagicCardItem.pointsOf(card);
            if (filter.test(card) && points < bestPoints) {
                best = i;
                bestPoints = points;
            }
        }
        if (best < 0) return Optional.empty();
        List<ItemStackTemplate> out = new ArrayList<>(cards);
        ItemStackTemplate t = out.get(best);
        if (t.count() > 1) out.set(best, t.withCount(t.count() - 1));
        else out.remove(best);
        return Optional.of(new DeckContents(out));
    }

    /** Stacks ordered from most to least valuable, for the tooltip. */
    public List<ItemStack> sortedForDisplay() {
        return stacks().stream()
                .sorted(Comparator.comparingInt((ItemStack s) -> -MagicCardItem.pointsOf(s) * s.getCount()))
                .toList();
    }
}
