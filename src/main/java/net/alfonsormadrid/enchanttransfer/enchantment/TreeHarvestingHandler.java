package net.alfonsormadrid.enchanttransfer.enchantment;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Harvesting on an axe, when the player breaks a log of a natural tree (logs touching
 * leaves that weren't placed by a player):
 * <ul>
 *   <li>I: replants the tree's sapling where a base log (one on soil) was, taking it
 *       from the inventory;</li>
 *   <li>II: also fells the logs straight above the broken one;</li>
 *   <li>III: fells the whole tree, branches included (up to {@link #MAX_LOGS}), for
 *       {@link HarvestingGameRules#TREE_XP_COST} experience levels; without them, like II;</li>
 *   <li>IV: grows the replanted sapling with bone meal from the inventory.</li>
 * </ul>
 * Felling goes top to bottom and costs one durability per log; sneaking turns it off.
 * Works for other mods' trees too: logs in {@code #minecraft:logs}, leaves in
 * {@code #minecraft:leaves} (or {@code #enchanttransfer:harvesting_foliage}), and the
 * sapling from {@link SaplingRules}.
 */
final class TreeHarvestingHandler {
    static final int MAX_LOGS = 256;
    private static final int COLUMN_LEVEL = 2;
    private static final int WHOLE_TREE_LEVEL = 3;

    /** Blocks that, besides non-persistent leaves, mark a tree as natural (Nether wart blocks). */
    static final TagKey<Block> NATURAL_FOLIAGE = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "harvesting_foliage"));

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
            double treeLevels = HarvestingGameRules.get(level, HarvestingGameRules.TREE_XP_COST);
            boolean wholeTree = harvesting >= WHOLE_TREE_LEVEL && ExperienceCost.canAfford(player, treeLevels);
            if (wholeTree) {
                ExperienceCost.charge(player, treeLevels);
            }
            List<BlockPos> toFell = wholeTree ? new ArrayList<BlockPos>(tree) : columnAbove(level, pos);
            toFell.sort(Comparator.comparingInt((BlockPos log) -> log.getY()).reversed());
            fell(level, player, axe, toFell, felled);
        }

        Block sapling = SaplingRules.saplingFor(state);
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

    /** A tree, not a build: some log touches leaves that grew there (or other natural foliage). */
    private static boolean touchesNaturalLeaves(ServerLevel level, Set<BlockPos> logs) {
        for (BlockPos log : logs) {
            for (BlockPos near : BlockPos.betweenClosed(log.offset(-1, -1, -1), log.offset(1, 1, 1))) {
                BlockState state = level.getBlockState(near);
                if (state.is(NATURAL_FOLIAGE)
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
