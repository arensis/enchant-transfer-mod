package net.alfonsormadrid.enchanttransfer.screens.infusioncoil;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.alfonsormadrid.enchanttransfer.gui.infusioncoil.InfusionCoilGuiMetrics;
import net.alfonsormadrid.enchanttransfer.gui.infusioncoil.InfusionCoilSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot.CardInputSlot;
import net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot.ExperienceBottleOutputSlot;
import net.alfonsormadrid.enchanttransfer.screens.infusioncoil.slot.GlassBottleSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.stream.IntStream;

/**
 * Screen handler for the Infusion Coil. Wires the block entity's persistent
 * inventory (card-in, glass-in, xp-out) to GUI slots at the coordinates
 * defined in {@link InfusionCoilSlotPositions}.
 */
public class InfusionCoilScreenHandler extends AbstractContainerMenu {

    private static final int GUI_SLOTS_END    = 3;
    private static final int PLAYER_INV_START = 3;
    private static final int HOTBAR_END       = 39;

    private final Container inventory;
    private final ContainerData propertyDelegate;
    /** Position of the Infusion Coil in the world — synced via ExtendedScreenHandlerType. */
    private final BlockPos coilPos;

    /** Client-side constructor: called by ExtendedScreenHandlerType with the synced coil pos. */
    public InfusionCoilScreenHandler(int syncId, Inventory playerInventory, BlockPos coilPos) {
        this(syncId, playerInventory,
                new SimpleContainer(3),
                new net.minecraft.world.inventory.SimpleContainerData(InfusionCoilBlockEntity.PROPERTY_COUNT),
                coilPos);
    }

    /** Fallback client-side constructor when position is not available. */
    public InfusionCoilScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, BlockPos.ZERO);
    }

    /** Server-side constructor: bound to the live block entity inventory and its PropertyDelegate. */
    public InfusionCoilScreenHandler(int syncId, Inventory playerInventory, Container inventory, ContainerData propertyDelegate) {
        this(syncId, playerInventory, inventory, propertyDelegate, BlockPos.ZERO);
    }

    /** Master constructor — all other constructors delegate here. */
    private InfusionCoilScreenHandler(int syncId, Inventory playerInventory,
                                      Container inventory, ContainerData propertyDelegate,
                                      BlockPos coilPos) {
        super(EnchantTransferMod.INFUSION_COIL_SCREEN_HANDLER, syncId);
        checkContainerSize(inventory, 3);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        this.coilPos = coilPos;
        inventory.startOpen(playerInventory.player);

        buildModuleSlots();
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addDataSlots(propertyDelegate);
    }

    /** Returns the world position of the Infusion Coil block (available client-side). */
    public BlockPos getCoilPos() {
        return coilPos;
    }

    // ── Property accessors (read by the screen) ──────────────────────────────

    public int getProgress() {
        return propertyDelegate.get(InfusionCoilBlockEntity.PROPERTY_PROGRESS);
    }

    public int getMaxProgress() {
        return InfusionCoilBlockEntity.TICKS_PER_INFUSION;
    }

    public int getStoredXp() {
        return propertyDelegate.get(InfusionCoilBlockEntity.PROPERTY_STORED_XP);
    }

    public int getTankCapacity() {
        return InfusionCoilBlockEntity.TANK_CAPACITY;
    }

    private void buildModuleSlots() {
        addSlot(new CardInputSlot(inventory, InfusionCoilBlockEntity.SLOT_CARD_IN, InfusionCoilSlotPositions.cardIn));
        addSlot(new GlassBottleSlot(inventory, InfusionCoilBlockEntity.SLOT_BOTTLE_IN, InfusionCoilSlotPositions.glassIn));
        addSlot(new ExperienceBottleOutputSlot(inventory, InfusionCoilBlockEntity.SLOT_BOTTLE_OUT, InfusionCoilSlotPositions.xpOut));
    }

    private void addPlayerInventory(Inventory playerInventory) {
        int x = InfusionCoilGuiMetrics.playerInventory.positionX;
        int y = InfusionCoilGuiMetrics.playerInventory.positionY;
        IntStream.range(0, 3).forEach(row ->
                IntStream.range(0, 9).forEach(col ->
                        addSlot(new Slot(playerInventory, col + row * 9 + 9, x + col * 18, y + row * 18))));
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        int x = InfusionCoilGuiMetrics.playerHotbar.positionX;
        int y = InfusionCoilGuiMetrics.playerHotbar.positionY;
        IntStream.range(0, 9).forEach(col ->
                addSlot(new Slot(playerInventory, col, x + col * 18, y)));
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
            // From the coil to the player
            if (!moveItemStackTo(original, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // From the player to the coil — pick the right slot based on the item.
            // Any MagicCardItem (base or coloured) routes to the card slot;
            // glass bottles go to the bottle slot.
            if (original.getItem() instanceof net.alfonsormadrid.enchanttransfer.item.MagicCardItem) {
                if (!moveItemStackTo(original, InfusionCoilBlockEntity.SLOT_CARD_IN, InfusionCoilBlockEntity.SLOT_CARD_IN + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (original.is(net.minecraft.world.item.Items.GLASS_BOTTLE)) {
                if (!moveItemStackTo(original, InfusionCoilBlockEntity.SLOT_BOTTLE_IN, InfusionCoilBlockEntity.SLOT_BOTTLE_IN + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }
}
