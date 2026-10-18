package net.alfonsormadrid.enchanttransfer.enchantment;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;

/** Inventory and bone meal helpers shared by the hoe and axe sides of Harvesting. */
final class HarvestingSupport {
    /** Harvesting level from which the replanted crop or sapling is grown with bone meal. */
    static final int BONE_MEAL_LEVEL = 4;
    /** Safety cap on bone meal per replant; vanilla needs 2-4 for a sapling, 1-3 for a crop. */
    private static final int MAX_BONE_MEAL = 16;

    private HarvestingSupport() {}

    static int harvestingLevel(Player player, ItemStack tool) {
        return ModEnchantments.level(player.level(), ModEnchantments.HARVESTING, tool);
    }

    /** First inventory slot holding the item, or -1. */
    static int findSlot(Inventory inventory, Item item) {
        if (item == Items.AIR) {
            return -1;
        }
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                return slot;
            }
        }
        return -1;
    }

    /** Takes one of the item from the inventory (free in creative); false if there is none. */
    static boolean consume(Player player, Item item) {
        int slot = findSlot(player.getInventory(), item);
        if (slot < 0) {
            return false;
        }
        if (!player.hasInfiniteMaterials()) {
            player.getInventory().getItem(slot).shrink(1);
        }
        return true;
    }

    /**
     * Applies bone meal from the inventory, one at a time with vanilla's odds, until the
     * plant at {@code pos} can't take more (fully grown, or grown into a tree). Stops
     * without wasting more when a successful application changes nothing (no room or
     * light for the tree).
     */
    static void growWithBoneMeal(ServerLevel level, BlockPos pos, Player player) {
        for (int used = 0; used < MAX_BONE_MEAL; used++) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof BonemealableBlock plant)
                    || !plant.isValidBonemealTarget(level, pos, state)
                    || !consume(player, Items.BONE_MEAL)) {
                return;
            }
            level.levelEvent(LevelEvent.PARTICLES_AND_SOUND_PLANT_GROWTH, pos, 15);
            if (plant.isBonemealSuccess(level, level.getRandom(), pos, state)) {
                plant.performBonemeal(level, level.getRandom(), pos, state);
                if (level.getBlockState(pos) == state) {
                    return;
                }
            }
        }
    }
}
