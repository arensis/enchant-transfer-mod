package net.alfonsormadrid.enchanttransfer.enchantment;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

/**
 * Harvesting: right-clicking a ripe crop with the enchanted hoe harvests it (normal
 * loot, Fortune included) and replants it, taking one seed from the inventory and one
 * point of durability, like tilling. Without that seed in the inventory nothing happens.
 * Which blocks count is the {@code enchanttransfer:harvesting_replantable} block tag;
 * ripe means its {@code age} property is at its maximum.
 */
final class HarvestingHandler {
    static final TagKey<Block> REPLANTABLE = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "harvesting_replantable"));

    private HarvestingHandler() {}

    static void register() {
        UseBlockCallback.EVENT.register(HarvestingHandler::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        if (player.isSpectator()) {
            return InteractionResult.PASS;
        }
        ItemStack hoe = player.getItemInHand(hand);
        boolean enchanted = ModEnchantments.holder(level, ModEnchantments.HARVESTING)
                .map(harvesting -> EnchantmentHelper.getItemEnchantmentLevel(harvesting, hoe) > 0)
                .orElse(false);
        if (!enchanted) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState crop = level.getBlockState(pos);
        IntegerProperty age = ripeAge(crop);
        Item seed = crop.getBlock().asItem();
        if (age == null || seed == Items.AIR) {
            return InteractionResult.PASS;
        }
        int seedSlot = findSeed(player.getInventory(), seed);
        if (seedSlot < 0) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            Block.dropResources(crop, level, pos, level.getBlockEntity(pos), player, hoe);
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(crop));
            level.setBlock(pos, crop.setValue(age, minAge(age)), Block.UPDATE_ALL);
            level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
            if (!player.hasInfiniteMaterials()) {
                player.getInventory().getItem(seedSlot).shrink(1);
            }
            hoe.hurtAndBreak(1, player, hand);
        }
        return InteractionResult.SUCCESS;
    }

    /** The crop's {@code age} property when it is replantable and fully grown, else null. */
    private static @Nullable IntegerProperty ripeAge(BlockState state) {
        if (!state.is(REPLANTABLE)) {
            return null;
        }
        for (var property : state.getProperties()) {
            if (property instanceof IntegerProperty age && age.getName().equals("age")) {
                return state.getValue(age).equals(Collections.max(age.getPossibleValues())) ? age : null;
            }
        }
        return null;
    }

    private static int minAge(IntegerProperty age) {
        return Collections.min(age.getPossibleValues());
    }

    private static int findSeed(Inventory inventory, Item seed) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(seed)) {
                return slot;
            }
        }
        return -1;
    }
}
