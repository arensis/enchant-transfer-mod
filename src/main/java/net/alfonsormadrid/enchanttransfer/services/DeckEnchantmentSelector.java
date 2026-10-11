package net.alfonsormadrid.enchanttransfer.services;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.function.ToIntFunction;
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
 * Uses the table's seeded {@code RandomSource}, so the clue shown in the
 * GUI matches the final result.
 */
public class DeckEnchantmentSelector {

    /** Keeps fractional weight factors when WeightedRandom needs ints. */
    private static final int WEIGHT_SCALE = 100;

    public List<EnchantmentInstance> select(RandomSource random, ItemStack stack, int cost,
                                            Stream<Holder<Enchantment>> pool, LibraryProfile profile) {
        List<EnchantmentInstance> results = Lists.newArrayList();
        Enchantable enchantable = stack.get(DataComponents.ENCHANTABLE);
        if (enchantable == null) return results;

        // ── Vanilla: cost jitter ─────────────────────────────────────────
        cost += 1 + random.nextInt(enchantable.value() / 4 + 1) + random.nextInt(enchantable.value() / 4 + 1);
        float jitter = (random.nextFloat() + random.nextFloat() - 1.0F) * 0.15F;
        cost = Mth.clamp(Math.round(cost + cost * jitter), 1, Integer.MAX_VALUE);

        // ── Vanilla selection loop, with deck-weighted candidates ───────
        ToIntFunction<EnchantmentInstance> weight =
                inst -> Math.max(1, (int) Math.round(inst.weight() * profile.weightMultiplier(inst) * WEIGHT_SCALE));
        List<EnchantmentInstance> available = EnchantmentHelper.getAvailableEnchantmentResults(cost, stack, pool);
        if (!available.isEmpty()) {
            WeightedRandom.getRandomItem(random, available, weight).ifPresent(results::add);
            while (random.nextInt(50) <= cost) {
                if (!results.isEmpty()) {
                    EnchantmentHelper.filterCompatibleEnchantments(available, results.getLast());
                }
                if (available.isEmpty()) break;
                WeightedRandom.getRandomItem(random, available, weight).ifPresent(results::add);
                cost /= 2;
            }
        }
        return results;
    }
}
