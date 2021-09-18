package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.CombineCardSlotPositions;
import net.alfonsormadrid.enchanttransfer.gui.transfertable.TransferItemSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.MagicCardSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.MagicCardResultSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferItemSlot;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferItemContentSlot;
import net.alfonsormadrid.enchanttransfer.services.CombineCardService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;
import java.util.stream.IntStream;

public class TransferTableScreenHandler extends ScreenHandler {
    private final Inventory combineCardsInput;
    private final Inventory combineCardsOutput;
    private final Inventory transferItem;
    private final Inventory transferItemContent;
    private final ScreenHandlerContext context;
    private final CombineCardService combineCardService;

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, syncId);
        this.combineCardsInput = buildInitInventory(2);
        this.combineCardsOutput = buildInitInventory(1);
        this.transferItem = buildInitInventory(1);
        this.transferItemContent = buildInitInventory(12);
        this.context = context;

        this.combineCardService = new CombineCardService(
                this.combineCardsInput.getStack(0),
                this.combineCardsInput.getStack(1)
        );

        this.builtCombineCardSlots();
        this.addSlot(new TransferItemSlot(this.transferItem, this.transferItemContent, TransferItemSlotPositions.transferItem));
        this.buildTransferItemContentSlots();

        addSlotGrid(9, 3, 8, 84, playerInventory, 9);
        addSlotGrid(9, 1, 8, 142, playerInventory, 0);
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.combineCardsInput.canPlayerUse(player);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);

        if (inventory == this.combineCardsInput) {
            updateCombineCardsOutput();
        }
    }

    @Override
    public void close(PlayerEntity player) {
        super.close(player);
        this.dropInventory(player, player.world, this.combineCardsInput);
        this.dropInventory(player, player.world, this.transferItem);
    }

    private void updateCombineCardsOutput() {
        combineCardService.setCard1(this.combineCardsInput.getStack(0));
        combineCardService.setCard2(this.combineCardsInput.getStack(1));

        if (this.combineCardService.cardsCanCombine()) {
            this.combineCardsOutput.setStack(0, this.combineCardService.combineCards());
        } else {
            this.combineCardsOutput.setStack(0, ItemStack.EMPTY);
        }

        this.sendContentUpdates();
    }

    private SimpleInventory buildInitInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty(){
                super.markDirty();
                TransferTableScreenHandler.this.onContentChanged(this);
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
        IntStream.range(0, this.transferItemContent.size())
                .forEach(index ->
                        this.addSlot(
                                new TransferItemContentSlot(
                                        this.transferItemContent,
                                        this.transferItem,
                                        index,
                                        TransferItemSlotPositions.transferItemContentPositions.get(index)
                                )));
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
