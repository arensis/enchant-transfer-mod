package net.alfonsormadrid.enchanttransfer.enchantment;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads {@link SaplingRule}s from datapacks and answers which sapling a log replants.
 * Reloaded with the rest of the data on world load and {@code /reload}.
 */
public class SaplingRules extends SimpleJsonResourceReloadListener<SaplingRule> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "harvesting_sapling");

    /** Not a real registry: only gives the directory, {@code enchanttransfer/harvesting_sapling}. */
    private static final ResourceKey<Registry<SaplingRule>> DIRECTORY = ResourceKey.createRegistryKey(ID);

    /** Suffixes of a tree's trunk and of what grows it, for logs no rule covers. */
    private static final List<String> LOG_SUFFIXES = List.of("_log", "_stem");
    private static final List<String> SAPLING_SUFFIXES = List.of("_sapling", "_propagule", "_fungus");

    private static volatile List<SaplingRule> rules = List.of();

    public SaplingRules(HolderLookup.Provider registries) {
        super(registries, SaplingRule.CODEC, DIRECTORY);
    }

    @Override
    protected void apply(Map<Identifier, SaplingRule> loaded, ResourceManager manager, ProfilerFiller profiler) {
        rules = List.copyOf(loaded.values());
    }

    /**
     * The sapling for this log: a datapack rule, else by name in the log's namespace
     * ({@code mymod:maple_log} → {@code mymod:maple_sapling}, {@code _propagule} or
     * {@code _fungus}), else none.
     */
    static @Nullable Block saplingFor(BlockState log) {
        for (SaplingRule rule : rules) {
            if (rule.logs().contains(log.typeHolder())) {
                return rule.sapling();
            }
        }
        return byName(log.getBlock()).orElse(null);
    }

    private static Optional<Block> byName(Block log) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(log);
        for (String logSuffix : LOG_SUFFIXES) {
            if (!id.getPath().endsWith(logSuffix)) {
                continue;
            }
            String wood = id.getPath().substring(0, id.getPath().length() - logSuffix.length());
            for (String saplingSuffix : SAPLING_SUFFIXES) {
                Optional<Block> sapling = BuiltInRegistries.BLOCK
                        .getOptional(Identifier.fromNamespaceAndPath(id.getNamespace(), wood + saplingSuffix))
                        .filter(block -> block != Blocks.AIR);
                if (sapling.isPresent()) {
                    return sapling;
                }
            }
        }
        return Optional.empty();
    }
}
