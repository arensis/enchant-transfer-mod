package net.alfonsormadrid.enchanttransfer.renderers;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlock;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlockEntity;
import net.alfonsormadrid.enchanttransfer.renderers.state.ZincSmelterRenderState;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/**
 * BER for the Zinc Smelter — renders ONLY the animated fire glow behind
 * the mirilla when lit.  The chimney, door details, and all static
 * geometry live in the baked block model so they participate in normal
 * world depth sorting (fixing the glass-on-top issue).
 */
public class ZincSmelterRenderer
        implements BlockEntityRenderer<ZincSmelterBlockEntity, ZincSmelterRenderState> {

    private static final Identifier WHITE_TEXTURE =
            Identifier.of(EnchantTransferMod.MOD_ID, "textures/misc/white.png");

    // Fire glow quad — on the BACK WALL of the door cavity (z=15.6).
    // The player looks through glass → dark empty cavity → this fire
    // on the rear wall, creating a real sense of depth.
    private static final float FIRE_X0 = 6.5f/16f, FIRE_X1 = 9.5f/16f;
    private static final float FIRE_Y0 = 4.8f/16f, FIRE_Y1 = 7.2f/16f;
    private static final float FIRE_Z  = 15.6f/16f;

    private static final float FIRE_R = 1.00f, FIRE_G = 0.45f, FIRE_B = 0.08f;
    private static final int FULL_LIGHT = 0xF000F0;

    public ZincSmelterRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public ZincSmelterRenderState createRenderState() {
        return new ZincSmelterRenderState();
    }

    @Override
    public void updateRenderState(ZincSmelterBlockEntity entity,
                                  ZincSmelterRenderState state,
                                  float tickDelta,
                                  Vec3d cameraPos,
                                  ModelCommandRenderer.CrumblingOverlayCommand crumbling) {
        BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);
        var blockState = entity.getCachedState();
        state.facing = blockState.get(ZincSmelterBlock.FACING);
        state.lit = blockState.get(ZincSmelterBlock.LIT);
        long worldTime = entity.getWorld() != null ? entity.getWorld().getTime() : 0L;
        state.animTime = (worldTime + tickDelta) * 0.15f;
    }

    @Override
    public void render(ZincSmelterRenderState state,
                       MatrixStack matrices,
                       OrderedRenderCommandQueue queue,
                       CameraRenderState cameraState) {

        if (!state.lit) return; // Nothing dynamic to render when idle

        RenderLayer glowLayer = RenderLayers.entityTranslucentEmissive(WHITE_TEXTURE);

        // Rotate to match block facing
        matrices.push();
        matrices.translate(0.5f, 0f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(facingToYaw(state.facing)));
        matrices.translate(-0.5f, 0f, -0.5f);

        // Pulsing fire glow on the mirilla — higher alpha for visibility
        float pulse = 0.6f + 0.4f * (float) Math.sin(state.animTime);
        float alpha = 0.65f + 0.30f * pulse;
        final float a = alpha;
        queue.submitCustom(matrices, glowLayer, (entry, vc) -> {
            quad(entry, vc,
                    FIRE_X0, FIRE_Y0, FIRE_Z,
                    FIRE_X1, FIRE_Y0, FIRE_Z,
                    FIRE_X1, FIRE_Y1, FIRE_Z,
                    FIRE_X0, FIRE_Y1, FIRE_Z,
                    FIRE_R, FIRE_G, FIRE_B, a,
                    0, 0, 1);
        });

        matrices.pop();
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

    private static void quad(MatrixStack.Entry entry, VertexConsumer vc,
                             float ax, float ay, float az,
                             float bx, float by, float bz,
                             float cx, float cy, float cz,
                             float dx, float dy, float dz,
                             float r, float g, float b, float a,
                             float nx, float ny, float nz) {
        vc.vertex(entry, ax,ay,az).color(r,g,b,a).texture(0f,0f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx,ny,nz);
        vc.vertex(entry, bx,by,bz).color(r,g,b,a).texture(1f,0f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx,ny,nz);
        vc.vertex(entry, cx,cy,cz).color(r,g,b,a).texture(1f,1f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx,ny,nz);
        vc.vertex(entry, dx,dy,dz).color(r,g,b,a).texture(0f,1f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx,ny,nz);
    }
}
