package net.alfonsormadrid.enchanttransfer.services;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.alfonsormadrid.enchanttransfer.item.CardDeckItem;
import net.alfonsormadrid.enchanttransfer.item.CardWear;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.alfonsormadrid.enchanttransfer.library.DeckCardStorage;
import net.alfonsormadrid.enchanttransfer.library.DeckLibrary;
import net.alfonsormadrid.enchanttransfer.library.LibraryProfile;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

/**
 * Deck wear (see DESIGN.md, "Desgaste de cartas"): the decks pay, in card
 * reserve, for the enchantments their bonus brought in.  Without it the bias
 * would be permanent and free (enchant → extract cards → fill decks → enchant better…).
 * <p>
 * Only enchantments whose pick the deck bonus changed count
 * ({@link DeckEnchantmentSelector.Selection#influenced}): not those vanilla
 * would have rolled anyway, nor those due to the decks' power.  Each costs
 * {@link CardWear#wearFor}, split among the deck kinds that
 * {@linkplain net.alfonsormadrid.enchanttransfer.library.DeckEffect#wearsOn wear on it}
 * in proportion to their {@linkplain LibraryProfile#extra extra}, each part at
 * that kind's {@linkplain net.alfonsormadrid.enchanttransfer.library.DeckEffect#wearRate rate}.
 * <p>
 * Wear is deterministic, so the {@link Plan} is known when the table shows
 * its options: one the decks can't pay for is disabled.
 */
public class DeckWearService {

    /** Wear each deck kind pays for one option, in {@link CardWear} units. */
    public record Plan(Map<DeckKind, Integer> units) {

        public static final Plan NONE = new Plan(Map.of());

        public Plan {
            units = Map.copyOf(units);
        }

        public boolean isEmpty() {
            return units.isEmpty();
        }

        /** Whether the decks around the table hold enough reserve for every part. */
        public boolean affordable(LibraryProfile profile) {
            return units.entrySet().stream().allMatch(e -> e.getValue() <= profile.getReserve(e.getKey()));
        }
    }

    private final LibraryResonanceService libraries;

    public DeckWearService(LibraryResonanceService libraries) {
        this.libraries = libraries;
    }

    /** Wear for enchanting with {@code applied} (the option's final list). */
    public Plan plan(LibraryProfile profile, List<EnchantmentInstance> applied, Set<Holder<Enchantment>> influenced) {
        if (profile.isEmpty() || applied.isEmpty() || influenced.isEmpty()) return Plan.NONE;

        Map<DeckKind, Double> units = new EnumMap<>(DeckKind.class);
        for (EnchantmentInstance inst : applied) {
            if (!influenced.contains(inst.enchantment())) continue;
            int wear = CardWear.wearFor(inst.enchantment(), inst.level());

            Map<DeckKind, Double> extras = new EnumMap<>(DeckKind.class);
            double total = 0;
            for (DeckKind kind : profile.getPresentKinds()) {
                if (!kind.effect().wearsOn(inst)) continue;
                double extra = profile.extra(kind, inst);
                if (extra <= 0) continue;
                extras.put(kind, extra);
                total += extra;
            }
            for (Map.Entry<DeckKind, Double> e : extras.entrySet()) {
                double share = e.getValue() / total * e.getKey().effect().wearRate(inst, profile);
                units.merge(e.getKey(), wear * share, Double::sum);
            }
        }

        Map<DeckKind, Integer> rounded = new EnumMap<>(DeckKind.class);
        units.forEach((kind, u) -> {
            int r = (int) Math.round(u);
            if (r > 0) rounded.put(kind, r);
        });
        return rounded.isEmpty() ? Plan.NONE : new Plan(rounded);
    }

    /**
     * Wears the decks by {@code plan}: for each kind, deck after deck in scan
     * order (the first deck's cards, then the leftover on the next deck…).
     */
    public void apply(Level level, LibraryProfile profile, Plan plan) {
        plan.units().forEach((kind, units) -> {
            int left = units;
            for (LibraryProfile.DeckLocation location : profile.getLocations()) {
                if (left <= 0) break;
                if (location.kind() != kind) continue;
                DeckLibrary library = libraries.libraryAt(level, location.libraryPos());
                if (library == null) continue;

                ItemStack deck = library.getStack(location.slot()).copy();
                if (!(deck.getItem() instanceof CardDeckItem item)) continue;
                DeckCardStorage storage = item.getStorage(deck);
                int before = left;
                left = storage.wear(left);
                if (left == before) continue;

                library.setStack(location.slot(), deck);
                level.playSound(null, location.libraryPos(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 0.8F);
            }
        });
    }
}
