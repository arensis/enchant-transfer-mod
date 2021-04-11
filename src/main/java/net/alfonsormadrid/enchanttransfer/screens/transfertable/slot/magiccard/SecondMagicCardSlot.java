package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.magiccard;

import net.alfonsormadrid.enchanttransfer.gui.transfertable.CombineCardSlotPositions;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.slot.TransferTableInventorySlots;
import net.alfonsormadrid.enchanttransfer.services.CombineCardService;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public class SecondMagicCardSlot extends MagicCardSlot {

    CombineCardService combineCardService;

    public SecondMagicCardSlot(Inventory inventory) {
        super(inventory, TransferTableInventorySlots.SECOND_MAGIC_CARD_SLOT.getSlotIndex(), CombineCardSlotPositions.bottomCard);
        this.combineCardService = new CombineCardService(
                inventory.getStack(TransferTableInventorySlots.SECOND_MAGIC_CARD_SLOT.getSlotIndex()),
                inventory.getStack(TransferTableInventorySlots.FIRST_MAGIC_CARD_SLOT.getSlotIndex())
        );
    }

//    public SecondMagicCardSlot(Inventory inventory, Inventory outputInventory) {
//        super(inventory, outputInventory, 1, CombineCardSlotPositions.bottomCard);
//        this.combineCardService = new CombineCardService(
//                inventory.getStack(currentSlot()),
//                inventory.getStack(otherCardSlot())
//        );
//    }

//    @Override
//    protected Integer currentSlot() {
//        return SECOND_MAGIC_CARD_SLOT;
//    }
//
//    @Override
//    protected Integer otherCardSlot() {
//        return FIRST_MAGIC_CARD_SLOT;
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
