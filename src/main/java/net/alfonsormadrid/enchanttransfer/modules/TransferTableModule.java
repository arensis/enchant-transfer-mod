package net.alfonsormadrid.enchanttransfer.modules;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Contract any block entity must satisfy to act as a module attached to a
 * Transfer Table core.
 * <p>
 * Modules are the leaves of the network: each one is its own block with its
 * own block entity. The core only knows about them through this interface,
 * which keeps coupling low and enables third-party modules in the future.
 */
public interface TransferTableModule {

    ModuleType getModuleType();

    /**
     * Position of the core this module is currently attached to, or
     * {@code null} if orphaned (no adjacent core).
     */
    @Nullable
    BlockPos getCorePos();

    /**
     * Face of the core through which this module is attached, or {@code null}
     * if orphaned.
     */
    @Nullable
    Direction getCoreFace();

    /**
     * Called once when the module successfully attaches to a core.
     * Implementations typically cache the reference and update their block
     * state for visual feedback.
     */
    void onAttachedToCore(BlockPos corePos, Direction coreFace);

    /**
     * Called when the link to the core is broken (core destroyed, module
     * picked up, chunk unload, etc.). The module remains functional as a
     * standalone block.
     */
    void onDetachedFromCore();

    /**
     * Snapshot of the module's state for display in the hub and nav-row.
     * Called only from the server thread; sent to clients via the network
     * sync packet.
     */
    ModulePreview buildPreview();
}
