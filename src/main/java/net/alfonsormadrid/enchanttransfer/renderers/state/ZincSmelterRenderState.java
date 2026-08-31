package net.alfonsormadrid.enchanttransfer.renderers.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * Render state for the Zinc Smelter BER.
 * Carries the facing direction and lit state from the game thread
 * to the render thread.
 */
public class ZincSmelterRenderState extends BlockEntityRenderState {
    /** Horizontal facing of the smelter (where the door is). */
    public Direction facing = Direction.NORTH;
    /** True when the smelter is burning fuel. */
    public boolean lit = false;
    /** Continuous animation time for fire flicker. */
    public float animTime = 0f;
}
