package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static net.alfonsormadrid.enchanttransfer.EnchantTransferMod.TRANSFER_TABLE_BLOCK_IDENTIFIER;


public class TransferTableBlock extends BlockWithEntity {

    public static final MapCodec<TransferTableBlock> CODEC = createCodec(TransferTableBlock::new);

    public TransferTableBlock() {
        this(
            FabricBlockSettings.copyOf(Blocks.CHEST)
                .sounds(
                    new BlockSoundGroup(
                        5.0F,
                        5.0F,
                        SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT,
                        SoundEvents.BLOCK_ANVIL_STEP,
                        SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER,
                        SoundEvents.BLOCK_CHAIN_HIT,
                        SoundEvents.BLOCK_SLIME_BLOCK_FALL)
                )
                .requiresTool()
                .strength(5.0f, 30.0f)
                .luminance(10)
        );
    }

    private TransferTableBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TransferTableBlockEntity(pos, state);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            NamedScreenHandlerFactory namedScreenHandlerFactory = this.createScreenHandlerFactory(state, world, pos);
            if (namedScreenHandlerFactory != null) {
                player.openHandledScreen(namedScreenHandlerFactory);
                player.incrementStat(this.getOpenStat());
            }

            return ActionResult.CONSUME;
        }
    }

    protected Stat<Identifier> getOpenStat() {
        return Stats.CUSTOM.getOrCreateStat(Stats.OPEN_CHEST);
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        return ScreenHandler.calculateComparatorOutput(world.getBlockEntity(pos));
    }
}
