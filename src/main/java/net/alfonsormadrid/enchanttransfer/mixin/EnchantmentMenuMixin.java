package net.alfonsormadrid.enchanttransfer.mixin;

import java.util.List;
import java.util.stream.Stream;
import net.alfonsormadrid.enchanttransfer.library.LibraryProfile;
import net.alfonsormadrid.enchanttransfer.services.DeckEnchantmentSelector;
import net.alfonsormadrid.enchanttransfer.services.DeckWearService;
import net.alfonsormadrid.enchanttransfer.services.LibraryResonanceService;
import net.minecraft.core.Holder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hooks the Card Decks in nearby libraries into the enchanting table.
 * <ul>
 *   <li><strong>Power</strong> — decks add bookshelves' worth of power to the
 *       count vanilla passes to {@code getEnchantmentCost} (still capped at 15).</li>
 *   <li><strong>Selection</strong> — {@code getEnchantmentList} only runs on the
 *       server (inside {@code access.execute}), both for the clues and when
 *       enchanting, always with the same seed.  When decks are present the
 *       vanilla pick is replaced by {@link DeckEnchantmentSelector}.</li>
 *   <li><strong>Wear</strong> — after a successful enchant, on the list actually applied.</li>
 * </ul>
 * The logic lives in the services; this mixin only wires them in.
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {

    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Unique
    private final LibraryResonanceService enchanttransfer$libraries = new LibraryResonanceService();

    @Unique
    private final DeckEnchantmentSelector enchanttransfer$selector = new DeckEnchantmentSelector();

    @Unique
    private final DeckWearService enchanttransfer$wear = new DeckWearService(enchanttransfer$libraries);

    @Unique
    private LibraryProfile enchanttransfer$profile = LibraryProfile.EMPTY;

    @Unique
    private List<EnchantmentInstance> enchanttransfer$lastResult = List.of();

    @ModifyArg(
            method = "lambda$slotsChanged$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"),
            index = 2)
    private int enchanttransfer$addDeckPower(int bookshelves) {
        return bookshelves + this.access.evaluate(enchanttransfer$libraries::deckPower, 0);
    }

    @Redirect(
            method = "getEnchantmentList",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;selectEnchantment(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;"))
    private List<EnchantmentInstance> enchanttransfer$selectWithDecks(RandomSource random, ItemStack stack, int cost,
                                                                      Stream<Holder<Enchantment>> pool) {
        LibraryProfile profile = this.access.evaluate(enchanttransfer$libraries::scan, LibraryProfile.EMPTY);
        List<EnchantmentInstance> result = profile.isEmpty()
                ? EnchantmentHelper.selectEnchantment(random, stack, cost, pool)
                : enchanttransfer$selector.select(random, stack, cost, pool, profile);
        this.enchanttransfer$profile = profile;
        // Same reference the menu keeps using: for books it removes all but
        // one entry from it, so wear only sees what was actually applied.
        this.enchanttransfer$lastResult = result;
        return result;
    }

    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void enchanttransfer$resetLastResult(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        this.enchanttransfer$lastResult = List.of();
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void enchanttransfer$wearDecks(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        LibraryProfile profile = this.enchanttransfer$profile;
        List<EnchantmentInstance> applied = this.enchanttransfer$lastResult;
        this.access.execute((level, pos) -> enchanttransfer$wear.apply(level, profile, applied));
    }
}
