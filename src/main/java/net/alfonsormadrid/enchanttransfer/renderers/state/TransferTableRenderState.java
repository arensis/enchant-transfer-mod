package net.alfonsormadrid.enchanttransfer.renderers.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Render state for the Transfer Table block entity renderer.
 * Carries a pre-computed animation time value for the pulsing emissive core
 * plus the set of faces that have an Infusion Coil neighbour, so the BER
 * can paint a connecting tube only for the connected sides.
 */
public class TransferTableRenderState extends BlockEntityRenderState {

    /** Continuous time value (worldTime + tickDelta) for sinusoidal pulse animation. */
    public float animTime = 0f;

    /**
     * For each {@link net.minecraft.core.Direction#ordinal()}, true when
     * the neighbour block on that face is an Infusion Coil — populated on
     * the game thread by {@code updateRenderState}.  The BER renders a
     * core→face tube for every {@code true} entry.
     */
    public final boolean[] connectedFaces = new boolean[6];
}
