package net.alfonsormadrid.enchanttransfer.blocks.infusioncoil;

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
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class InfusionCoilBlock extends BlockWithEntity {

    public static final MapCodec<InfusionCoilBlock> CODEC = createCodec(InfusionCoilBlock::new);

    public InfusionCoilBlock(RegistryKey<Block> registryKey) {
        this(
            AbstractBlock.Settings.copy(Blocks.LODESTONE)
                .registryKey(registryKey)
                .sounds(BlockSoundGroup.AMETHYST_BLOCK)
                .requiresTool()
                .strength(4.0f, 25.0f)
                .luminance(state -> 6)
        );
    }

    private InfusionCoilBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new InfusionCoilBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (!(world instanceof ServerWorld)) return null;
        return validateTicker(type, EnchantTransferMod.INFUSION_COIL_BLOCK_ENTITY, InfusionCoilBlockEntity::serverTick);
    }

    /**
     * Drop the coil's inventory contents when the block is broken or replaced.
     * Module detachment from the core is handled by the block entity itself in
     * {@link InfusionCoilBlockEntity#markRemoved()}.
     */
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
}
