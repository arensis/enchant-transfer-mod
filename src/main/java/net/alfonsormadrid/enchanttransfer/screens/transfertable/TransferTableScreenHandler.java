package net.alfonsormadrid.enchanttransfer.screens.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
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
    private final Inventory combineCardsOuputResult;
    private final Inventory transferInput;
    private final Inventory transferItemContent;
    private final ScreenHandlerContext context;

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public TransferTableScreenHandler(int syncId, PlayerInventory playerInventory, final ScreenHandlerContext context) {
        super(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, syncId);
        this.combineCardsInput = buildInitInventory(2);
        this.transferInput = buildInitInventory(1);
        this.combineCardsOuputResult = new CraftingResultInventory();
        this.transferItemContent = new TransferTableBlockEntity();
        this.context = context;
        this.addSlot(buildMagicCardSlot(0, 20, 30));
        this.addSlot(buildMagicCardSlot(1, 20, 40));
        this.addSlot(new Slot(this.combineCardsOuputResult, 0, 50, 40) {
            //Logic for the combine card result
        });
        this.addSlot(new Slot(this.transferInput, 0, 50, 30) {
            public boolean canInsert(ItemStack stack) {
                return stack.isEnchantable();
            }
        });
        //Add slots for contain of transfer input
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(this.context, player, EnchantTransferMod.TRANSFER_TABLE_BLOCK);
    }

    public Slot buildMagicCardSlot(int index, int x, int y) {
        return new Slot(this.combineCardsInput, index, x, y) {
            public boolean canInsert(ItemStack stack) {
                return stack.getItem() == EnchantTransferMod.MAGIC_CARD_ITEM;
            }
        };
    }

    public SimpleInventory buildInitInventory(int size) {
        return new SimpleInventory(size) {
            public void markDirty(){
                super.markDirty();
                TransferTableScreenHandler.this.onContentChanged(this);
            }
        };
    }
}
