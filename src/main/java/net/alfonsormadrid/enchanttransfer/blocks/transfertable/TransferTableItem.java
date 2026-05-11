package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

public class TransferTableItem extends BlockItem {
    public TransferTableItem(TransferTableBlock transferTableBlock, RegistryKey<Item> registryKey) {
        super(
            transferTableBlock,
            new Item.Settings().registryKey(registryKey).fireproof()
        );
    }
}
