package net.alfonsormadrid.enchanttransfer.library;

/**
 * Implemented by the vanilla {@code EnchantmentMenu} (via mixin): which of its
 * three options the Card Decks changed compared to a vanilla roll, and which
 * of those the decks lack the wear reserve to pay for (they can't be picked).
 * Synced to the client through a data slot so the Enchanting Table screen
 * can mark them.
 */
public interface DeckInfluenceView {

    boolean enchanttransfer$isDeckInfluenced(int option);

    boolean enchanttransfer$lacksReserve(int option);
}
