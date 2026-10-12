package net.alfonsormadrid.enchanttransfer.services;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import net.alfonsormadrid.enchanttransfer.item.CardDeckItem;
import net.alfonsormadrid.enchanttransfer.item.DeckKind;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.library.DeckCardStorage;
import net.alfonsormadrid.enchanttransfer.library.DeckLibrary;
import net.alfonsormadrid.enchanttransfer.library.LibraryProfile;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;

/**
 * Deck wear: every enchantment the library helped with may consume a card.
 * Without it the bias would be permanent and free (enchant → extract cards →
 * fill decks → enchant better…).
 * <p>
 * Only enchantments whose pick the deck bonus changed count
 * ({@link DeckEnchantmentSelector.Selection#influenced}): not those vanilla
 * would have rolled anyway, nor those due to the decks' power.  For each of them and each
 * deck kind present whose effect
 * {@link net.alfonsormadrid.enchanttransfer.library.DeckEffect#wearsOn wears on it}:
 * {@value #WEAR_CHANCE} chance to consume the cheapest card from a deck of
 * that kind, preferring cards of that same enchantment.
 */
public class DeckWearService {

    private static final float WEAR_CHANCE = 0.2F;

    private final LibraryResonanceService libraries;

    public DeckWearService(LibraryResonanceService libraries) {
        this.libraries = libraries;
    }

    public void apply(Level level, LibraryProfile profile, List<EnchantmentInstance> applied,
                      Set<Holder<Enchantment>> influenced) {
        if (profile.isEmpty() || applied.isEmpty() || influenced.isEmpty()) return;
        RandomSource random = level.getRandom();

        for (EnchantmentInstance inst : applied) {
            if (!influenced.contains(inst.enchantment())) continue;
            for (DeckKind kind : profile.getPresentKinds()) {
                if (!kind.effect().wearsOn(inst) || random.nextFloat() >= WEAR_CHANCE) continue;
                Predicate<ItemStack> sameEnchantment = card -> MagicCardItem.enchantmentOf(card)
                        .map(e -> e.equals(inst.enchantment())).orElse(false);
                if (!consumeOne(level, profile, kind, sameEnchantment)) {
                    consumeOne(level, profile, kind, card -> true);
                }
            }
        }
    }

    private boolean consumeOne(Level level, LibraryProfile profile, DeckKind kind, Predicate<ItemStack> filter) {
        for (LibraryProfile.DeckLocation location : profile.getLocations()) {
            if (location.kind() != kind) continue;
            DeckLibrary library = libraries.libraryAt(level, location.libraryPos());
            if (library == null) continue;

            ItemStack deck = library.getStack(location.slot()).copy();
            if (!(deck.getItem() instanceof CardDeckItem item)) continue;
            DeckCardStorage storage = item.getStorage(deck);
            if (!storage.consumeOne(filter)) continue;

            library.setStack(location.slot(), deck);
            level.playSound(null, location.libraryPos(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 0.8F);
            return true;
        }
        return false;
    }
}
