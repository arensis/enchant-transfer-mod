package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.services.CombineCardService;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.slot.Slot;

public class TransferTableScreenHandler extends ScreenHandler {
    private final Inventory combineCardsInput;
    private final Inventory combineCardsOutput;
    private final Inventory transferInput;
    private final Inventory transferItemContent;
    private final ScreenHandlerContext context;
    private final CombineCardService combineCardService;

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, syncId);
        this.combineCardsInput = buildInitInventory(2);
        this.combineCardsOutput = new CraftingResultInventory();
        this.transferInput = buildInitInventory(1);
        this.transferItemContent = buildInitInventory(12);
        this.context = context;

        this.combineCardService = new CombineCardService(
                this.combineCardsInput.getStack(0),
                this.combineCardsInput.getStack(1)
        );

        this.builtCombineCardSlots();
        this.builtTransferSlots();
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, EnchantTransferMod.TRANSFER_TABLE_BLOCK);
    }

    @Override
    public void onContentChanged(Inventory inventory) {
        super.onContentChanged(inventory);
        if (inventory == this.combineCardsInput) {
            updateCombineCardsOutput();
        }

        if (inventory == this.transferInput) {
            //logic for transfer item
        }
    }

    private void updateCombineCardsOutput() {
        if (this.combineCardService.cardsCanCombine()) {
            this.combineCardsOutput.setStack(0, this.combineCardService.combineCards());
        } else {
            this.combineCardsOutput.setStack(0, ItemStack.EMPTY);
        }
    }

    private void builtCombineCardSlots() {
        this.addSlot(buildMagicCardSlot(0, 20, 30));
        this.addSlot(buildMagicCardSlot(1, 20, 40));
        this.addSlot(new Slot(this.combineCardsOutput, 0, 50, 40) {
            public boolean canInsert(ItemStack stack) {
                return false;
            }
            //Logic for onTakeItem method
        });
    }

    private Slot buildMagicCardSlot(int index, int x, int y) {
        return new Slot(this.combineCardsInput, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
            }
        };
    }

    private void builtTransferSlots() {
        this.addSlot(new Slot(this.transferInput, 0, 50, 30) {
            public boolean canInsert(ItemStack stack) {
                return stack.isEnchantable();
            }
        });
        //Add slots for contain of transfer input
    }

    private SimpleInventory buildInitInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty(){
                super.markDirty();
                TransferTableScreenHandler.this.onContentChanged(this);
            }
        };
    }
}
