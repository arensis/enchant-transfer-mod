package net.alfonsormadrid.enchanttransfer.modules;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.energy.ExperienceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * The modules around a Transfer Table core, read from the world when needed.
 * <p>
 * Nothing is registered or kept in sync: a module is part of the network
 * while its block entity sits on one of the core's 6 faces.  So modules need
 * no attach/detach code, and nothing can go stale.  Lookups never load a
 * chunk ({@code isLoaded} first): an unloaded neighbour simply isn't part of
 * the network, and getBlockEntity on it would load it — from
 * {@code setRemoved} on server shutdown that hung the server (3.2.1).
 * <p>
 * Shared resources are found by capability: {@link #storages} returns every
 * module implementing a storage interface (today {@link ExperienceStorage};
 * tomorrow, say, essence), and the XP helpers spread a transfer across every
 * Infusion Coil around the core as if they were one tank.
 * <p>
 * Building one checks 6 blocks; build it where it's needed (e.g. once per
 * tick of a consuming module) rather than caching it.
 */
public final class ModuleNetwork {

    private static final ModuleNetwork EMPTY = new ModuleNetwork(new EnumMap<>(Direction.class));

    private final Map<Direction, TransferTableModule> modulesByFace;

    private ModuleNetwork(Map<Direction, TransferTableModule> modulesByFace) {
        this.modulesByFace = modulesByFace;
    }

    /** The modules on the 6 faces of the core at {@code corePos}. */
    public static ModuleNetwork around(@Nullable Level level, BlockPos corePos) {
        if (level == null) return EMPTY;
        Map<Direction, TransferTableModule> modules = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            BlockPos pos = corePos.relative(face);
            if (!level.isLoaded(pos)) continue;
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TransferTableModule module && !be.isRemoved()) {
                modules.put(face, module);
            }
        }
        return new ModuleNetwork(modules);
    }

    /** The core a module at {@code modulePos} sits next to, if any. */
    public static Optional<BlockPos> findCore(@Nullable Level level, BlockPos modulePos) {
        if (level == null) return Optional.empty();
        for (Direction face : Direction.values()) {
            BlockPos pos = modulePos.relative(face);
            if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof TransferTableBlockEntity) {
                return Optional.of(pos);
            }
        }
        return Optional.empty();
    }

    // ── Modules ─────────────────────────────────────────────────────────────

    @Nullable
    public TransferTableModule get(Direction coreFace) {
        return modulesByFace.get(coreFace);
    }

    public boolean hasModuleOn(Direction coreFace) {
        return modulesByFace.containsKey(coreFace);
    }

    public Map<Direction, TransferTableModule> getAll() {
        return Collections.unmodifiableMap(modulesByFace);
    }

    public Collection<TransferTableModule> getModules() {
        return Collections.unmodifiableCollection(modulesByFace.values());
    }

    public int connectedCount() {
        return modulesByFace.size();
    }

    public boolean isEmpty() {
        return modulesByFace.isEmpty();
    }

    /** Every module implementing {@code type}, in face order (e.g. {@code ExperienceStorage.class}). */
    public <T> List<T> storages(Class<T> type) {
        return modulesByFace.values().stream().filter(type::isInstance).map(type::cast).toList();
    }

    // ── Aggregated XP view ──────────────────────────────────────────────────

    public int getTotalStoredXp() {
        return tanks().mapToInt(ExperienceStorage::getStored).sum();
    }

    public int getTotalCapacityXp() {
        return tanks().mapToInt(ExperienceStorage::getCapacity).sum();
    }

    /** Inserts XP across every tank, filling them in face order.  Returns the amount inserted. */
    public int insertXp(int points, boolean simulate) {
        int remaining = points;
        for (ExperienceStorage tank : storages(ExperienceStorage.class)) {
            if (remaining <= 0) break;
            remaining -= tank.insert(remaining, simulate);
        }
        return points - remaining;
    }

    /** Extracts XP across every tank, draining them in face order.  Returns the amount extracted. */
    public int extractXp(int points, boolean simulate) {
        int remaining = points;
        for (ExperienceStorage tank : storages(ExperienceStorage.class)) {
            if (remaining <= 0) break;
            remaining -= tank.extract(remaining, simulate);
        }
        return points - remaining;
    }

    private Stream<ExperienceStorage> tanks() {
        return storages(ExperienceStorage.class).stream();
    }
}
