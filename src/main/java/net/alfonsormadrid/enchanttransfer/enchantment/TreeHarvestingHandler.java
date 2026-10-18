package net.alfonsormadrid.enchanttransfer.enchantment;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Harvesting on an axe, when the player breaks a log of a natural tree (logs touching
 * leaves that weren't placed by a player):
 * <ul>
 *   <li>I: replants the tree's sapling where a base log (one on soil) was, taking it
 *       from the inventory;</li>
 *   <li>II: also fells the logs straight above the broken one;</li>
 *   <li>III: fells the whole tree, branches included (up to {@link #MAX_LOGS});</li>
 *   <li>IV: grows the replanted sapling with bone meal from the inventory.</li>
 * </ul>
 * Felling goes top to bottom and costs one durability per log; sneaking turns it off.
 */
final class TreeHarvestingHandler {
    static final int MAX_LOGS = 256;
    private static final int COLUMN_LEVEL = 2;
    private static final int WHOLE_TREE_LEVEL = 3;

    private static final Map<Block, Block> SAPLINGS = Map.ofEntries(
            Map.entry(Blocks.OAK_LOG, Blocks.OAK_SAPLING),
            Map.entry(Blocks.SPRUCE_LOG, Blocks.SPRUCE_SAPLING),
            Map.entry(Blocks.BIRCH_LOG, Blocks.BIRCH_SAPLING),
            Map.entry(Blocks.JUNGLE_LOG, Blocks.JUNGLE_SAPLING),
            Map.entry(Blocks.ACACIA_LOG, Blocks.ACACIA_SAPLING),
            Map.entry(Blocks.DARK_OAK_LOG, Blocks.DARK_OAK_SAPLING),
            Map.entry(Blocks.MANGROVE_LOG, Blocks.MANGROVE_PROPAGULE),
            Map.entry(Blocks.CHERRY_LOG, Blocks.CHERRY_SAPLING),
            Map.entry(Blocks.PALE_OAK_LOG, Blocks.PALE_OAK_SAPLING),
            Map.entry(Blocks.CRIMSON_STEM, Blocks.CRIMSON_FUNGUS),
            Map.entry(Blocks.WARPED_STEM, Blocks.WARPED_FUNGUS));

    private TreeHarvestingHandler() {}

    static void register() {
        PlayerBlockBreakEvents.AFTER.register(TreeHarvestingHandler::afterBreak);
    }

    private static void afterBreak(Level world, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (!(world instanceof ServerLevel level) || !state.is(BlockTags.LOGS)) {
            return;
        }
        ItemStack axe = player.getMainHandItem();
        int harvesting = axe.is(ItemTags.AXES) ? HarvestingSupport.harvestingLevel(player, axe) : 0;
        if (harvesting <= 0) {
            return;
        }
        Set<BlockPos> tree = connectedLogs(level, pos);
        if (!touchesNaturalLeaves(level, tree)) {
            return;
        }

        List<BlockPos> felled = new ArrayList<>();
        felled.add(pos);
        if (harvesting >= COLUMN_LEVEL && !player.isShiftKeyDown()) {
            List<BlockPos> toFell = harvesting >= WHOLE_TREE_LEVEL
                    ? new ArrayList<BlockPos>(tree)
                    : columnAbove(level, pos);
            toFell.sort(Comparator.comparingInt((BlockPos log) -> log.getY()).reversed());
            fell(level, player, axe, toFell, felled);
        }

        Block sapling = SAPLINGS.get(state.getBlock());
        if (sapling != null) {
            replant(level, player, sapling, felled, harvesting >= HarvestingSupport.BONE_MEAL_LEVEL);
        }
    }

    /** Logs connected to the broken one (any of the 26 neighbours, so diagonal branches count). */
    private static Set<BlockPos> connectedLogs(ServerLevel level, BlockPos start) {
        Set<BlockPos> logs = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && logs.size() < MAX_LOGS) {
            BlockPos current = queue.poll();
            for (BlockPos next : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                if (logs.size() < MAX_LOGS && !logs.contains(next) && level.getBlockState(next).is(BlockTags.LOGS)) {
                    BlockPos found = next.immutable();
                    logs.add(found);
                    queue.add(found);
                }
            }
        }
        return logs;
    }

    /** A tree, not a build: some log touches leaves that grew there (or Nether wart blocks). */
    private static boolean touchesNaturalLeaves(ServerLevel level, Set<BlockPos> logs) {
        for (BlockPos log : logs) {
            for (BlockPos near : BlockPos.betweenClosed(log.offset(-1, -1, -1), log.offset(1, 1, 1))) {
                BlockState state = level.getBlockState(near);
                if (state.is(BlockTags.WART_BLOCKS)
                        || state.is(BlockTags.LEAVES) && state.hasProperty(LeavesBlock.PERSISTENT)
                                && !state.getValue(LeavesBlock.PERSISTENT)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<BlockPos> columnAbove(ServerLevel level, BlockPos pos) {
        List<BlockPos> column = new ArrayList<>();
        for (BlockPos up = pos.above(); column.size() < MAX_LOGS && level.getBlockState(up).is(BlockTags.LOGS); up = up.above()) {
            column.add(up);
        }
        return column;
    }

    private static void fell(ServerLevel level, Player player, ItemStack axe, List<BlockPos> logs, List<BlockPos> felled) {
        for (BlockPos log : logs) {
            if (axe.isEmpty()) {
                return;
            }
            BlockState state = level.getBlockState(log);
            if (!state.is(BlockTags.LOGS)) {
                continue;
            }
            Block.dropResources(state, level, log, level.getBlockEntity(log), player, axe);
            level.destroyBlock(log, false, player);
            felled.add(log);
            axe.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
        }
    }

    /** Plants a sapling on each felled spot that was on soil; IV then grows the first one. */
    private static void replant(ServerLevel level, Player player, Block sapling, List<BlockPos> felled, boolean boneMeal) {
        BlockPos first = null;
        for (BlockPos spot : felled) {
            BlockState plant = sapling.defaultBlockState();
            if (level.getBlockState(spot).isAir() && plant.canSurvive(level, spot)
                    && HarvestingSupport.consume(player, sapling.asItem())) {
                level.setBlock(spot, plant, Block.UPDATE_ALL);
                if (first == null) {
                    first = spot;
                }
            }
        }
        if (boneMeal && first != null) {
            HarvestingSupport.growWithBoneMeal(level, first, player);
        }
    }
}
