package net.alfonsormadrid.enchanttransfer.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.alfonsormadrid.enchanttransfer.item.CardType;
import net.alfonsormadrid.enchanttransfer.item.CardWear;
import net.alfonsormadrid.enchanttransfer.item.DeckContents;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Snapshot of every Card Deck in the libraries around one enchanting table.
 * <p>
 * Plays the role {@link net.alfonsormadrid.enchanttransfer.modules.ModuleNetwork}
 * plays for the Transfer Table: it collects the raw numbers and exposes
 * aggregated views over the {@link DeckEffect}s present, so callers ask the
 * profile instead of iterating decks themselves.
 */
public final class LibraryProfile {

    public static final LibraryProfile EMPTY = new LibraryProfile();

    /** Where a deck is, so it can be worn later. */
    public record DeckLocation(BlockPos libraryPos, int slot, DeckKind kind) {
    }

    private final Map<DeckKind, Integer> pointsByKind = new EnumMap<>(DeckKind.class);
    private final Map<CardType, Integer> pointsByCategory = new EnumMap<>(CardType.class);
    /** Points of max-level cards only: they're the ones that favor their exact enchantment. */
    private final Map<ResourceKey<Enchantment>, Integer> maxLevelPointsByEnchantment = new HashMap<>();
    private final Map<DeckKind, Integer> reserveByKind = new EnumMap<>(DeckKind.class);
    private final List<DeckLocation> locations = new ArrayList<>();
    private int categoryTotal;
    private List<AffinityRule> affinityRules = List.of();

    public static final double MAX_AFFINITY = 3.0;

    /** Adds one deck to the snapshot.  Empty decks are ignored. */
    public void add(DeckKind kind, DeckContents contents, BlockPos libraryPos, int slot) {
        if (contents.isEmpty()) return;
        int points = contents.points();
        locations.add(new DeckLocation(libraryPos, slot, kind));
        pointsByKind.merge(kind, points, Integer::sum);
        reserveByKind.merge(kind, contents.reserve(), Integer::sum);

        if (kind.effect() instanceof CategoryDeckEffect category) {
            categoryTotal += points;
            pointsByCategory.merge(category.getCategory(), points, Integer::sum);
            for (ItemStack card : contents.stacks()) {
                if (!CardWear.isMaxLevel(card)) continue;
                MagicCardItem.enchantmentOf(card).flatMap(Holder::unwrapKey).ifPresent(key ->
                        maxLevelPointsByEnchantment.merge(key, MagicCardItem.pointsOf(card) * card.getCount(), Integer::sum));
            }
        }
    }

    // ── Raw numbers ─────────────────────────────────────────────────────────

    public boolean isEmpty() {
        return locations.isEmpty();
    }

    /** Decks holding at least one card. */
    public int getDeckCount() {
        return locations.size();
    }

    public List<DeckLocation> getLocations() {
        return Collections.unmodifiableList(locations);
    }

    public Set<DeckKind> getPresentKinds() {
        return pointsByKind.isEmpty() ? EnumSet.noneOf(DeckKind.class) : EnumSet.copyOf(pointsByKind.keySet());
    }

    public int getPoints(DeckKind kind) {
        return pointsByKind.getOrDefault(kind, 0);
    }

    /** Points across all category decks (wildcards excluded). */
    public int getCategoryTotal() {
        return categoryTotal;
    }

    public int getCategoryPoints(CardType category) {
        return pointsByCategory.getOrDefault(category, 0);
    }

    /** Points of max-level {@code enchantment} cards across category decks. */
    public int getMaxLevelEnchantmentPoints(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(key -> maxLevelPointsByEnchantment.getOrDefault(key, 0)).orElse(0);
    }

    /** Wear reserve left across the decks of {@code kind}, in {@code CardWear} units. */
    public int getReserve(DeckKind kind) {
        return reserveByKind.getOrDefault(kind, 0);
    }

    /**
     * Extra ({@code factor − 1}, with affinity) {@code kind} decks give
     * {@code candidate}: its part of {@link #weightMultiplier}.
     */
    public double extra(DeckKind kind, EnchantmentInstance candidate) {
        if (!pointsByKind.containsKey(kind)) return 0.0;
        double factor = kind.effect().weightMultiplier(candidate, this);
        return (factor - 1.0) * affinity(kind, candidate.enchantment());
    }

    // ── Aggregated effect views ─────────────────────────────────────────────

    /**
     * Product of every present effect's weight factor for {@code candidate}.
     * Each effect's extra ({@code factor − 1}) is scaled by the deck's
     * {@link #affinity} at the table's location.
     */
    public double weightMultiplier(EnchantmentInstance candidate) {
        double multiplier = 1.0;
        for (DeckKind kind : getPresentKinds()) {
            multiplier *= 1.0 + extra(kind, candidate);
        }
        return multiplier;
    }

    // ── Location affinity ───────────────────────────────────────────────────

    /** Rules that matched the table's dimension and biome (set by the scan). */
    public void setAffinityRules(List<AffinityRule> rules) {
        this.affinityRules = List.copyOf(rules);
    }

    /**
     * How much {@code kind} decks resonate with this place for
     * {@code enchantment} cards: the product of every matching rule (so a
     * specific rule stacks on a general one), capped at {@value #MAX_AFFINITY}.
     */
    public double affinity(DeckKind kind, Holder<Enchantment> enchantment) {
        double affinity = 1.0;
        for (AffinityRule rule : affinityRules) {
            if (rule.matches(kind, enchantment)) affinity *= rule.multiplier();
        }
        return Math.min(MAX_AFFINITY, affinity);
    }
}
