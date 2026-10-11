package net.alfonsormadrid.enchanttransfer.library;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * Data-driven affinity: a deck works better in a given place.
 * <p>
 * Loaded from datapacks ({@code data/<namespace>/enchanttransfer/library_affinity/*.json}),
 * so modpacks and other mods can add rules for their own dimensions and biomes:
 * <pre>
 * {
 *   "deck": "enchanttransfer:card_deck_green",       // required
 *   "dimension": "minecraft:overworld",              // optional
 *   "biome": "#minecraft:is_ocean",                  // optional: id, list or #tag
 *   "enchantments": ["minecraft:respiration"],       // optional: only these cards of the deck
 *   "multiplier": 1.5                                // required
 * }
 * </pre>
 * The multiplier scales the deck's <em>extra</em> effect, so it never
 * affects a table without that kind of deck around.
 */
public record AffinityRule(
        Identifier deck,
        Optional<ResourceKey<Level>> dimension,
        Optional<HolderSet<Biome>> biome,
        Optional<HolderSet<Enchantment>> enchantments,
        float multiplier
) {

    public static final Codec<AffinityRule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("deck").forGetter(AffinityRule::deck),
            Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension").forGetter(AffinityRule::dimension),
            RegistryCodecs.homogeneousList(Registries.BIOME).optionalFieldOf("biome").forGetter(AffinityRule::biome),
            RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("enchantments").forGetter(AffinityRule::enchantments),
            Codec.floatRange(0.0F, 10.0F).fieldOf("multiplier").forGetter(AffinityRule::multiplier)
    ).apply(instance, AffinityRule::new));

    /** Whether this rule applies to a table in this dimension and biome. */
    public boolean matchesLocation(ResourceKey<Level> tableDimension, Holder<Biome> tableBiome) {
        return dimension.map(tableDimension::equals).orElse(true)
                && biome.map(set -> set.contains(tableBiome)).orElse(true);
    }

    /** Whether this rule boosts {@code enchantment} cards in {@code kind} decks. */
    public boolean matches(DeckKind kind, Holder<Enchantment> enchantment) {
        return EnchantTransferMod.cardDeckId(kind).equals(deck)
                && enchantments.map(set -> set.contains(enchantment)).orElse(true);
    }
}
