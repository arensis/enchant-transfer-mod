package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import com.mojang.serialization.MapCodec;
import net.alfonsormadrid.enchanttransfer.network.OpenSelectorPayload;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.level.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class TransferTableBlock extends BaseEntityBlock {

    public static final MapCodec<TransferTableBlock> CODEC = simpleCodec(TransferTableBlock::new);

    public TransferTableBlock(ResourceKey<Block> registryKey) {
        this(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CHEST)
                .setId(registryKey)
                .sound(
                    new SoundType(
                        5.0F,
                        5.0F,
                        SoundEvents.LIGHTNING_BOLT_IMPACT,
                        SoundEvents.ANVIL_STEP,
                        SoundEvents.LIGHTNING_BOLT_THUNDER,
                        SoundEvents.CHAIN_HIT,
                        SoundEvents.SLIME_BLOCK_FALL)
                )
                .requiresCorrectToolForDrops()
                .strength(5.0f, 30.0f)
                .lightLevel(state -> 10)
        );
    }

    private TransferTableBlock(BlockBehaviour.Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (world instanceof ServerLevel serverWorld) {
            Vec3 center = Vec3.atCenterOf(pos);
            serverWorld.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    center.x, center.y + 0.5, center.z, 40, 0.45, 0.45, 0.45, 0.25);
            serverWorld.sendParticles(ParticleTypes.SCULK_SOUL,
                    center.x, center.y + 0.5, center.z, 20, 0.35, 0.35, 0.35, 0.08);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean moved) {
        Vec3 center = Vec3.atCenterOf(pos);
        world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                center.x, center.y + 0.5, center.z, 25, 0.4, 0.4, 0.4, 0.3);
        world.sendParticles(ParticleTypes.REVERSE_PORTAL,
                center.x, center.y + 0.5, center.z, 50, 0.5, 0.5, 0.5, 0.6);
        super.affectNeighborsAfterRemoval(state, world, pos, moved);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TransferTableBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Right-click sends an S2C packet that opens the Selector screen on the
     * client. From there the player can navigate to the core (Transfer Table)
     * or any attached module.
     */
    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos,
                              Player player, BlockHitResult hit) {
        if (!(world instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new OpenSelectorPayload(pos));
        }
        return InteractionResult.CONSUME;
    }

}
