package net.alfonsormadrid.enchanttransfer.modules;

/**
 * Contract any block entity must satisfy to act as a module attached to a
 * Transfer Table core.
 * <p>
 * Modules are the leaves of the network: each one is its own block with its
 * own block entity. The core only knows about them through this interface,
 * which keeps coupling low and enables third-party modules in the future.
 * <p>
 * A module is attached while its block entity sits on one of the core's
 * faces; nothing is registered, so modules need no attach/detach code (see
 * {@link ModuleNetwork}, which also finds the core from a module).  To share
 * a resource through the core, implement a storage interface such as
 * {@code ExperienceStorage}: {@link ModuleNetwork#storages} finds it.
 */
public interface TransferTableModule {

    ModuleType getModuleType();

    /**
     * Snapshot of the module's state for display in the hub and nav-row.
     * Called only from the server thread; sent to clients via the network
     * sync packet.
     */
    ModulePreview buildPreview();
}
