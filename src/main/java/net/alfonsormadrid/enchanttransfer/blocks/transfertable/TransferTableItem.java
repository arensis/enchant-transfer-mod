package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class TransferTableItem extends BlockItem {
    public TransferTableItem(TransferTableBlock transferTableBlock, ResourceKey<Item> registryKey) {
        super(
            transferTableBlock,
            new Item.Properties().setId(registryKey).fireResistant()
        );
    }
}
