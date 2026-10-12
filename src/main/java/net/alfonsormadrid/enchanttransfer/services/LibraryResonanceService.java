package net.alfonsormadrid.enchanttransfer.services;

import net.alfonsormadrid.enchanttransfer.item.CardDeckItem;
import net.alfonsormadrid.enchanttransfer.library.DeckLibrary;
import net.alfonsormadrid.enchanttransfer.library.LibraryAffinities;
import net.alfonsormadrid.enchanttransfer.library.LibraryProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantingTableBlock;
import org.jetbrains.annotations.Nullable;

/**
 * Finds the {@link DeckLibrary}s around an enchanting table and summarizes
 * their decks into a {@link LibraryProfile}.
 * <p>
 * Uses the same positions and the same "air in between" rule vanilla
 * applies to regular bookshelves ({@link EnchantingTableBlock#BOOKSHELF_OFFSETS}),
 * but only at the table's own height: a lectern fills its whole column
 * (nothing can sit on it), standing in for the two bookshelves stacked there.
 */
public class LibraryResonanceService {

    /**
     * Every {@value #DECKS_PER_POWER_STEP} decks (holding at least one card)
     * count as {@value #POWER_PER_STEP} regular bookshelves for the table's
     * power, so a full Deck Lectern is worth the two bookshelves stacked in
     * its column.  They add up with regular bookshelves; vanilla still caps
     * the total at 15.
     */
    public static final int DECKS_PER_POWER_STEP = 3;
    public static final int POWER_PER_STEP = 2;

    public LibraryProfile scan(Level level, BlockPos tablePos) {
        LibraryProfile profile = new LibraryProfile();
        profile.setAffinityRules(LibraryAffinities.rulesAt(level.dimension(), level.getBiome(tablePos)));
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            if (offset.getY() != 0) continue;
            BlockPos between = tablePos.offset(offset.getX() / 2, offset.getY() / 2, offset.getZ() / 2);
            if (!level.getBlockState(between).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) continue;

            BlockPos libraryPos = tablePos.offset(offset);
            DeckLibrary library = libraryAt(level, libraryPos);
            if (library == null || !library.isActive()) continue;

            for (int slot = 0; slot < library.getSlotCount(); slot++) {
                ItemStack stack = library.getStack(slot);
                if (stack.getItem() instanceof CardDeckItem deck) {
                    profile.add(deck.getKind(), deck.getStorage(stack).getContents(), libraryPos, slot);
                }
            }
        }
        return profile;
    }

    /** Bookshelves' worth of power the decks around the table add. */
    public int deckPower(Level level, BlockPos tablePos) {
        return scan(level, tablePos).getDeckCount() * POWER_PER_STEP / DECKS_PER_POWER_STEP;
    }

    /** The deck library at {@code pos} (a block entity implementing {@link DeckLibrary}), if any. */
    public @Nullable DeckLibrary libraryAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof DeckLibrary library ? library : null;
    }
}
