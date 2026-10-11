package net.alfonsormadrid.enchanttransfer.library;

import java.util.List;
import java.util.Map;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Loads {@link AffinityRule}s from datapacks and keeps the current set.
 * Reloaded with the rest of the data on world load and {@code /reload}.
 */
public class LibraryAffinities extends SimpleJsonResourceReloadListener<AffinityRule> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "library_affinity");

    /** Not a real registry: only gives the directory, {@code enchanttransfer/library_affinity}. */
    private static final ResourceKey<Registry<AffinityRule>> DIRECTORY = ResourceKey.createRegistryKey(ID);

    private static volatile List<AffinityRule> rules = List.of();

    public LibraryAffinities(HolderLookup.Provider registries) {
        super(registries, AffinityRule.CODEC, DIRECTORY);
    }

    @Override
    protected void apply(Map<Identifier, AffinityRule> loaded, ResourceManager manager, ProfilerFiller profiler) {
        rules = List.copyOf(loaded.values());
    }

    /** Rules that apply to a table in this dimension and biome. */
    public static List<AffinityRule> rulesAt(ResourceKey<Level> dimension, Holder<Biome> biome) {
        return rules.stream().filter(rule -> rule.matchesLocation(dimension, biome)).toList();
    }
}
