package net.alfonsormadrid.enchanttransfer.screens.zincsmelter;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.stream.IntStream;

/**
 * Screen handler for the Zinc Smelter.
 * <p>
 * 4 block slots: input1 (0), input2 (1), fuel (2), output (3).
 * Followed by 27 inventory + 9 hotbar = 36 player slots.
 */
public class ZincSmelterScreenHandler extends AbstractContainerMenu {

    private static final int GUI_SLOTS_END    = 4;
    private static final int PLAYER_INV_START = 4;
    private static final int HOTBAR_END       = 40;

    private final Container inventory;
    private final ContainerData propertyDelegate;

    /** Client-side constructor. */
    public ZincSmelterScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory,
                new SimpleContainer(ZincSmelterBlockEntity.INVENTORY_SIZE),
                new SimpleContainerData(ZincSmelterBlockEntity.PROPERTY_COUNT));
    }

    /** Server-side constructor — bound to the live block entity. */
    public ZincSmelterScreenHandler(int syncId, Inventory playerInventory,
                                    Container inventory, ContainerData propertyDelegate) {
        super(EnchantTransferMod.ZINC_SMELTER_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, ZincSmelterBlockEntity.INVENTORY_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.startOpen(playerInventory.player);

        // ── Block slots ──────────────────────────────────────────────────────
        // Slot positions from the GUI handoff (gui-local coordinates)
        addSlot(new Slot(inventory, ZincSmelterBlockEntity.SLOT_INPUT1, 12, 22));
        addSlot(new Slot(inventory, ZincSmelterBlockEntity.SLOT_INPUT2, 12, 52));
        addSlot(new FuelSlot(inventory, ZincSmelterBlockEntity.SLOT_FUEL, 42, 62));
        addSlot(new FurnaceResultSlot(playerInventory.player, inventory,
                ZincSmelterBlockEntity.SLOT_OUTPUT, 136, 30));

        // ── Player inventory (3×9) ──────────────────────────────────────────
        // Rows at y=98, 116, 134 (18px spacing), items start at x=8.
        IntStream.range(0, 3).forEach(row ->
                IntStream.range(0, 9).forEach(col ->
                        addSlot(new Slot(playerInventory, col + row * 9 + 9,
                                8 + col * 18, 98 + row * 18))));

        // ── Hotbar (y=158, 24px gap after last inventory row) ───────────────
        IntStream.range(0, 9).forEach(col ->
                addSlot(new Slot(playerInventory, col, 8 + col * 18, 158)));

        addDataSlots(propertyDelegate);
    }

    // ── Property accessors for the screen ─────────────────────────────────

    public boolean isLit() {
        return propertyDelegate.get(ZincSmelterBlockEntity.PROP_LIT_TIME) > 0;
    }

    public float getLitProgress() {
        int duration = propertyDelegate.get(ZincSmelterBlockEntity.PROP_LIT_DURATION);
        if (duration <= 0) return 0f;
        return (float) propertyDelegate.get(ZincSmelterBlockEntity.PROP_LIT_TIME) / duration;
    }

    public float getCookProgress() {
        int total = propertyDelegate.get(ZincSmelterBlockEntity.PROP_COOK_TOTAL);
        if (total <= 0) return 0f;
        return (float) propertyDelegate.get(ZincSmelterBlockEntity.PROP_COOK_PROGRESS) / total;
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
            // From smelter to player
            if (!moveItemStackTo(original, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player to smelter — route by item type
            if (ZincSmelterBlockEntity.isValidFuel(original)) {
                if (!moveItemStackTo(original, ZincSmelterBlockEntity.SLOT_FUEL,
                        ZincSmelterBlockEntity.SLOT_FUEL + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Try input1 first, then input2
                if (!moveItemStackTo(original, ZincSmelterBlockEntity.SLOT_INPUT1,
                        ZincSmelterBlockEntity.SLOT_INPUT1 + 1, false)
                        && !moveItemStackTo(original, ZincSmelterBlockEntity.SLOT_INPUT2,
                        ZincSmelterBlockEntity.SLOT_INPUT2 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    /** Fuel slot that only accepts valid smelter fuels. */
    private static class FuelSlot extends Slot {
        public FuelSlot(Container inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return ZincSmelterBlockEntity.isValidFuel(stack);
        }
    }
}
