package net.alfonsormadrid.enchanttransfer.services;

import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Optional;

/**
 * Converts a Magic Card stack into raw XP points for the Infusion Coil.
 * <p>
 * Formula (intentionally simple so it can be re-balanced from a single place):
 * <pre>
 *   points = BASE_POINTS_PER_LEVEL × level × rarityFactor(weight) × LOSS_FACTOR
 * </pre>
 * Lower enchantment weight → higher rarity → more XP returned.
 * Loss factor below 1.0 makes infusing always a net loss versus actually
 * applying the card, so the mechanic is a sink, not a free conversion.
 */
public class XpConversionService {

    private static final int BASE_POINTS_PER_LEVEL = 10;
    private static final double LOSS_FACTOR = 0.75;
    private static final int MIN_POINTS_PER_CARD = 5;

    /**
     * XP points yielded by infusing one single card from the stack
     * (count-agnostic). Use this for per-tick infusion logic.
     */
    public int convertSingle(ItemStack card) {
        if (card.isEmpty()) return 0;

        ItemEnchantmentsComponent enchants = card.getEnchantments();
        Optional<RegistryEntry<Enchantment>> first = enchants.getEnchantments().stream().findFirst();
        // Unenchanted cards yield a flat base amount instead of 0 so they can
        // always be infused (the enchanted-card path adds a rarity bonus on top).
        if (first.isEmpty()) return MIN_POINTS_PER_CARD;

        RegistryEntry<Enchantment> entry = first.get();
        int level = enchants.getLevel(entry);
        if (level <= 0) return 0;

        int weight = entry.value().getWeight();
        double rarityFactor = rarityFactorFor(weight);

        double raw = BASE_POINTS_PER_LEVEL * level * rarityFactor * LOSS_FACTOR;
        return Math.max(MIN_POINTS_PER_CARD, (int) Math.round(raw));
    }

    /**
     * Total XP points for the full stack, useful for previews and tooltips.
     */
    public int convert(ItemStack card) {
        return convertSingle(card) * card.getCount();
    }

    private double rarityFactorFor(int weight) {
        // Vanilla weights span ~1..10. Invert so that rarer = higher factor.
        int clamped = Math.clamp(weight, 1, 10);
        return (11 - clamped);
    }
}
