package net.alfonsormadrid.enchanttransfer.blocks.zincsmelter;

import com.mojang.serialization.MapCodec;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Standalone steampunk smelter for zinc processing and brass alloying.
 * <p>
 * Oriented by the player on placement (like a furnace). Two blockstate
 * properties: {@code facing} (horizontal) and {@code lit} (active fire).
 * The {@code lit} variant swaps the baked model so the lava glow texture
 * shows inside the mirilla and bumps luminance.
 */
public class ZincSmelterBlock extends BaseEntityBlock {

    public static final MapCodec<ZincSmelterBlock> CODEC = simpleCodec(ZincSmelterBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public ZincSmelterBlock(ResourceKey<Block> registryKey) {
        this(
            BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE)
                .setId(registryKey)
                .sound(SoundType.COPPER_BULB)
                .requiresCorrectToolForDrops()
                .strength(7.0f, 7.0f)
                .lightLevel(state -> state.getValue(LIT) ? 13 : 0)
                .noOcclusion()
        );
    }

    private ZincSmelterBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, LIT);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ZincSmelterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (!(world instanceof ServerLevel)) return null;
        return createTickerHelper(type, EnchantTransferMod.ZINC_SMELTER_BLOCK_ENTITY,
                ZincSmelterBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos,
                                 Player player, BlockHitResult hit) {
        if (!(world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        var factory = state.getMenuProvider(world, pos);
        if (factory != null) {
            player.openMenu(factory);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        if (!state.is(this) || moved) {
            super.affectNeighborsAfterRemoval(state, world, pos, moved);
            return;
        }
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof Container inventory) {
            Containers.dropContents(world, pos, inventory);
            world.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    // Smoke + lava particles from the chimney are emitted by the BER or
    // a future client tick — randomDisplayTick is not used to avoid API
    // compatibility issues across MC versions.
}
