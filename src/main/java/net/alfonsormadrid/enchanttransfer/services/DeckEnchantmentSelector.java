package net.alfonsormadrid.enchanttransfer.services;

import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import net.alfonsormadrid.enchanttransfer.library.LibraryProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/**
 * Copy of {@link EnchantmentHelper#selectEnchantment} where each candidate's
 * weight is multiplied by {@link LibraryProfile#weightMultiplier} (the deck
 * effects present around the table).
 * <p>
 * Levels are untouched here: they come from the table's power, to which
 * decks contribute like bookshelves ({@link LibraryResonanceService#deckPower}).
 * <p>
 * Each pick is <em>coupled</em> to vanilla: the table's seeded random draws
 * exactly as vanilla does, and that vanilla pick is kept whenever the deck
 * weights allow it (maximal coupling, see {@link #pick}).  The result follows
 * the deck weights, yet decks only change a pick they actually caused, always
 * toward an enchantment they favor.  Those picks are the
 * {@linkplain Selection#influenced influenced} ones; later picks that merely
 * differ because an earlier one changed (other candidates left) were still
 * drawn with vanilla weights, so they don't count.
 * The coupling draws come from a separate random seeded per option, so the
 * clue shown in the GUI still matches the final result.
 */
public class DeckEnchantmentSelector {

    /**
     * @param enchantments the picked enchantments, in vanilla's order
     * @param influenced   enchantments whose pick the deck bonus changed
     */
    public record Selection(List<EnchantmentInstance> enchantments, Set<Holder<Enchantment>> influenced) {}

    /**
     * @param random   the table's random, seeded by vanilla for this option
     * @param coupling a random seeded deterministically for this option, used
     *                 only to decide whether the decks change a vanilla pick
     */
    public Selection select(RandomSource random, RandomSource coupling, ItemStack stack, int cost,
                           Stream<Holder<Enchantment>> pool, LibraryProfile profile) {
        List<EnchantmentInstance> results = Lists.newArrayList();
        Set<Holder<Enchantment>> influenced = new HashSet<>();
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) return new Selection(results, influenced);

        // ── Vanilla: cost jitter ─────────────────────────────────────────
        cost += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
        float jitter = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        cost = Mth.clamp(Math.round(cost + cost * jitter), 1, Integer.MAX_VALUE);

        // ── Vanilla selection loop, with deck-coupled picks ──────────────
        List<EnchantmentInstance> available = EnchantmentHelper.getAvailableEnchantmentResults(cost, stack, pool);
        if (!available.isEmpty()) {
            pick(random, coupling, available, profile, influenced).ifPresent(results::add);
            while (random.nextInt(50) <= cost) {
                if (!results.isEmpty()) {
                    EnchantmentHelper.filterCompatibleEnchantments(available, results.getLast());
                }
                if (available.isEmpty()) break;
                pick(random, coupling, available, profile, influenced).ifPresent(results::add);
                cost /= 2;
            }
        }
        return new Selection(results, influenced);
    }

    /**
     * Draws vanilla's pick, then couples it to the deck weights:
     * <ul>
     *   <li>keeps it with probability {@code min(1, pDeck / pVanilla)};</li>
     *   <li>otherwise picks among the enchantments the decks favor, in
     *       proportion to {@code pDeck − pVanilla}.</li>
     * </ul>
     * The pick then follows {@code pDeck} exactly, and differs from vanilla's
     * only as often as the two distributions differ.  A changed pick is added
     * to {@code influenced}.
     */
    private static Optional<EnchantmentInstance> pick(RandomSource random, RandomSource coupling,
                                                      List<EnchantmentInstance> available, LibraryProfile profile,
                                                      Set<Holder<Enchantment>> influenced) {
        Optional<EnchantmentInstance> drawn = WeightedRandom.getRandomItem(random, available, EnchantmentInstance::weight);
        if (drawn.isEmpty()) return drawn;

        int n = available.size();
        double[] vanilla = new double[n];
        double[] deck = new double[n];
        double vanillaTotal = 0, deckTotal = 0;
        for (int i = 0; i < n; i++) {
            EnchantmentInstance inst = available.get(i);
            vanilla[i] = inst.weight();
            deck[i] = inst.weight() * profile.weightMultiplier(inst);
            vanillaTotal += vanilla[i];
            deckTotal += deck[i];
        }
        if (vanillaTotal <= 0 || deckTotal <= 0) return drawn;

        int x = available.indexOf(drawn.get());
        double pVanilla = vanilla[x] / vanillaTotal;
        double pDeck = deck[x] / deckTotal;
        if (coupling.nextDouble() * pVanilla < pDeck) return drawn;

        double[] surplus = new double[n];
        double surplusTotal = 0;
        for (int i = 0; i < n; i++) {
            surplus[i] = Math.max(0, deck[i] / deckTotal - vanilla[i] / vanillaTotal);
            surplusTotal += surplus[i];
        }
        double r = coupling.nextDouble() * surplusTotal;
        for (int i = 0; i < n; i++) {
            r -= surplus[i];
            if (r < 0) {
                EnchantmentInstance changed = available.get(i);
                influenced.add(changed.enchantment());
                return Optional.of(changed);
            }
        }
        return drawn;
    }
}
