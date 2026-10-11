package net.alfonsormadrid.enchanttransfer.library;

import net.alfonsormadrid.enchanttransfer.item.DeckKind;

/**
 * Base for wildcard decks: they don't favor a category, they change how the
 * table rolls.  Their strength is the wildcard's fill around the table:
 * {@code min(1, points of this wildcard / 64)}.
 */
public abstract class WildcardDeckEffect implements DeckEffect {

    public static final int FULL_WILDCARD_POINTS = 64;

    /** The kind whose points measure this wildcard's strength. */
    protected abstract DeckKind kind();

    protected double strength(LibraryProfile profile) {
        return Math.min(1.0, profile.getPoints(kind()) / (double) FULL_WILDCARD_POINTS);
    }
}
