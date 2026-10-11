package net.alfonsormadrid.enchanttransfer.blocks.decklectern;

import net.alfonsormadrid.enchanttransfer.item.CardDeckItem;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

/**
 * State of one slot's indicator lamp on the Deck Lectern:
 * dark brown with no deck, light with an empty deck, emerald with cards.
 */
public enum LampState implements StringRepresentable {
    NONE("none"),
    EMPTY("empty"),
    FULL("full");

    private final String name;

    LampState(String name) {
        this.name = name;
    }

    public static LampState of(ItemStack stack) {
        if (!(stack.getItem() instanceof CardDeckItem deck)) return NONE;
        return deck.getStorage(stack).isEmpty() ? EMPTY : FULL;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
