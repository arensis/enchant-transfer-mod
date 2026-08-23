package net.alfonsormadrid.enchanttransfer.renderers.state;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Render state for the Infusion Coil block entity renderer.
 * Carries the data needed to draw the fluid fill, cap glow and connection
 * tube without touching the block entity on the render thread.
 */
public class InfusionCoilRenderState extends BlockEntityRenderState {

    /** 0..1 fraction of the XP tank that is filled. */
    public float fillRatio = 0f;

    /** True when the coil is actively infusing a magic card. */
    public boolean isProcessing = false;

    /** Continuous time value (worldTime + tickDelta) for pulse animation. */
    public float animTime = 0f;

    public int blockLight;

    /**
     * Fractional tick position of the XP drip drop (0..1 along the glass face,
     * 0 = top of fluid, 1 = bottom rim). Advances each game tick while
     * isProcessing is true. Populated as a deterministic function of worldTime
     * so it doesn't need to be stored in the BE.
     */
    public float dripProgress = 0f;

    /** Whether a drip drop should be drawn this frame. */
    public boolean showDrip = false;

    /** 0..1 bump progress after drip impact (0 = peak, 1 = settled). */
    public float fluidBump = 0f;

    /** 0..1 ripple ring expansion after drip impact. */
    public float rippleProgress = 0f;

    /**
     * Direction <em>from this coil toward the adjacent Transfer Table</em>,
     * or {@code null} when not connected. Populated by scanning adjacent
     * block states on the game thread (avoids relying on the server-only
     * {@code cachedCoreFace} field, which is never synced to the client).
     */
    @Nullable
    public Direction dirToTable = null;
}
