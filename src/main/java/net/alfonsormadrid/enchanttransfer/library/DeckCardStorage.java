package net.alfonsormadrid.enchanttransfer.library;

import java.util.function.Predicate;
import net.alfonsormadrid.enchanttransfer.item.DeckContents;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.item.ModDataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * {@link CardStorage} view over a Card Deck stack.
 * <p>
 * Plays the role {@link net.alfonsormadrid.enchanttransfer.energy.SimpleExperienceTank}
 * plays for XP, except the state lives in the stack's immutable
 * {@code deck_contents} component: every write replaces the component on
 * the wrapped stack.  Cheap to create — build one per operation.
 */
public class DeckCardStorage implements CardStorage {

    public static final int CAPACITY_POINTS = 64;

    private final ItemStack deck;
    private final DeckKind kind;

    public DeckCardStorage(ItemStack deck, DeckKind kind) {
        this.deck = deck;
        this.kind = kind;
    }

    public DeckKind getKind() {
        return kind;
    }

    public DeckContents getContents() {
        return deck.getOrDefault(ModDataComponents.DECK_CONTENTS, DeckContents.EMPTY);
    }

    private void setContents(DeckContents contents) {
        deck.set(ModDataComponents.DECK_CONTENTS, contents);
    }

    @Override
    public boolean accepts(ItemStack card) {
        return kind.accepts(card);
    }

    @Override
    public int insert(ItemStack card, boolean simulate) {
        if (card.isEmpty() || !accepts(card)) return 0;
        int perCard = MagicCardItem.pointsOf(card);
        if (perCard <= 0) return 0;
        int accepted = Math.min(card.getCount(), getAvailablePoints() / perCard);
        if (accepted > 0 && !simulate) {
            setContents(getContents().with(card.copyWithCount(accepted)));
        }
        return accepted;
    }

    @Override
    public ItemStack extractLast(boolean simulate) {
        DeckContents contents = getContents();
        ItemStack last = contents.last().orElse(ItemStack.EMPTY);
        if (!last.isEmpty() && !simulate) {
            setContents(contents.withoutLast());
        }
        return last;
    }

    @Override
    public int wear(int units) {
        if (units <= 0) return 0;
        DeckContents.Worn worn = getContents().wear(units);
        setContents(worn.contents());
        return worn.leftover();
    }

    @Override
    public int getReserve() {
        return getContents().reserve();
    }

    @Override
    public int getStoredPoints() {
        return getContents().points();
    }

    @Override
    public int getCapacityPoints() {
        return CAPACITY_POINTS;
    }
}
