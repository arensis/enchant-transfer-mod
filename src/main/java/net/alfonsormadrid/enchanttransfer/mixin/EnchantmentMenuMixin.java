package net.alfonsormadrid.enchanttransfer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.alfonsormadrid.enchanttransfer.library.DeckInfluenceView;
import net.alfonsormadrid.enchanttransfer.library.LibraryProfile;
import net.alfonsormadrid.enchanttransfer.services.DeckEnchantmentSelector;
import net.alfonsormadrid.enchanttransfer.services.DeckWearService;
import net.alfonsormadrid.enchanttransfer.services.LibraryResonanceService;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
 *   <li><strong>Influence</strong> — the selector reports the picks the deck
 *       bonus changed; a data slot syncs, per option, whether there are any,
 *       for the screen's mark.</li>
 *   <li><strong>Wear</strong> — a {@link DeckWearService.Plan} per option, on
 *       its final list (books included); options the decks can't pay for
 *       are refused and flagged for the screen; the plan is applied after a
 *       successful enchant.</li>
 * </ul>
 * The logic lives in the services; this mixin only wires them in.
 */
@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin extends AbstractContainerMenu implements DeckInfluenceView {

    @Shadow
    @Final
    private ContainerLevelAccess access;

    @Shadow
    @Final
    private DataSlot enchantmentSeed;

    @Unique
    private final LibraryResonanceService enchanttransfer$libraries = new LibraryResonanceService();

    @Unique
    private final DeckEnchantmentSelector enchanttransfer$selector = new DeckEnchantmentSelector();

    @Unique
    private final DeckWearService enchanttransfer$wear = new DeckWearService(enchanttransfer$libraries);

    /** Seeds the selector's coupling random apart from the table's own (same option seed). */
    @Unique
    private static final long enchanttransfer$COUPLING_SALT = 0x9E3779B97F4A7C15L;

    /**
     * Synced to the client: bit {@code i} when the decks changed option {@code i},
     * bit {@code 3 + i} when they lack the reserve to pay for it.
     */
    @Unique
    private final DataSlot enchanttransfer$influence = DataSlot.standalone();

    @Unique
    private static final int enchanttransfer$NO_RESERVE_SHIFT = 3;

    @Unique
    private LibraryProfile enchanttransfer$profile = LibraryProfile.EMPTY;

    @Unique
    private Set<Holder<Enchantment>> enchanttransfer$lastInfluenced = Set.of();

    @Unique
    private DeckWearService.Plan enchanttransfer$lastPlan = DeckWearService.Plan.NONE;

    private EnchantmentMenuMixin(MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    // Runs on both sides (the client constructor delegates to this one), so
    // the data slot gets the same index on server and client.
    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void enchanttransfer$addInfluenceSlot(int containerId, Inventory inventory, ContainerLevelAccess access,
                                                  CallbackInfo ci) {
        this.addDataSlot(enchanttransfer$influence);
    }

    @ModifyArg(
            method = "lambda$slotsChanged$0",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"),
            index = 2)
    private int enchanttransfer$addDeckPower(int bookshelves) {
        return bookshelves + this.access.evaluate(enchanttransfer$libraries::deckPower, 0);
    }

    // WrapOperation (MixinExtras, bundled with Fabric Loader) instead of
    // @Redirect so other mods wrapping the same call chain with ours
    // instead of failing to load.
    @WrapOperation(
            method = "getEnchantmentList",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;selectEnchantment(Lnet/minecraft/util/RandomSource;Lnet/minecraft/world/item/ItemStack;ILjava/util/stream/Stream;)Ljava/util/List;"))
    private List<EnchantmentInstance> enchanttransfer$selectWithDecks(RandomSource random, ItemStack stack, int cost,
                                                                      Stream<Holder<Enchantment>> pool,
                                                                      Operation<List<EnchantmentInstance>> original,
                                                                      @Local(argsOnly = true, ordinal = 0) int option) {
        LibraryProfile profile = this.access.evaluate(enchanttransfer$libraries::scan, LibraryProfile.EMPTY);
        List<EnchantmentInstance> result;
        Set<Holder<Enchantment>> influenced;
        if (profile.isEmpty()) {
            result = original.call(random, stack, cost, pool);
            influenced = Set.of();
        } else {
            // Same seed vanilla gives the table's random right before this call.
            long optionSeed = this.enchantmentSeed.get() + option;
            RandomSource coupling = RandomSource.create(optionSeed ^ enchanttransfer$COUPLING_SALT);
            DeckEnchantmentSelector.Selection selection =
                    enchanttransfer$selector.select(random, coupling, stack, cost, pool, profile);
            result = selection.enchantments();
            influenced = selection.influenced();
        }
        this.enchanttransfer$profile = profile;
        this.enchanttransfer$lastInfluenced = influenced;
        enchanttransfer$setFlag(option, !influenced.isEmpty());
        return result;
    }

    @Inject(method = "getEnchantmentList", at = @At("HEAD"))
    private void enchanttransfer$resetSelection(RegistryAccess registryAccess, ItemStack stack, int option, int cost,
                                                CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        this.enchanttransfer$profile = LibraryProfile.EMPTY;
        this.enchanttransfer$lastInfluenced = Set.of();
    }

    // On the final list: for books vanilla has removed an entry by now.
    @Inject(method = "getEnchantmentList", at = @At("RETURN"))
    private void enchanttransfer$planWear(RegistryAccess registryAccess, ItemStack stack, int option, int cost,
                                          CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        LibraryProfile profile = this.enchanttransfer$profile;
        DeckWearService.Plan plan = enchanttransfer$wear.plan(profile, cir.getReturnValue(), this.enchanttransfer$lastInfluenced);
        this.enchanttransfer$lastPlan = plan;
        enchanttransfer$setFlag(enchanttransfer$NO_RESERVE_SHIFT + option, !plan.affordable(profile));
    }

    // Runs on both sides: the client doesn't even send the click.
    @Inject(method = "clickMenuButton", at = @At("HEAD"), cancellable = true)
    private void enchanttransfer$refuseWithoutReserve(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        this.enchanttransfer$lastPlan = DeckWearService.Plan.NONE;
        if (enchanttransfer$lacksReserve(id)) cir.setReturnValue(false);
    }

    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void enchanttransfer$wearDecks(Player player, int id, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        LibraryProfile profile = this.enchanttransfer$profile;
        DeckWearService.Plan plan = this.enchanttransfer$lastPlan;
        if (plan.isEmpty()) return;
        this.access.execute((level, pos) -> enchanttransfer$wear.apply(level, profile, plan));
    }

    @Unique
    private void enchanttransfer$setFlag(int index, boolean set) {
        if (index < 0 || index >= 6) return;
        int bit = 1 << index;
        int flags = enchanttransfer$influence.get();
        enchanttransfer$influence.set(set ? flags | bit : flags & ~bit);
    }

    @Unique
    private boolean enchanttransfer$flag(int index) {
        return (enchanttransfer$influence.get() & (1 << index)) != 0;
    }

    @Override
    public boolean enchanttransfer$isDeckInfluenced(int option) {
        return option >= 0 && option < 3 && enchanttransfer$flag(option);
    }

    @Override
    public boolean enchanttransfer$lacksReserve(int option) {
        return option >= 0 && option < 3 && enchanttransfer$flag(enchanttransfer$NO_RESERVE_SHIFT + option);
    }
}
