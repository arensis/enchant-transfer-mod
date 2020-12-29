package net.alfonsormadrid.enchanttransfer.item;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class MagicCardItem extends Item {
    public MagicCardItem() {
        super(new Item.Settings().group(EnchantTransferMod.ENCHANT_TRANSFER).fireproof());
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity playerEntity, Hand hand) {
        playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        return TypedActionResult.success(playerEntity.getOffHandStack());
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        if (stack.getEnchantments().size() < 1) {
            return super.isEnchantable(stack);
        } else {
            return false;
        }
    }
}
