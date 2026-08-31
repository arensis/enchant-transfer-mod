package net.alfonsormadrid.enchanttransfer.blocks.infusioncoil;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class InfusionCoilItem extends BlockItem {
    public InfusionCoilItem(InfusionCoilBlock block, ResourceKey<Item> registryKey) {
        super(
            block,
            new Item.Properties().setId(registryKey).fireResistant()
        );
    }
}
