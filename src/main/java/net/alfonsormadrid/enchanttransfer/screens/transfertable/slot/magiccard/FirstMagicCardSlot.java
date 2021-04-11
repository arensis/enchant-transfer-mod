package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.CombineCardSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferTableInventorySlots;
import net.alfonsormadrid.enchanttransfer.services.CombineCardService;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class FirstMagicCardSlot extends MagicCardSlot {

    protected CombineCardService combineCardService;

    public FirstMagicCardSlot(Inventory inventory) {
        super(inventory, TransferTableInventorySlots.FIRST_MAGIC_CARD_SLOT.getSlotIndex(), CombineCardSlotPositions.topCard);
        this.combineCardService = new CombineCardService(
                inventory.getStack(TransferTableInventorySlots.FIRST_MAGIC_CARD_SLOT.getSlotIndex()),
                inventory.getStack(TransferTableInventorySlots.SECOND_MAGIC_CARD_SLOT.getSlotIndex())
        );
    }

//    public FirstMagicCardSlot(Inventory inventory, Inventory outputInventory) {
//        super(inventory, outputInventory, 0, CombineCardSlotPositions.topCard);
//        this.combineCardService = new CombineCardService(
//                inventory.getStack(currentSlot()),
//                inventory.getStack(otherCardSlot())
//        );
//    }

//    @Override
//    protected Integer currentSlot() {
//        return firstMagicCardSlot;
//    }
//
//    @Override
//    protected Integer otherCardSlot() {
//        return secondMagicCardSlot;
//    }

    @Override
    protected ItemStack combineCards() {
        return this.combineCardService.combineCards();
    }

    @Override
    protected boolean cardsCanCombine() {
        return this.combineCardService.cardsCanCombine();
    }

}
