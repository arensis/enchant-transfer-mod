package net.alfonsormadrid.enchanttransfer.item;

import net.alfonsormadrid.enchanttransfer.library.CategoryDeckEffect;
import net.alfonsormadrid.enchanttransfer.library.DeckEffect;
import net.alfonsormadrid.enchanttransfer.library.RarityDeckEffect;
import net.minecraft.world.item.ItemStack;

/**
 * Kind of a {@link CardDeckItem}: one per card category, plus the Rarity
 * wildcard.  Each kind is composed with the
 * {@link DeckEffect} that defines which cards it stores and what it does to
 * the table.
 * <p>
 * {@link CardType#BLACK} has no category deck: curses never come out of the
 * table, so Curse cards feed the Rarity wildcard instead.
 */
public enum DeckKind {

    BLUE("blue", new CategoryDeckEffect(CardType.BLUE), 0x3a64c8),
    GREEN("green", new CategoryDeckEffect(CardType.GREEN), 0x3f9e4d),
    RED("red", new CategoryDeckEffect(CardType.RED), 0xc8423a),
    YELLOW("yellow", new CategoryDeckEffect(CardType.YELLOW), 0xe0b532),
    PURPLE("purple", new CategoryDeckEffect(CardType.PURPLE), 0x8a4fc4),
    RARE("rare", new RarityDeckEffect(), 0x767676);

    private final String id;
    private final DeckEffect effect;
    private final int barColor;

    DeckKind(String id, DeckEffect effect, int barColor) {
        this.id = id;
        this.effect = effect;
        this.barColor = barColor;
    }

    /** Suffix of the registry id ({@code card_deck_<id>}), texture and lang keys. */
    public String id() {
        return id;
    }

    public DeckEffect effect() {
        return effect;
    }

    public int barColor() {
        return barColor;
    }

    /** Whether a deck of this kind can store {@code card}. */
    public boolean accepts(ItemStack card) {
        return MagicCardItem.enchantmentOf(card).map(effect::accepts).orElse(false);
    }
}
