package net.alfonsormadrid.enchanttransfer.modules;

import net.alfonsormadrid.enchanttransfer.energy.ExperienceStorage;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Per-side map of modules currently attached to a Transfer Table core.
 * <p>
 * Lives inside the core's block entity. Modules register themselves when their
 * block entity loads (event-driven) and unregister on removal, so the registry
 * does not need to be persisted: it rebuilds from world state automatically.
 * <p>
 * Also exposes aggregated views over modules that implement
 * {@link ExperienceStorage}, so other modules can consume XP from the whole
 * network through a single call to the core.
 */
public class ModuleConnectionRegistry {

    private final EnumMap<Direction, TransferTableModule> modulesByFace = new EnumMap<>(Direction.class);

    public void attach(Direction coreFace, TransferTableModule module) {
        modulesByFace.put(coreFace, module);
    }

    public void detach(Direction coreFace) {
        modulesByFace.remove(coreFace);
    }

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

    // ── Aggregated XP view ──────────────────────────────────────────────────

    public int getTotalStoredXp() {
        return tanks().mapToInt(ExperienceStorage::getStored).sum();
    }

    public int getTotalCapacityXp() {
        return tanks().mapToInt(ExperienceStorage::getCapacity).sum();
    }

    /**
     * Inserts XP across all tanks in the network, filling them in the order
     * they were registered. Returns the amount actually inserted.
     */
    public int insertXp(int points, boolean simulate) {
        int remaining = points;
        for (TransferTableModule module : modulesByFace.values()) {
            if (remaining <= 0) break;
            if (module instanceof ExperienceStorage tank) {
                remaining -= tank.insert(remaining, simulate);
            }
        }
        return points - remaining;
    }

    /**
     * Extracts XP across all tanks in the network, draining them in the order
     * they were registered. Returns the amount actually extracted.
     */
    public int extractXp(int points, boolean simulate) {
        int remaining = points;
        for (TransferTableModule module : modulesByFace.values()) {
            if (remaining <= 0) break;
            if (module instanceof ExperienceStorage tank) {
                remaining -= tank.extract(remaining, simulate);
            }
        }
        return points - remaining;
    }

    private java.util.stream.Stream<ExperienceStorage> tanks() {
        return modulesByFace.values().stream()
                .filter(ExperienceStorage.class::isInstance)
                .map(ExperienceStorage.class::cast);
    }
}
