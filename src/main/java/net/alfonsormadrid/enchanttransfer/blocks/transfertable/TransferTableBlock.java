package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.alfonsormadrid.enchanttransfer.EnchantTransferMod.TRANSFER_TABLE_BLOCK_IDENTIFIER;


public class TransferTableBlock extends BlockWithEntity {

    public static final MapCodec<TransferTableBlock> CODEC = createCodec(TransferTableBlock::new);

    public TransferTableBlock() {
        this(
            AbstractBlock.Settings.copy(Blocks.CHEST)
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
                .luminance(state -> 10)
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
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            ServerWorld serverWorld = (ServerWorld) world;
            Vec3d center = Vec3d.ofCenter(pos);
            serverWorld.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
                    center.x, center.y + 0.5, center.z, 40, 0.45, 0.45, 0.45, 0.25);
            serverWorld.spawnParticles(ParticleTypes.END_ROD,
                    center.x, center.y + 0.5, center.z, 20, 0.35, 0.35, 0.35, 0.08);
        }
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
