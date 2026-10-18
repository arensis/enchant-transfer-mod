package net.alfonsormadrid.enchanttransfer.enchantment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;

/**
 * Data-driven: which sapling Harvesting replants for a tree's logs. Loaded from datapacks
 * ({@code data/<namespace>/enchanttransfer/harvesting_sapling/*.json}), so modpacks and
 * other mods can add their own trees:
 * <pre>
 * {
 *   "logs": "minecraft:oak_log",      // id, list or #tag
 *   "sapling": "minecraft:oak_sapling"
 * }
 * </pre>
 * Logs without a rule fall back to the naming convention, see {@link SaplingRules}.
 */
public record SaplingRule(HolderSet<Block> logs, Block sapling) {

    public static final Codec<SaplingRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("logs").forGetter(SaplingRule::logs),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("sapling").forGetter(SaplingRule::sapling)
    ).apply(instance, SaplingRule::new));
}
