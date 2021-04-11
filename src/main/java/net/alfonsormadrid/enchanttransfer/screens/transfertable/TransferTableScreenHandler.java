package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.TransferItemSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferTableInventorySlots;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard.FirstMagicCardSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard.MagicCardResultSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard.SecondMagicCardSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.transferitem.TransferItemSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.transferitem.TransferItemContentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.stream.IntStream;

public class TransferTableScreenHandler extends ScreenHandler {
    private final Inventory transferTableInventory;

//    private final Inventory combineCardsInput;
//    private final Inventory combineCardsOutput;
//    private final Inventory transferItem;
//    private final Inventory transferItemContent;

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, syncId);
        this.transferTableInventory = buildInitInventory(16);

//        this.combineCardsInput = buildInitInventory(2);
//        this.combineCardsOutput = buildInitInventory(1);
//        this.transferItem = buildInitInventory(1);
//        this.transferItemContent = buildInitInventory(12);

        this.buildMagicCardSlots();
        this.buildTransferSlots();
        this.buildPlayerInventorySlots(playerInventory);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.transferTableInventory.canPlayerUse(player);
//        return this.combineCardsInput.canPlayerUse(player) &&
//                this.combineCardsOutput.canPlayerUse(player) &&
//                this.transferItem.canPlayerUse(player) &&
//                this.transferItemContent.canPlayerUse(player);
    }

    private SimpleInventory buildInitInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty(){
                super.markDirty();
                TransferTableScreenHandler.this.onContentChanged(this);
            }
        };
    }

    private void buildMagicCardSlots() {
        this.addSlot(new FirstMagicCardSlot(this.transferTableInventory));
        this.addSlot(new SecondMagicCardSlot(this.transferTableInventory));
        this.addSlot(new MagicCardResultSlot(this.transferTableInventory));
//        this.addSlot(new FirstMagicCardSlot(this.combineCardsInput, this.combineCardsOutput));
//        this.addSlot(new SecondMagicCardSlot(this.combineCardsInput, this.combineCardsOutput));
//        this.addSlot(new MagicCardResultSlot(this.combineCardsOutput, this.combineCardsInput));
    }

    private void buildTransferSlots() {
        this.addSlot(new TransferItemSlot(this.transferTableInventory, TransferItemSlotPositions.transferItem));
//        this.addSlot(new TransferItemSlot(this.transferItem, this.transferItemContent, TransferItemSlotPositions.transferItem));

        IntStream.range(TransferTableInventorySlots.ITEM_CONTENT_FIRST_SLOT.getSlotIndex(), TransferTableInventorySlots.ITEM_CONTENT_LAST_SLOT.getSlotIndex())
                .forEach(index -> {
                        System.out.println("Total inventory size: " + this.transferTableInventory.size());
                        System.out.println("Index: " + index) ;
                        this.addSlot(
                                new TransferItemContentSlot(
                                        this.transferTableInventory,
                                        index,
                                        TransferTableInventorySlots.ITEM_SLOT.getSlotIndex(),
                                        TransferItemSlotPositions.transferItemContentPositions.get(index - TransferTableInventorySlots.ITEM_CONTENT_FIRST_SLOT.getSlotIndex())
                                )
                        );
                });

//        IntStream.range(0, this.transferItemContent.size())
//                .forEach(index ->
//                        this.addSlot(
//                                new TransferItemContentSlot(
//                                        this.transferItemContent,
//                                        this.transferItem,
//                                        index,
//                                        TransferItemSlotPositions.transferItemContentPositions.get(index)
//                                )));
    }

    private void buildPlayerInventorySlots(PlayerInventory playerInventory) {
        addSlotGrid(9, 3, 8, 84, playerInventory, 9);
        addSlotGrid(9, 1, 8, 142, playerInventory, 0);
    }

    public void addSlotGrid(int columnsAmount, int rowsAmount, int startPositionX, int startPositionY, Inventory inventory, int startInventoryIndex) {
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
