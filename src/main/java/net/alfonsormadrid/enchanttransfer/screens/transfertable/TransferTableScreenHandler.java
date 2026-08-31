package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.CombineCardSlotPositions;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.TransferItemSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.MagicCardSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.MagicCardResultSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferItemSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferItemContentSlot;
import net.alfonsormadrid.enchanttransfer.services.CombineCardService;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.*;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import java.util.stream.IntStream;

public class TransferTableScreenHandler extends AbstractContainerMenu {
    private final Container combineCardsInput;
    private final Container combineCardsOutput;
    private final Container transferItem;
    private final Container transferItemContent;
    /** Position of the Transfer Table block — synced from server via ExtendedScreenHandlerType. */
    private final net.minecraft.core.BlockPos tablePos;
    private final CombineCardService combineCardService;

    /** Fallback constructor — used when block-pos is unavailable (e.g. creative menus). */
    public TransferTableScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, BlockPos.ZERO);
    }

    /** Client-side constructor — called by ExtendedScreenHandlerType factory with the synced pos. */
    public TransferTableScreenHandler(int syncId, Inventory playerInventory,
                                      net.minecraft.core.BlockPos tablePos) {
        super(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, syncId);
        this.combineCardsInput = buildInitInventory(2);
        this.combineCardsOutput = buildInitInventory(1);
        this.transferItem = buildInitInventory(1);
        this.transferItemContent = buildInitInventory(12);
        this.tablePos = tablePos;

        this.combineCardService = new CombineCardService(
                this.combineCardsInput.getItem(0),
                this.combineCardsInput.getItem(1)
        );

        this.builtCombineCardSlots();
        this.addSlot(new TransferItemSlot(this.transferItem, this.transferItemContent, TransferItemSlotPositions.transferItem));
        this.buildTransferItemContentSlots();

        addSlotGrid(9, 3, 8, 118, playerInventory, 9);
        addSlotGrid(9, 1, 8, 176, playerInventory, 0);
    }

    // Slot layout:
    //   0-1  : MagicCardSlot (combine input)
    //   2    : MagicCardResultSlot
    //   3    : TransferItemSlot
    //   4-15 : TransferItemContentSlot (12 enchantment card slots)
    //   16-42: Player main inventory
    //   43-51: Player hotbar
    private static final int GUI_SLOTS_END        = 16;
    private static final int PLAYER_INV_START     = 16;
    private static final int PLAYER_INV_END       = 43;
    private static final int HOTBAR_START         = 43;
    private static final int HOTBAR_END           = 52;

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack original = slot.getItem();
        ItemStack copy = original.copy();

        if (slotIndex < GUI_SLOTS_END) {
            if (!this.moveItemStackTo(original, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (original.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM) {
                if (this.slots.get(3).hasItem()) {
                    if (!this.moveItemStackTo(original, 4, GUI_SLOTS_END, false)
                            && !this.moveItemStackTo(original, 0, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(original, 0, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if (!this.moveItemStackTo(original, 3, 4, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (original.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
            slot.onTake(player, copy);
        } else {
            slot.setChanged();
        }

        return copy;
    }

    /** Returns the position of the Transfer Table in the world. */
    public BlockPos getTablePos() {
        return tablePos;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.combineCardsInput.stillValid(player);
    }

    @Override
    public void slotsChanged(Container inventory) {
        super.slotsChanged(inventory);

        if (inventory == this.combineCardsInput) {
            updateCombineCardsOutput();
        }
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.clearContainer(player, this.combineCardsInput);
        this.clearContainer(player, this.transferItem);
    }

    private void updateCombineCardsOutput() {
        combineCardService.setCard1(this.combineCardsInput.getItem(0));
        combineCardService.setCard2(this.combineCardsInput.getItem(1));

        if (this.combineCardService.cardsCanCombine()) {
            this.combineCardsOutput.setItem(0, this.combineCardService.combineCards());
        } else {
            this.combineCardsOutput.setItem(0, ItemStack.EMPTY);
        }

        this.broadcastChanges();
    }

    private SimpleContainer buildInitInventory(int size) {
        return new SimpleContainer(size) {
            public void setChanged(){
                super.setChanged();
                TransferTableScreenHandler.this.slotsChanged(this);
            }
        };
    }

    private void builtCombineCardSlots() {
        this.addSlot(new MagicCardSlot(this.combineCardsInput, 0, CombineCardSlotPositions.topCard));
        this.addSlot(new MagicCardSlot(this.combineCardsInput, 1, CombineCardSlotPositions.bottomCard));
        this.addSlot(
            new MagicCardResultSlot(
                this.combineCardsOutput,
                this.combineCardsInput,
                0,
                CombineCardSlotPositions.resultCard
            )
        );
    }

    private void buildTransferItemContentSlots() {
        IntStream.range(0, this.transferItemContent.getContainerSize())
                .forEach(index ->
                        this.addSlot(
                                new TransferItemContentSlot(
                                        this.transferItemContent,
                                        this.transferItem,
                                        index,
                                        TransferItemSlotPositions.transferItemContentPositions.get(index)
                                )));
    }

    public void addSlotGrid(int columnsAmount, int rowsAmount, int startPositionX, int startPositionY, Container inventory, int startInventoryIndex) {
        IntStream.range(0, rowsAmount)
                .forEach(rowIndex -> IntStream.range(0, columnsAmount)
                        .forEach(columnIndex -> {
                            int slotWidth = 18;
                            int positionX = startPositionX + columnIndex * slotWidth;
                            int positionY = startPositionY + rowIndex * slotWidth;
                            int slotIndex = columnIndex + rowIndex * columnsAmount + startInventoryIndex;

                            this.addSlot(new Slot(inventory, slotIndex, positionX, positionY));
        }));
    }
}
