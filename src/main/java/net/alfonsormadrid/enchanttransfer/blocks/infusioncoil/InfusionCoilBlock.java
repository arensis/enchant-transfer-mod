package net.alfonsormadrid.enchanttransfer.blocks.infusioncoil;

import com.mojang.serialization.MapCodec;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class InfusionCoilBlock extends BaseEntityBlock {

    public static final MapCodec<InfusionCoilBlock> CODEC = simpleCodec(InfusionCoilBlock::new);

    /**
     * True while the coil is actively processing a card.
     * Drives the swap between the idle model (brass knob) and the active model
     * (sea-lantern knob) and bumps the block's luminance so the knob looks lit.
     */
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public InfusionCoilBlock(ResourceKey<Block> registryKey) {
        this(
            BlockBehaviour.Properties.ofFullCopy(Blocks.LODESTONE)
                .setId(registryKey)
                .sound(SoundType.AMETHYST)
                .requiresCorrectToolForDrops()
                .strength(4.0f, 25.0f)
                // Idle: dim ambient glow (matches the molten-XP body).
                // Active: full-bright so the knob convincingly reads as "lit".
                .lightLevel(state -> state.getValue(ACTIVE) ? 15 : 6)
                // Mark non-opaque so Minecraft does NOT cull the top face of the block
                // below us.  Without this, the inset model geometry leaves black holes
                // in the corners where there is no cuboid to cover the missing face.
                .noOcclusion()
        );
    }

    private InfusionCoilBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionCoilBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        if (!(world instanceof ServerLevel)) return null;
        return createTickerHelper(type, EnchantTransferMod.INFUSION_COIL_BLOCK_ENTITY, InfusionCoilBlockEntity::serverTick);
    }

    /**
     * Drop the coil's inventory contents when the block is broken or replaced.
     * Module detachment from the core is handled by the block entity itself in
     * {@link InfusionCoilBlockEntity#setRemoved()}.
     */
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
}
