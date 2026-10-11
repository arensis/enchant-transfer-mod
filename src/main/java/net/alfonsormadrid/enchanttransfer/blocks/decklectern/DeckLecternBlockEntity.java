package net.alfonsormadrid.enchanttransfer.blocks.decklectern;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.item.CardDeckItem;
import net.alfonsormadrid.enchanttransfer.library.DeckLibrary;
import net.alfonsormadrid.enchanttransfer.screens.decklectern.DeckLecternScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for the Deck Lectern: 3 deck slots, exposed to the
 * enchanting logic as a {@link DeckLibrary}.
 * <p>
 * Every change re-derives the slot lamps into the blockstate (so the baked
 * model shows them) and pushes the items to nearby clients (so the renderer
 * draws the decks on the tray).
 */
public class DeckLecternBlockEntity extends BlockEntity implements Container, MenuProvider, DeckLibrary {

    public static final int SLOT_COUNT = 3;

    public static final int PROP_ACTIVE = 0;
    public static final int PROPERTY_COUNT = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    /** Mirrors {@code active} to the open screen (the gem next to the slots). */
    private final ContainerData propertyDelegate = new ContainerData() {
        @Override
        public int get(int index) {
            return index == PROP_ACTIVE && isActive() ? 1 : 0;
        }

        @Override
        public void set(int index, int value) {
            // Read-only: the blockstate is the source of truth.
        }

        @Override
        public int getCount() {
            return PROPERTY_COUNT;
        }
    };

    public DeckLecternBlockEntity(BlockPos pos, BlockState state) {
        super(EnchantTransferMod.DECK_LECTERN_BLOCK_ENTITY, pos, state);
    }

    public static boolean isDeck(ItemStack stack) {
        return stack.getItem() instanceof CardDeckItem;
    }

    // ── DeckLibrary ────────────────────────────────────────────────────────

    @Override
    public int getSlotCount() {
        return SLOT_COUNT;
    }

    @Override
    public ItemStack getStack(int slot) {
        return getItem(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        setItem(slot, stack);
    }

    @Override
    public boolean isActive() {
        return getBlockState().getValue(DeckLecternBlock.ACTIVE);
    }

    // ── Change propagation: lamps + client sync ────────────────────────────

    @Override
    public void setChanged() {
        super.setChanged();
        if (!(level instanceof ServerLevel serverLevel)) return;

        BlockState state = getBlockState();
        BlockState updated = state;
        for (int i = 0; i < SLOT_COUNT; i++) {
            updated = updated.setValue(DeckLecternBlock.SLOTS.get(i), LampState.of(items.get(i)));
        }
        if (updated != state) {
            serverLevel.setBlock(worldPosition, updated, Block.UPDATE_CLIENTS);
        }
        serverLevel.getChunkSource().blockChanged(worldPosition);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    // ── Persistence ────────────────────────────────────────────────────────

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        items.clear();
        ContainerHelper.loadAllItems(view, items);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, items);
    }

    // ── Container ──────────────────────────────────────────────────────────

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.limitSize(getMaxStackSize());
        setChanged();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isDeck(stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    /** Client-side read of the decks, for the renderer. */
    public NonNullList<ItemStack> getItems() {
        return items;
    }

    // ── Screen ─────────────────────────────────────────────────────────────

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new DeckLecternScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }
}
