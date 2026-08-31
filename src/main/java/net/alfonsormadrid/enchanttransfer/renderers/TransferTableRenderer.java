package net.alfonsormadrid.enchanttransfer.renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.renderers.state.TransferTableRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Block entity renderer for the Transfer Table.
 *
 * <p>Two responsibilities:
 * <ol>
 *   <li>The pulsing emissive cube on the diamond core
 *       ({@code [5,5,5]→[11,11,11]} in model space).</li>
 *   <li>One copper {@link #drawTube tube} per connected Infusion Coil
 *       neighbour, going from the corresponding block face inward toward
 *       the core.  Each tube uses the same brida-pipe-brida pattern as
 *       the coil's matching tube, so when both blocks render the two
 *       halves meet at the block boundary and read as a single continuous
 *       industrial run.</li>
 * </ol>
 */
public class TransferTableRenderer
        implements BlockEntityRenderer<TransferTableBlockEntity, TransferTableRenderState> {

    // ── Core cube ────────────────────────────────────────────────────────────
    private static final float CORE_MIN = 5f  / 16f;
    private static final float CORE_MAX = 11f / 16f;
    // Core glow colour — boosted from #46AFEB so it reads as a bright
    // cyan even after world-lighting attenuation in debugFilledBox.
    private static final float CR = 0.45f, CG = 0.85f, CB = 1.0f;

    // ── Tube geometry ────────────────────────────────────────────────────────
    // Tube length from the block face to the core boundary = 5 px.  Same
    // brida-pipe-brida pattern as the coil so the visual reads as one
    // continuous tube spanning both blocks.
    private static final float TUBE_PIPE_MIN     = 6.5f  / 16f;  // 3-px cross
    private static final float TUBE_PIPE_MAX     = 9.5f  / 16f;
    private static final float TUBE_FLANGE_MIN   = 5.5f  / 16f;  // 5-px cross
    private static final float TUBE_FLANGE_MAX   = 10.5f / 16f;
    // 4.85 (not 5) so the far brida sits 0.15/16 away from the core face,
    // never sharing the z=5/16 (or y=5/16, etc.) plane.  The previous
    // 0.05/16 gap was getting overpowered by the polygon offset of the
    // pulse — when the coil ends up on a vertical neighbour the user
    // reported the cyan glow bleeding through the brida again.  0.15/16
    // is still sub-pixel at default texture-pack scale (invisible visually)
    // but well over any reasonable polygon-offset magnitude.
    private static final float TUBE_LENGTH       = 4.5f / 16f;
    private static final float TUBE_FLANGE_DEPTH = 0.8f  / 16f;

    // Same two-tone copper as the coil tube
    private static final float TUBE_PIPE_R   = 0.62f, TUBE_PIPE_G   = 0.36f, TUBE_PIPE_B   = 0.12f;
    private static final float TUBE_FLANGE_R = 0.92f, TUBE_FLANGE_G = 0.60f, TUBE_FLANGE_B = 0.22f;

    // Mod-owned 1×1 white texture (also used by InfusionCoilRenderer) — keeps
    // the tube on the entityTranslucentEmissive layer so the two halves at the
    // block boundary are lit identically.
    private static final Identifier WHITE_TEXTURE =
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "textures/misc/white.png");
    private static final int FULL_LIGHT = 0xF000F0;

    public TransferTableRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public TransferTableRenderState createRenderState() {
        return new TransferTableRenderState();
    }

    @Override
    public void extractRenderState(TransferTableBlockEntity entity,
                                  TransferTableRenderState state,
                                  float tickDelta,
                                  Vec3 cameraPos,
                                  ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderer.super.extractRenderState(entity, state, tickDelta, cameraPos, crumbling);
        long worldTime = entity.getLevel() != null ? entity.getLevel().getGameTime() : 0L;
        state.animTime = (worldTime + tickDelta) * 0.05f;

        // Scan the 6 neighbours.  We can't trust the server-only
        // ModuleConnectionRegistry on the client, so we check block states
        // directly — same pattern the coil renderer uses to find its table.
        if (entity.getLevel() != null) {
            BlockPos pos = entity.getBlockPos();
            for (Direction dir : Direction.values()) {
                state.connectedFaces[dir.ordinal()] =
                        entity.getLevel().getBlockState(pos.relative(dir))
                                .is(EnchantTransferMod.INFUSION_COIL_BLOCK);
            }
        } else {
            for (int i = 0; i < state.connectedFaces.length; i++) {
                state.connectedFaces[i] = false;
            }
        }
    }

    @Override
    public void submit(TransferTableRenderState state,
                       PoseStack matrices,
                       SubmitNodeCollector queue,
                       CameraRenderState cameraState) {
        float alpha = 0.55f + 0.45f * (0.5f + 0.5f * (float) Math.sin(state.animTime));

        // 1 — Pulsing core overlay.  debugFilledBox (POSITION_COLOR) avoids
        //     the polygon-offset and translucent-sorting artefacts that
        //     entityTranslucentEmissive causes on this in-block geometry.
        //     Brighter RGB values compensate for world-lighting attenuation.
        queue.submitCustomGeometry(matrices, RenderTypes.debugFilledBox(), (entry, vc) ->
                colorBox(entry, vc,
                        CORE_MIN, CORE_MIN, CORE_MIN,
                        CORE_MAX, CORE_MAX, CORE_MAX,
                        CR, CG, CB, alpha));

        // 2 — Full brida-pipe-brida tube for every connected face, vertical
        //     included.  On the vertical case the table-side near brida sits
        //     right at the boundary, visually docking with the coil's base
        //     (when the coil is above) or its cap (when the coil is below).
        //     The coil renderer doesn't add a tube for vertical because its
        //     centre column is occupied by the flask geometry — the docking
        //     brida is enough to read the connection cleanly.
        // entityCutout writes depth — prevents translucent-sorting glitches
        // where back faces of the tube show through the core glow at
        // oblique angles.
        RenderType tubeLayer = RenderTypes.entityCutout(WHITE_TEXTURE);
        for (Direction dir : Direction.values()) {
            if (!state.connectedFaces[dir.ordinal()]) continue;
            drawTube(matrices, queue, tubeLayer, dir);
        }
    }

    /**
     * Emits the three segments of one tube (near flange → pipe → far flange)
     * going from the {@code dir} face of the table inward toward the core.
     */
    private void drawTube(PoseStack matrices, SubmitNodeCollector queue,
                          RenderType layer, Direction dir) {
        final float[] flangeNear =
                tubeSegment(dir, TUBE_FLANGE_MIN, TUBE_FLANGE_MAX, 0f, TUBE_FLANGE_DEPTH);
        final float[] pipe =
                tubeSegment(dir, TUBE_PIPE_MIN, TUBE_PIPE_MAX,
                            TUBE_FLANGE_DEPTH, TUBE_LENGTH - TUBE_FLANGE_DEPTH);
        final float[] flangeFar =
                tubeSegment(dir, TUBE_FLANGE_MIN, TUBE_FLANGE_MAX,
                            TUBE_LENGTH - TUBE_FLANGE_DEPTH, TUBE_LENGTH);

        queue.submitCustomGeometry(matrices, layer, (entry, vc) -> {
            drawEntityBox(entry, vc,
                    flangeNear[0], flangeNear[1], flangeNear[2],
                    flangeNear[3], flangeNear[4], flangeNear[5],
                    TUBE_FLANGE_R, TUBE_FLANGE_G, TUBE_FLANGE_B, 1.0f);
            drawEntityBox(entry, vc,
                    pipe[0], pipe[1], pipe[2],
                    pipe[3], pipe[4], pipe[5],
                    TUBE_PIPE_R, TUBE_PIPE_G, TUBE_PIPE_B, 1.0f);
            drawEntityBox(entry, vc,
                    flangeFar[0], flangeFar[1], flangeFar[2],
                    flangeFar[3], flangeFar[4], flangeFar[5],
                    TUBE_FLANGE_R, TUBE_FLANGE_G, TUBE_FLANGE_B, 1.0f);
        });
    }

    // ── Geometry helpers ──────────────────────────────────────────────────────

    /**
     * Bounding box of one tube segment along the axis pointing from the
     * given face toward the centre of the block.  Mirror of the helper in
     * the coil renderer.
     */
    private static float[] tubeSegment(Direction dir, float cmin, float cmax,
                                        float axisStart, float axisEnd) {
        return switch (dir) {
            case NORTH -> new float[]{ cmin, cmin, axisStart,     cmax, cmax, axisEnd      };
            case SOUTH -> new float[]{ cmin, cmin, 1f - axisEnd,  cmax, cmax, 1f - axisStart };
            case WEST  -> new float[]{ axisStart,     cmin, cmin, axisEnd,      cmax, cmax };
            case EAST  -> new float[]{ 1f - axisEnd,  cmin, cmin, 1f - axisStart, cmax, cmax };
            case DOWN  -> new float[]{ cmin, axisStart,     cmin, cmax, axisEnd,      cmax };
            case UP    -> new float[]{ cmin, 1f - axisEnd,  cmin, cmax, 1f - axisStart, cmax };
        };
    }

    // ── POSITION_COLOR box (debugFilledBox) ─────────────────────────────────

    /** Solid-colour box, 6 quads, POSITION_COLOR vertex format. */
    private static void colorBox(PoseStack.Pose entry, VertexConsumer vc,
                                  float x0, float y0, float z0,
                                  float x1, float y1, float z1,
                                  float r, float g, float b, float a) {
        cq(entry, vc, x0,y0,z0,  x1,y0,z0,  x1,y0,z1,  x0,y0,z1,  r,g,b,a); // -Y
        cq(entry, vc, x0,y1,z1,  x1,y1,z1,  x1,y1,z0,  x0,y1,z0,  r,g,b,a); // +Y
        cq(entry, vc, x0,y1,z0,  x1,y1,z0,  x1,y0,z0,  x0,y0,z0,  r,g,b,a); // -Z
        cq(entry, vc, x1,y1,z1,  x0,y1,z1,  x0,y0,z1,  x1,y0,z1,  r,g,b,a); // +Z
        cq(entry, vc, x0,y1,z1,  x0,y1,z0,  x0,y0,z0,  x0,y0,z1,  r,g,b,a); // -X
        cq(entry, vc, x1,y1,z0,  x1,y1,z1,  x1,y0,z1,  x1,y0,z0,  r,g,b,a); // +X
    }

    private static void cq(PoseStack.Pose entry, VertexConsumer vc,
                            float ax, float ay, float az,
                            float bx, float by, float bz,
                            float cx, float cy, float cz,
                            float dx, float dy, float dz,
                            float r, float g, float b, float a) {
        vc.addVertex(entry, ax, ay, az).setColor(r, g, b, a);
        vc.addVertex(entry, bx, by, bz).setColor(r, g, b, a);
        vc.addVertex(entry, cx, cy, cz).setColor(r, g, b, a);
        vc.addVertex(entry, dx, dy, dz).setColor(r, g, b, a);
    }

    // ── ENTITY-format box (entityTranslucentEmissive) ───────────────────────

    /**
     * Same 6-quad box but emitting the ENTITY vertex format expected by
     * {@link RenderTypes#entityTranslucentEmissive}.  Pos + colour +
     * tex(0,0) + overlay + full-bright light + normal — matches the helper
     * used by the coil renderer so the boundary between table tube and
     * coil tube is invisible.
     */
    private static void drawEntityBox(PoseStack.Pose entry, VertexConsumer vc,
                                       float x0, float y0, float z0,
                                       float x1, float y1, float z1,
                                       float r, float g, float b, float a) {
        // Winding matches colorBox (outward-facing for entityCutout culling).
        // Directional shading so the box reads as 3D.
        float sY0 = 0.35f, sY1 = 1.00f, sZ = 0.70f, sX = 0.50f;
        eq(entry, vc, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, r*sY0,g*sY0,b*sY0,a,  0f,-1f, 0f); // -Y
        eq(entry, vc, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, r*sY1,g*sY1,b*sY1,a,  0f, 1f, 0f); // +Y
        eq(entry, vc, x0,y1,z0, x1,y1,z0, x1,y0,z0, x0,y0,z0, r*sZ, g*sZ, b*sZ, a,  0f, 0f,-1f); // -Z
        eq(entry, vc, x1,y1,z1, x0,y1,z1, x0,y0,z1, x1,y0,z1, r*sZ, g*sZ, b*sZ, a,  0f, 0f, 1f); // +Z
        eq(entry, vc, x0,y1,z1, x0,y1,z0, x0,y0,z0, x0,y0,z1, r*sX, g*sX, b*sX, a, -1f, 0f, 0f); // -X
        eq(entry, vc, x1,y1,z0, x1,y1,z1, x1,y0,z1, x1,y0,z0, r*sX, g*sX, b*sX, a,  1f, 0f, 0f); // +X
    }

    private static void eq(PoseStack.Pose entry, VertexConsumer vc,
                            float ax, float ay, float az,
                            float bx, float by, float bz,
                            float cx, float cy, float cz,
                            float dx, float dy, float dz,
                            float r, float g, float b, float a,
                            float nx, float ny, float nz) {
        vc.addVertex(entry, ax, ay, az).setColor(r,g,b,a).setUv(0f,0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx, ny, nz);
        vc.addVertex(entry, bx, by, bz).setColor(r,g,b,a).setUv(1f,0f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx, ny, nz);
        vc.addVertex(entry, cx, cy, cz).setColor(r,g,b,a).setUv(1f,1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx, ny, nz);
        vc.addVertex(entry, dx, dy, dz).setColor(r,g,b,a).setUv(0f,1f).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_LIGHT).setNormal(entry, nx, ny, nz);
    }
}
