package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class TransferTableItem extends BlockItem {
    public TransferTableItem(TransferTableBlock transferTableBlock) {
        super(
            transferTableBlock,
            new Item.Settings()
                .group(EnchantTransferMod.ENCHANT_TRANSFER)
                .fireproof()
        );
    }
}
