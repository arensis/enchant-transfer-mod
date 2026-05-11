package net.alfonsormadrid.enchanttransfer.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class MagicCardItem extends Item {
    public MagicCardItem(RegistryKey<Item> registryKey) {
        super(new Item.Settings().registryKey(registryKey).fireproof());
    }

    @Override
    public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
        playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        return ActionResult.SUCCESS;
    }
}
