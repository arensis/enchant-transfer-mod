package net.alfonsormadrid.enchanttransfer.screens.decklectern;

import java.util.stream.IntStream;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.decklectern.DeckLecternBlockEntity;
import net.alfonsormadrid.enchanttransfer.blocks.decklectern.LampState;
import net.alfonsormadrid.enchanttransfer.screens.decklectern.slot.DeckSlot;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Screen handler for the Deck Lectern.
 * <p>
 * 3 deck slots (0..2), followed by 27 inventory + 9 hotbar = 36 player slots.
 */
public class DeckLecternScreenHandler extends AbstractContainerMenu {

    public static final int[] DECK_SLOT_X = {53, 80, 107};
    public static final int DECK_SLOT_Y = 27;

    private static final int GUI_SLOTS_END = DeckLecternBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_START = GUI_SLOTS_END;
    private static final int HOTBAR_END = PLAYER_INV_START + 36;

    private final Container inventory;
    private final ContainerData propertyDelegate;

    /** Client-side constructor. */
    public DeckLecternScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory,
                new SimpleContainer(DeckLecternBlockEntity.SLOT_COUNT),
                new SimpleContainerData(DeckLecternBlockEntity.PROPERTY_COUNT));
    }

    /** Server-side constructor — bound to the live block entity. */
    public DeckLecternScreenHandler(int syncId, Inventory playerInventory,
                                    Container inventory, ContainerData propertyDelegate) {
        super(EnchantTransferMod.DECK_LECTERN_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, DeckLecternBlockEntity.SLOT_COUNT);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.startOpen(playerInventory.player);

        for (int i = 0; i < DeckLecternBlockEntity.SLOT_COUNT; i++) {
            addSlot(new DeckSlot(inventory, i, DECK_SLOT_X[i], DECK_SLOT_Y));
        }

        IntStream.range(0, 3).forEach(row ->
                IntStream.range(0, 9).forEach(col ->
                        addSlot(new Slot(playerInventory, col + row * 9 + 9,
                                8 + col * 18, 84 + row * 18))));
        IntStream.range(0, 9).forEach(col ->
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 142)));

        addDataSlots(propertyDelegate);
    }

    public boolean isActive() {
        return propertyDelegate.get(DeckLecternBlockEntity.PROP_ACTIVE) != 0;
    }

    public boolean hasCards() {
        for (int i = 0; i < DeckLecternBlockEntity.SLOT_COUNT; i++) {
            if (lampState(i) == LampState.FULL) return true;
        }
        return false;
    }

    /** Lamp shown under a deck slot, from the synced slot contents. */
    public LampState lampState(int slot) {
        return LampState.of(slots.get(slot).getItem());
    }

    @Override
    public boolean stillValid(Player player) {
        return inventory.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        inventory.stopOpen(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (slotIndex < GUI_SLOTS_END) {
            if (!moveItemStackTo(original, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else if (!DeckLecternBlockEntity.isDeck(original)
                || !moveItemStackTo(original, 0, GUI_SLOTS_END, false)) {
            return ItemStack.EMPTY;
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
