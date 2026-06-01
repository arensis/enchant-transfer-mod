package net.alfonsormadrid.enchanttransfer.blocks.infusioncoil;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.energy.ExperienceStorage;
import net.alfonsormadrid.enchanttransfer.energy.SimpleExperienceTank;
import net.alfonsormadrid.enchanttransfer.modules.ModulePreview;
import net.alfonsormadrid.enchanttransfer.modules.ModuleType;
import net.alfonsormadrid.enchanttransfer.modules.ProcessingModule;
import net.alfonsormadrid.enchanttransfer.screens.infusioncoil.InfusionCoilScreenHandler;
import net.alfonsormadrid.enchanttransfer.services.XpConversionService;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
// InfusionCoilBlock is in this same package, no import needed.
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Infusion Coil — the first module of the Transfer Table
 * network.
 * <p>
 * Responsibilities:
 * <ul>
 *   <li>Hold a 3-slot inventory: card-in, glass-bottle-in, xp-bottle-out</li>
 *   <li>Own an {@link SimpleExperienceTank} (acts as {@link ExperienceStorage})</li>
 *   <li>Process cards over time (furnace pattern) into stored XP</li>
 *   <li>Convert glass bottles to experience bottles by draining the tank</li>
 *   <li>Self-attach to an adjacent {@link TransferTableBlockEntity} core
 *       so its tank is visible to the rest of the network</li>
 * </ul>
 */
