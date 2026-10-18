package net.alfonsormadrid.enchanttransfer.enchantment;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Harvesting on a hoe: right-clicking a ripe crop harvests it (normal loot, Fortune
 * included) and replants it, taking one seed from the inventory and one point of
 * durability per crop, like tilling. Without that seed in the inventory nothing happens.
 * II also takes the next two crops ahead, III the whole connected field (for
 * experience), and IV grows every replanted crop with bone meal from the inventory.
 * Which blocks count is the {@code enchanttransfer:harvesting_replantable} block tag;
 * ripe means its {@code age} property is at its maximum.
 */
final class HarvestingHandler {
    static final TagKey<Block> REPLANTABLE = TagKey.create(Registries.BLOCK,
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "harvesting_replantable"));
    private static final int ROW_LEVEL = 2;
    private static final int ROW_LENGTH = 3;
    private static final int FIELD_LEVEL = 3;
    /** Crops in a 9×9: the field is paid per started block of this many. */
    static final int FIELD_CHUNK = 81;
    /** Experience levels per started 9×9; doubled at IV when bone meal is in the inventory. */
    static final double FIELD_CHUNK_LEVELS = 0.5;
    private static final int MAX_FIELD = 4096;

    private HarvestingHandler() {}

    static void register() {
        UseBlockCallback.EVENT.register(HarvestingHandler::onUseBlock);
    }

    private static InteractionResult onUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hit) {
        ItemStack hoe = player.getItemInHand(hand);
        if (player.isSpectator() || !hoe.is(ItemTags.HOES)) {
            return InteractionResult.PASS;
        }
        int harvesting = HarvestingSupport.harvestingLevel(player, hoe);
        if (harvesting <= 0) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState crop = level.getBlockState(pos);
        Item seed = crop.getBlock().asItem();
        if (ripeAge(crop) == null || HarvestingSupport.findSlot(player.getInventory(), seed) < 0) {
            return InteractionResult.PASS;
        }

        if (level instanceof ServerLevel server) {
            boolean boneMeal = harvesting >= HarvestingSupport.BONE_MEAL_LEVEL;
            Plan plan = plan(server, player, pos, crop, seed, harvesting, boneMeal);
            int harvested = 0;
            for (BlockPos target : plan.crops()) {
                if (hoe.isEmpty() || !harvestAndReplant(server, player, target, seed)) {
                    break;
                }
                harvested++;
                hoe.hurtAndBreak(1, player, hand);
                if (boneMeal) {
                    HarvestingSupport.growWithBoneMeal(server, target, player);
                }
            }
            // Paid for what was actually harvested, in case the hoe broke halfway.
            ExperienceCost.charge(player, Math.ceil(harvested / (double) FIELD_CHUNK) * plan.levelsPerChunk());
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * The ripe crops to harvest, the clicked one first: just it (I, or sneaking), a row of
     * three ahead (II) or the whole connected field (III+), never more than there are
     * seeds for. The field costs experience per started 9×9 ({@link #FIELD_CHUNK} crops),
     * charged after harvesting; it is cut to what the player can pay, or falls back to
     * the row if not even one.
     */
    private static Plan plan(ServerLevel level, Player player, BlockPos pos, BlockState crop,
                             Item seed, int harvesting, boolean boneMeal) {
        if (harvesting < ROW_LEVEL || player.isShiftKeyDown()) {
            return new Plan(List.of(pos), 0);
        }
        int seeds = player.hasInfiniteMaterials() ? Integer.MAX_VALUE : countItem(player, seed);
        if (harvesting >= FIELD_LEVEL) {
            double levelsPerChunk = boneMeal && HarvestingSupport.findSlot(player.getInventory(), Items.BONE_MEAL) >= 0
                    ? FIELD_CHUNK_LEVELS * 2 : FIELD_CHUNK_LEVELS;
            int affordable = ExperienceCost.affordableUnits(player, levelsPerChunk);
            if (affordable > 0) {
                List<BlockPos> field = ripeField(level, pos, crop.getBlock());
                int count = (int) Math.min(field.size(), Math.min(seeds, (long) affordable * FIELD_CHUNK));
                return new Plan(field.subList(0, count), levelsPerChunk);
            }
        }
        List<BlockPos> row = new ArrayList<>();
        Direction ahead = player.getDirection();
        for (int i = 0; i < ROW_LENGTH && row.size() < seeds; i++) {
            BlockPos next = pos.relative(ahead, i);
            if (level.getBlockState(next).is(crop.getBlock()) && ripeAge(level.getBlockState(next)) != null) {
                row.add(next);
            }
        }
        return new Plan(row, 0);
    }

    /** Crops to harvest, in order, and the experience levels each started 9×9 of them costs. */
    private record Plan(List<BlockPos> crops, double levelsPerChunk) {}

    /**
     * Ripe crops of the same kind connected to {@code start} on its layer (unripe ones are
     * walked through but not harvested), nearest first. Bounded by {@link #MAX_FIELD} only
     * to keep a runaway search cheap; the experience cost is the real limit.
     */
    private static List<BlockPos> ripeField(ServerLevel level, BlockPos start, Block kind) {
        List<BlockPos> ripe = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>(List.of(start));
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(List.of(start));
        while (!queue.isEmpty() && seen.size() < MAX_FIELD) {
            BlockPos current = queue.poll();
            if (ripeAge(level.getBlockState(current)) != null) {
                ripe.add(current);
            }
            for (Direction side : Direction.Plane.HORIZONTAL) {
                BlockPos next = current.relative(side);
                if (level.getBlockState(next).is(kind) && seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        return ripe;
    }

    private static boolean harvestAndReplant(ServerLevel level, Player player, BlockPos pos, Item seed) {
        BlockState crop = level.getBlockState(pos);
        IntegerProperty age = ripeAge(crop);
        if (age == null || !HarvestingSupport.consume(player, seed)) {
            return false;
        }
        Block.dropResources(crop, level, pos, level.getBlockEntity(pos), player, player.getMainHandItem());
        level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(crop));
        level.setBlock(pos, crop.setValue(age, minAge(age)), Block.UPDATE_ALL);
        level.gameEvent(player, GameEvent.BLOCK_CHANGE, pos);
        return true;
    }

    private static int countItem(Player player, Item item) {
        int count = 0;
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
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
}
