package net.alfonsormadrid.enchanttransfer.screens.transfertable.slot;

public enum TransferTableInventorySlots {
    FIRST_MAGIC_CARD_SLOT(0),
    SECOND_MAGIC_CARD_SLOT(1),
    OUTPUT_CARD_SLOT(2),
    ITEM_SLOT(3),
    ITEM_CONTENT_FIRST_SLOT(4),
    ITEM_CONTENT_LAST_SLOT(15);

    private final Integer slotIndex;

    TransferTableInventorySlots(Integer slotIndex) {
        this.slotIndex = slotIndex;
    }

    public Integer getSlotIndex() {
        return this.slotIndex;
    }
}
