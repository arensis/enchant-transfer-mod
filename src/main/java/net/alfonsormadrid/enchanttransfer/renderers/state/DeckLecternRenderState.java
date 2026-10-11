package net.alfonsormadrid.enchanttransfer.renderers.state;

import net.alfonsormadrid.enchanttransfer.blocks.decklectern.DeckLecternBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

/**
 * Render state for the Deck Lectern BER: the facing and one item render
 * state per slot, extracted on the game thread.
 */
public class DeckLecternRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public final ItemStackRenderState[] decks = new ItemStackRenderState[DeckLecternBlockEntity.SLOT_COUNT];

    public DeckLecternRenderState() {
        for (int i = 0; i < decks.length; i++) {
            decks[i] = new ItemStackRenderState();
        }
    }
}
