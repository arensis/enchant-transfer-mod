package net.alfonsormadrid.enchanttransfer.blocks.zincsmelter;

import com.mojang.serialization.MapCodec;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

/**
 * Standalone steampunk smelter for zinc processing and brass alloying.
 * <p>
 * Oriented by the player on placement (like a furnace). Two blockstate
 * properties: {@code facing} (horizontal) and {@code lit} (active fire).
 * The {@code lit} variant swaps the baked model so the lava glow texture
 * shows inside the mirilla and bumps luminance.
 */
public class ZincSmelterBlock extends BlockWithEntity {

    public static final MapCodec<ZincSmelterBlock> CODEC = createCodec(ZincSmelterBlock::new);

    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = Properties.LIT;

    public ZincSmelterBlock(RegistryKey<Block> registryKey) {
        this(
            AbstractBlock.Settings.copy(Blocks.BLAST_FURNACE)
                .registryKey(registryKey)
                .sounds(BlockSoundGroup.COPPER_BULB)
                .requiresTool()
                .strength(7.0f, 7.0f)
                .luminance(state -> state.get(LIT) ? 13 : 0)
                .nonOpaque()
        );
    }

    private ZincSmelterBlock(AbstractBlock.Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(LIT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, LIT);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ZincSmelterBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!(world instanceof ServerWorld)) return null;
        return validateTicker(type, EnchantTransferMod.ZINC_SMELTER_BLOCK_ENTITY,
                ZincSmelterBlockEntity::serverTick);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        var factory = state.createScreenHandlerFactory(world, pos);
        if (factory != null) {
            player.openHandledScreen(factory);
        }
        return ActionResult.CONSUME;
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!state.isOf(this) || moved) {
            super.onStateReplaced(state, world, pos, moved);
            return;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Inventory inventory) {
            ItemScatterer.spawn(world, pos, inventory);
            world.updateComparators(pos, this);
        }
        super.onStateReplaced(state, world, pos, moved);
    }

    // Smoke + lava particles from the chimney are emitted by the BER or
    // a future client tick — randomDisplayTick is not used to avoid API
    // compatibility issues across MC versions.
}
