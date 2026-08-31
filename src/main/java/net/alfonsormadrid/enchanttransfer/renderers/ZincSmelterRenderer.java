package net.alfonsormadrid.enchanttransfer.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlock;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlockEntity;
import net.alfonsormadrid.enchanttransfer.renderers.state.ZincSmelterRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * BER for the Zinc Smelter — renders ONLY the animated fire glow behind
 * the mirilla when lit.  The chimney, door details, and all static
 * geometry live in the baked block model so they participate in normal
 * world depth sorting (fixing the glass-on-top issue).
 */
public class ZincSmelterRenderer
        implements BlockEntityRenderer<ZincSmelterBlockEntity, ZincSmelterRenderState> {

    private static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "textures/misc/white.png");

    // Fire glow quad — on the BACK WALL of the door cavity (z=15.6).
    // The player looks through glass → dark empty cavity → this fire
    // on the rear wall, creating a real sense of depth.
    private static final float FIRE_X0 = 6.5f/16f, FIRE_X1 = 9.5f/16f;
    private static final float FIRE_Y0 = 4.8f/16f, FIRE_Y1 = 7.2f/16f;
    private static final float FIRE_Z  = 15.6f/16f;

    private static final float FIRE_R = 1.00f, FIRE_G = 0.45f, FIRE_B = 0.08f;
    private static final int FULL_LIGHT = 0xF000F0;

    public ZincSmelterRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public ZincSmelterRenderState createRenderState() {
        return new ZincSmelterRenderState();
    }

    @Override
    public void extractRenderState(ZincSmelterBlockEntity entity,
                                  ZincSmelterRenderState state,
                                  float tickDelta,
                                  Vec3 cameraPos,
                                  ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickDelta, cameraPos, crumbling);
        var blockState = entity.getBlockState();
        state.facing = blockState.getValue(ZincSmelterBlock.FACING);
        state.lit = blockState.getValue(ZincSmelterBlock.LIT);
        long worldTime = entity.getLevel() != null ? entity.getLevel().getGameTime() : 0L;
        state.animTime = (worldTime + tickDelta) * 0.15f;
    }

    @Override
    public void submit(ZincSmelterRenderState state,
                       PoseStack matrices,
                       SubmitNodeCollector queue,
                       CameraRenderState cameraState) {

        if (!state.lit) return; // Nothing dynamic to render when idle

        RenderType glowLayer = RenderTypes.entityTranslucentEmissive(WHITE_TEXTURE);

        // Rotate to match block facing
        matrices.pushPose();
        matrices.translate(0.5f, 0f, 0.5f);
        matrices.mulPose(Axis.YP.rotationDegrees(facingToYaw(state.facing)));
        matrices.translate(-0.5f, 0f, -0.5f);

        // Pulsing fire glow on the mirilla — higher alpha for visibility
        float pulse = 0.6f + 0.4f * (float) Math.sin(state.animTime);
        float alpha = 0.65f + 0.30f * pulse;
        final float a = alpha;
        queue.submitCustomGeometry(matrices, glowLayer, (entry, vc) -> {
            quad(entry, vc,
                    FIRE_X0, FIRE_Y0, FIRE_Z,
                    FIRE_X1, FIRE_Y0, FIRE_Z,
                    FIRE_X1, FIRE_Y1, FIRE_Z,
                    FIRE_X0, FIRE_Y1, FIRE_Z,
                    FIRE_R, FIRE_G, FIRE_B, a,
                    0, 0, 1);
        });

        matrices.popPose();
    }

    /** Maps facing to the same Y rotation the blockstate JSON uses. */
    private static float facingToYaw(Direction dir) {
        return switch (dir) {
            case NORTH -> 0f;
            case EAST  -> 90f;
            case SOUTH -> 180f;
            case WEST  -> 270f;
            default    -> 0f;
        };
    }

    private static void quad(PoseStack.Pose entry, VertexConsumer vc,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz,
                             float r, float g, float b, float a,
                             float nx, float ny, float nz) {
        vc.addVertex(entry, ax,ay,az).setColor(r,g,b,a).setUv(0f,0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx,ny,nz);
        vc.addVertex(entry, bx,by,bz).setColor(r,g,b,a).setUv(1f,0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx,ny,nz);
        vc.addVertex(entry, cx,cy,cz).setColor(r,g,b,a).setUv(1f,1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx,ny,nz);
        vc.addVertex(entry, dx,dy,dz).setColor(r,g,b,a).setUv(0f,1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx,ny,nz);
    }
}
