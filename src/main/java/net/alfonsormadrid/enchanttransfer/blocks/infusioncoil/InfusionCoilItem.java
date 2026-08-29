package net.alfonsormadrid.enchanttransfer.blocks.infusioncoil;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;

public class InfusionCoilItem extends BlockItem {
    public InfusionCoilItem(InfusionCoilBlock block, RegistryKey<Item> registryKey) {
        super(
            block,
            new Item.Settings().registryKey(registryKey).fireproof()
        );
    }
}
