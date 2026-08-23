package net.alfonsormadrid.enchanttransfer.screens.zincsmelter;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.FurnaceOutputSlot;
import net.minecraft.screen.slot.Slot;

import java.util.stream.IntStream;

/**
 * Screen handler for the Zinc Smelter.
 * <p>
 * 4 block slots: input1 (0), input2 (1), fuel (2), output (3).
 * Followed by 27 inventory + 9 hotbar = 36 player slots.
 */
public class ZincSmelterScreenHandler extends ScreenHandler {

    private static final int GUI_SLOTS_END    = 4;
    private static final int PLAYER_INV_START = 4;
    private static final int HOTBAR_END       = 40;

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    /** Client-side constructor. */
    public ZincSmelterScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory,
                new SimpleInventory(ZincSmelterBlockEntity.INVENTORY_SIZE),
                new ArrayPropertyDelegate(ZincSmelterBlockEntity.PROPERTY_COUNT));
    }

    /** Server-side constructor — bound to the live block entity. */
    public ZincSmelterScreenHandler(int syncId, PlayerInventory playerInventory,
                                    Inventory inventory, PropertyDelegate propertyDelegate) {
        super(EnchantTransferMod.ZINC_SMELTER_SCREEN_HANDLER, syncId);
        checkSize(inventory, ZincSmelterBlockEntity.INVENTORY_SIZE);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        // ── Block slots ──────────────────────────────────────────────────────
        // Slot positions from the GUI handoff (gui-local coordinates)
        addSlot(new Slot(inventory, ZincSmelterBlockEntity.SLOT_INPUT1, 12, 22));
        addSlot(new Slot(inventory, ZincSmelterBlockEntity.SLOT_INPUT2, 12, 52));
        addSlot(new FuelSlot(inventory, ZincSmelterBlockEntity.SLOT_FUEL, 42, 62));
        addSlot(new FurnaceOutputSlot(playerInventory.player, inventory,
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

        addProperties(propertyDelegate);
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
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack original = slot.getStack();
        ItemStack copy = original.copy();

        if (slotIndex < GUI_SLOTS_END) {
            // From smelter to player
            if (!insertItem(original, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From player to smelter — route by item type
            if (ZincSmelterBlockEntity.isValidFuel(original)) {
                if (!insertItem(original, ZincSmelterBlockEntity.SLOT_FUEL,
                        ZincSmelterBlockEntity.SLOT_FUEL + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Try input1 first, then input2
                if (!insertItem(original, ZincSmelterBlockEntity.SLOT_INPUT1,
                        ZincSmelterBlockEntity.SLOT_INPUT1 + 1, false)
                        && !insertItem(original, ZincSmelterBlockEntity.SLOT_INPUT2,
                        ZincSmelterBlockEntity.SLOT_INPUT2 + 1, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (original.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return copy;
    }

    /** Fuel slot that only accepts valid smelter fuels. */
    private static class FuelSlot extends Slot {
        public FuelSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            return ZincSmelterBlockEntity.isValidFuel(stack);
        }
    }
}