public class InfusionCoilBlockEntity extends BlockEntity
        implements SidedInventory, ProcessingModule, ExperienceStorage, ExtendedScreenHandlerFactory<BlockPos> {

    public static final ModuleType MODULE_TYPE =
            new ModuleType(net.minecraft.util.Identifier.of(EnchantTransferMod.MOD_ID, "infusion_coil"));

    public static final int SLOT_CARD_IN = 0;
    public static final int SLOT_BOTTLE_IN = 1;
    public static final int SLOT_BOTTLE_OUT = 2;

    private static final int[] TOP_SLOTS = new int[]{SLOT_CARD_IN};
    private static final int[] SIDE_SLOTS = new int[]{SLOT_BOTTLE_IN};
    private static final int[] BOTTOM_SLOTS = new int[]{SLOT_BOTTLE_OUT};

    public static final int TANK_CAPACITY = 1000;
    public static final int TICKS_PER_INFUSION = 200; // ~10s, same ballpark as a furnace
    private static final int XP_PER_BOTTLE = 7;        // per handoff spec (≈ vanilla average)

    private static final String NBT_PROGRESS = "Progress";
    private static final String NBT_TANK = "Tank";
    private static final String NBT_CORE_POS = "CorePos";
    private static final String NBT_CORE_FACE = "CoreFace";

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);
    private final SimpleExperienceTank tank = new SimpleExperienceTank(TANK_CAPACITY);
    private final XpConversionService xpService = new XpConversionService();

    // ── Property indices (PropertyDelegate for screen sync) ─────────────────
    public static final int PROPERTY_PROGRESS   = 0;
    public static final int PROPERTY_STORED_XP  = 1;
    public static final int PROPERTY_COUNT      = 2;

    private int progress  = 0;
    /** Counts ticks since last forced sync; resets every {@value SYNC_INTERVAL} ticks. */
    private int syncTimer = 0;
    private static final int SYNC_INTERVAL = 20; // sync ~once per second while processing

    @Nullable private BlockPos cachedCorePos;
    @Nullable private Direction cachedCoreFace;
    private boolean needsAttachmentCheck = true;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case PROPERTY_PROGRESS  -> progress;
                case PROPERTY_STORED_XP -> tank.getStored();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            // Called on the CLIENT side to mirror server values
            switch (index) {
                case PROPERTY_PROGRESS  -> progress = value;
                case PROPERTY_STORED_XP -> tank.setStored(value);
            }
        }

        @Override
        public int size() {
            return PROPERTY_COUNT;
        }
    };

    public PropertyDelegate getPropertyDelegate() {
        return propertyDelegate;
    }

    /**
     * Overridden to also send a block-entity update packet to nearby clients
     * every time server-side state changes.  Without this, {@code toUpdatePacket()}
     * is defined but never triggered, so client-side {@code fillRatio} /
     * {@code isProcessing} stay at their initial values (0 / false) forever.
     */
    @Override
    public void markDirty() {
        super.markDirty();
        // ServerChunkManager.markForUpdate() queues toUpdatePacket() for all
        // nearby players — the only reliable way to push live BE state to clients.
        // world.updateListeners() only notifies block-change listeners, which is
        // not the same as sending the block-entity update packet.
        if (world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(pos);
        }
    }

    /** 0..1 fill ratio of the XP tank (for rendering). */
    public float getFillRatio() {
        return tank.getCapacity() <= 0 ? 0f : (float) tank.getStored() / tank.getCapacity();
    }

    public InfusionCoilBlockEntity(BlockPos pos, BlockState state) {
        super(EnchantTransferMod.INFUSION_COIL_BLOCK_ENTITY, pos, state);
    }

    // ── Tick ────────────────────────────────────────────────────────────────

    public static void serverTick(World world, BlockPos pos, BlockState state, InfusionCoilBlockEntity be) {
        if (be.needsAttachmentCheck) {
            be.attemptAttachToCore();
            be.needsAttachmentCheck = false;
        }

        boolean dirty = false;
        dirty |= be.tickInfusion();
        dirty |= be.tickBottleConversion();

        // Keep the ACTIVE blockstate in sync with the live progress.
        // This drives the visual model swap (idle ↔ active) and the luminance bump.
        // Block.NOTIFY_LISTENERS (2) only notifies client/listeners — no neighbour
        // recalculations, no comparator updates.  Cheap and exactly what we need.
        boolean shouldBeActive = be.progress > 0;
        if (state.contains(InfusionCoilBlock.ACTIVE) && state.get(InfusionCoilBlock.ACTIVE) != shouldBeActive) {
            world.setBlockState(pos, state.with(InfusionCoilBlock.ACTIVE, shouldBeActive), Block.NOTIFY_LISTENERS);
        }

        if (dirty) be.markDirty();

        // Emit a soft pale-blue halo of Dust particles around the knob while
        // processing.  DustParticleEffect lets us pick an arbitrary RGB tint
        // and a scale, which is exactly what we need to fake a glowing aura.
        // 0xA8D0FF is a pale powder-blue → reads as a translucent icy halo
        // rather than a solid coloured speck.  Spawned every 2 ticks (≈10/s)
        // so the cloud stays dense; with Dust's ~1–2 s lifetime this settles
        // into ~15–20 visible particles around the knob.
        if (be.progress > 0 && world instanceof ServerWorld sw && world.getTime() % 2L == 0L) {
            double cx = pos.getX() + 0.5;
            double cy = pos.getY() + 18.0 / 16.0;   // top of knob
            double cz = pos.getZ() + 0.5;
            DustParticleEffect dust = new DustParticleEffect(0xA8D0FF, 1.4f);
            sw.spawnParticles(
                dust,
                cx, cy, cz,
                1,                  // count per call
                0.14, 0.08, 0.14,   // gaussian spread XYZ (wraps & overflows knob)
                0.005               // particle initial speed (very slow drift)
            );
        }

        // Periodic sync so nearby clients (Selector screen) see live tank / processing state.
        if (be.progress > 0) {
            if (++be.syncTimer >= SYNC_INTERVAL) {
                be.syncTimer = 0;
                be.markDirty();
            }
        } else {
            be.syncTimer = 0;
        }
    }

    private boolean tickInfusion() {
        ItemStack card = inventory.get(SLOT_CARD_IN);
        if (card.isEmpty()) {
            return resetProgress();
        }

        int xpPerCard = xpService.convertSingle(card);
        if (xpPerCard <= 0 || tank.getAvailableSpace() < xpPerCard) {
            return resetProgress();
        }

        progress++;
        if (progress < TICKS_PER_INFUSION) {
            return false;
        }

        tank.insert(xpPerCard, false);
        card.decrement(1);
        progress = 0;
        return true;
    }

    private boolean tickBottleConversion() {
        if (tank.getStored() < XP_PER_BOTTLE) return false;

        ItemStack glass = inventory.get(SLOT_BOTTLE_IN);
        if (glass.isEmpty() || !glass.isOf(Items.GLASS_BOTTLE)) return false;

        ItemStack out = inventory.get(SLOT_BOTTLE_OUT);
        if (!out.isEmpty() && (!out.isOf(Items.EXPERIENCE_BOTTLE) || out.getCount() >= out.getMaxCount())) {
            return false;
        }

        tank.extract(XP_PER_BOTTLE, false);
        glass.decrement(1);
        if (out.isEmpty()) {
            inventory.set(SLOT_BOTTLE_OUT, new ItemStack(Items.EXPERIENCE_BOTTLE));
        } else {
            out.increment(1);
        }
        return true;
    }

    private boolean resetProgress() {
        if (progress == 0) return false;
        progress = 0;
        return true;
    }

    // ── Module attachment ───────────────────────────────────────────────────

    private void attemptAttachToCore() {
        if (!(world instanceof ServerWorld)) return;

        for (Direction faceFromCoil : Direction.values()) {
            BlockPos neighborPos = pos.offset(faceFromCoil);
            BlockEntity neighbor = world.getBlockEntity(neighborPos);
            if (neighbor instanceof TransferTableBlockEntity core) {
                Direction coreFace = faceFromCoil.getOpposite();
                core.attachModule(coreFace, this);
                cachedCorePos = neighborPos;
                cachedCoreFace = coreFace;
                markDirty();
                return;
            }
        }
    }

    private void detachFromCachedCore() {
        if (world == null || cachedCorePos == null || cachedCoreFace == null) return;

        BlockEntity neighbor = world.getBlockEntity(cachedCorePos);
        if (neighbor instanceof TransferTableBlockEntity core) {
            core.detachModule(cachedCoreFace);
        }
        cachedCorePos = null;
        cachedCoreFace = null;
    }

    @Override
    public void markRemoved() {
        detachFromCachedCore();
        super.markRemoved();
    }

    // ── TransferTableModule ─────────────────────────────────────────────────

    @Override
    public ModuleType getModuleType() {
        return MODULE_TYPE;
    }

    @Nullable
    @Override
    public BlockPos getCorePos() {
        return cachedCorePos;
    }

    @Nullable
    @Override
    public Direction getCoreFace() {
        return cachedCoreFace;
    }

    @Override
    public void onAttachedToCore(BlockPos corePos, Direction coreFace) {
        this.cachedCorePos = corePos;
        this.cachedCoreFace = coreFace;
        markDirty();
    }

    @Override
    public void onDetachedFromCore() {
        this.cachedCorePos = null;
        this.cachedCoreFace = null;
        markDirty();
    }

    @Override
    public ModulePreview buildPreview() {
        return ModulePreview.builder(net.minecraft.util.Identifier.of(EnchantTransferMod.MOD_ID, "gui/module/infusion_coil"))
                .value(tank.getStored(), tank.getCapacity())
                .progress(getProgressRatio())
                .build();
    }

    // ── ProcessingModule ────────────────────────────────────────────────────

    @Override
    public int getProgress() {
        return progress;
    }

    @Override
    public int getMaxProgress() {
        return TICKS_PER_INFUSION;
    }

    @Override
    public boolean isProcessing() {
        return progress > 0;
    }

    // ── ExperienceStorage (delegated to tank) ───────────────────────────────

    @Override
    public int insert(int points, boolean simulate) {
        int result = tank.insert(points, simulate);
        if (!simulate && result > 0) markDirty();
        return result;
    }

    @Override
    public int extract(int points, boolean simulate) {
        int result = tank.extract(points, simulate);
        if (!simulate && result > 0) markDirty();
        return result;
    }

    @Override
    public int getStored() {
        return tank.getStored();
    }

    @Override
    public int getCapacity() {
        return tank.getCapacity();
    }

    // ── SidedInventory ──────────────────────────────────────────────────────

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case UP -> TOP_SLOTS;
            case DOWN -> BOTTOM_SLOTS;
            default -> SIDE_SLOTS;
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_BOTTLE_OUT;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_CARD_IN -> stack.isOf(EnchantTransferMod.MAGIC_CARD_ITEM);
            case SLOT_BOTTLE_IN -> stack.isOf(Items.GLASS_BOTTLE);
            case SLOT_BOTTLE_OUT -> false;
            default -> false;
        };
    }

    @Override
    public int size() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack removed = Inventories.splitStack(inventory, slot, amount);
        if (!removed.isEmpty()) markDirty();
        return removed;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack removed = Inventories.removeStack(inventory, slot);
        if (!removed.isEmpty()) markDirty();
        return removed;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
        if (stack.getCount() > getMaxCountPerStack()) {
            stack.setCount(getMaxCountPerStack());
        }
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null || world.getBlockEntity(pos) != this) return false;
        return player.squaredDistanceTo(pos.toCenterPos()) <= 64.0;
    }

    @Override
    public void clear() {
        inventory.clear();
        markDirty();
    }

    // ── ExtendedScreenHandlerFactory ────────────────────────────────────────

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    /**
     * Called by the server to push a lightweight state update to nearby clients.
     * This lets the Selector screen and the BER see live tank / processing data
     * without waiting for the player to open the coil's own screen.
     */
    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    /**
     * Provides the NBT data that {@link BlockEntityUpdateS2CPacket#create(BlockEntity)}
     * serialises into the update packet.  The default implementation returns an
     * <em>empty</em> compound, so we override it here to include the full state
     * (tank, progress) — otherwise the client BE is never populated.
     */
    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }

    /** Sends this coil's position to the client so the screen can render the nav row. */
    @Override
    public BlockPos getScreenOpeningData(ServerPlayerEntity player) {
        return this.pos;
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new InfusionCoilScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    // ── NBT ─────────────────────────────────────────────────────────────────

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, inventory);
        tank.readData(view.getReadView(NBT_TANK));
        progress = view.getInt(NBT_PROGRESS, 0);

        view.read(NBT_CORE_POS, BlockPos.CODEC).ifPresent(p -> cachedCorePos = p);
        view.getOptionalString(NBT_CORE_FACE).ifPresent(name -> {
            try { cachedCoreFace = Direction.valueOf(name); } catch (IllegalArgumentException ignored) {}
        });
        // Re-validate attachment on next tick — neighbor BE may not be loaded yet.
        needsAttachmentCheck = true;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory);
        tank.writeData(view.get(NBT_TANK));
        view.putInt(NBT_PROGRESS, progress);

        if (cachedCorePos != null) {
            view.put(NBT_CORE_POS, BlockPos.CODEC, cachedCorePos);
        }
        if (cachedCoreFace != null) {
            view.putString(NBT_CORE_FACE, cachedCoreFace.name());
        }
    }
}
