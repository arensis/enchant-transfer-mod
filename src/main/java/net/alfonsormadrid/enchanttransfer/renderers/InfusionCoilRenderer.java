package net.alfonsormadrid.enchanttransfer.renderers;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.alfonsormadrid.enchanttransfer.renderers.state.InfusionCoilRenderState;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * BlockEntityRenderer for the Infusion Coil.
 * <p>
 * Renders ONLY the dynamic overlays that the baked block model cannot express:
 * <ul>
 *   <li>The XP fluid level inside the glass body.</li>
 *   <li>The falling-drop animation while processing.</li>
 *   <li>The copper connection tube toward an adjacent Transfer Table.</li>
 * </ul>
 * The knob's "lit lightbulb" effect is NOT drawn here — it's handled by the
 * {@code ACTIVE} blockstate property in {@link
 * net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlock},
 * which swaps the baked model and bumps the block's luminance.  Doing it
 * through the model system avoids the Z-fighting / partial-occlusion problems
 * that an overlay box renders on top of opaque baked geometry suffers from.
 */
public class InfusionCoilRenderer
  implements BlockEntityRenderer<InfusionCoilBlockEntity, InfusionCoilRenderState> {

  // Mod-owned 1×1 fully-opaque white RGBA texture, generated at build time
  // and shipped in resources.  We cannot use minecraft:textures/misc/white.png
  // because that texture no longer exists in 1.21.11 (it was removed) — using
  // it returns the missing-texture sprite, which the shader then tints with
  // the vertex colour, producing a checkerboard-of-vertex-colour-on-black
  // pattern.  Hosting our own guarantees the binding succeeds.
  private static final Identifier WHITE_TEXTURE =
    Identifier.of(EnchantTransferMod.MOD_ID, "textures/misc/white.png");

  // ── Fluid (XP) ───────────────────────────────────────────────────────────
  // Fluid box sits INSIDE the glass cavity (glass body XZ = [3.5..12.5]/16)
  // with a 0.05/16 inset on every side.  The fluid is rendered on
  // entityCutout, which is OPAQUE and DOES write depth.  Because BER cutout
  // runs before the world translucent pass, the depth buffer at the fluid
  // pixels already contains the fluid's depth when the glass renders.
  //
  // The glass at z=12.5 is then CLOSER to the camera than the fluid at
  // z=12.45 → glass passes LEQUAL → glass renders translucent on top of
  // the opaque green fluid → the classic Thaumcraft essence-jar look:
  // a coloured liquid mass visible behind a thin tinted glass shell.
  private static final float FLUID_X_MIN  =  3.55f / 16f;
  private static final float FLUID_Z_MIN  =  3.55f / 16f;
  private static final float FLUID_X_MAX  = 12.45f / 16f;
  private static final float FLUID_Z_MAX  = 12.45f / 16f;
  private static final float FLUID_Y_BASE =  1.55f / 16f;
  private static final float FLUID_Y_MAX  =  7.45f / 16f;
  // Minimum visible fluid column.  Without this, a fillRatio of 0.007
  // (one card = ~7 XP out of 1000 capacity) produces a half-pixel sliver
  // that's effectively invisible.  Clamp the visible bottom-to-top span so
  // any non-zero fill reads as "yes there's something in here".
  private static final float FLUID_MIN_VISIBLE_HEIGHT = 0.6f / 16f;

  // ── Drip animation ───────────────────────────────────────────────────────
  // Half-extents in XZ are 0.75/16 → final bead width is 1.5 px on a default
  // texture pack: thin enough to read as a "drop", thick enough to stay
  // visible while falling through the cavity.
  private static final float DRIP_HALF_WIDTH    = 0.75f / 16f;
  private static final float DRIP_HALF_DEPTH    = 0.75f / 16f;
  private static final float DRIP_HEIGHT        = 2.0f  / 16f;
  private static final int   DRIP_CYCLE_TICKS   = 40;
  private static final int   DRIP_VISIBLE_TICKS = 32;

  // ── Connection tube ──────────────────────────────────────────────────────
  // The tube is built from three stacked segments along the axis pointing at
  // the Transfer Table:
  //
  //   |==FLANGE==|----PIPE----|==FLANGE==|
  //   0       0.8/16        2.7/16    3.5/16  ← measured from the neighbour face
  //
  // Flanges are wider than the pipe (5 px vs 3 px cross-section) so they
  // read as collars where the tube bolts onto each block — turns the old
  // flat box into something that looks like an actual industrial fitting.
  private static final float TUBE_PIPE_MIN     = 6.5f  / 16f;  // 3-px cross-section
  private static final float TUBE_PIPE_MAX     = 9.5f  / 16f;
  private static final float TUBE_FLANGE_MIN   = 5.5f  / 16f;  // 5-px cross-section
  private static final float TUBE_FLANGE_MAX   = 10.5f / 16f;
  private static final float TUBE_LENGTH       = 3.5f  / 16f;
  private static final float TUBE_FLANGE_DEPTH = 0.8f  / 16f;

  // ── Colours ──────────────────────────────────────────────────────────────
  // Brighter, more saturated lime-green for the XP fluid.  The original
  // 168/224/48 amber blended too closely with grass + the wood block under
  // the coil, making the (already small) fluid column read as "tint" rather
  // than a clear liquid.  Pushing R down and G up gives a vivid XP green
  // that pops against any natural background and reads clearly even when
  // the column is short (low fillRatio).
  // Lighter, desaturated XP green — reads as translucent liquid when
  // seen through the glass body, even though it renders opaque.
  private static final float FLUID_R = 120 / 255f, FLUID_G = 230 / 255f,
                              FLUID_B = 100 / 255f, FLUID_A = 1.00f;
  // Two-tone copper for visual depth: bright (polished) flanges + a darker
  // (oxidised) central pipe.
  private static final float TUBE_PIPE_R   = 0.62f, TUBE_PIPE_G   = 0.36f, TUBE_PIPE_B   = 0.12f;
  private static final float TUBE_FLANGE_R = 0.92f, TUBE_FLANGE_G = 0.60f, TUBE_FLANGE_B = 0.22f;
  // Knob pulse skin: bright white that washes over the diamond_block base.
  // Alpha modulates between "barely there" and "almost-white" → the knob
  // appears to breathe brighter/dimmer in time with animTime.
  private static final float PULSE_R = 1.00f, PULSE_G = 1.00f, PULSE_B = 1.00f;

  private static final int FULL_LIGHT = 0xF000F0;

  public InfusionCoilRenderer(BlockEntityRendererFactory.Context ctx) {}

  @Override
  public InfusionCoilRenderState createRenderState() {
    return new InfusionCoilRenderState();
  }

  @Override
  public void updateRenderState(InfusionCoilBlockEntity entity,
                                InfusionCoilRenderState state,
                                float tickDelta,
                                Vec3d cameraPos,
                                ModelCommandRenderer.CrumblingOverlayCommand crumbling) {
    BlockEntityRenderer.super.updateRenderState(entity, state, tickDelta, cameraPos, crumbling);
    state.fillRatio    = entity.getFillRatio();
    state.isProcessing = entity.isProcessing();

    state.dirToTable = null;
    if (entity.getWorld() != null) {
      BlockPos pos = entity.getPos();
      for (Direction dir : Direction.values()) {
        if (entity.getWorld().getBlockState(pos.offset(dir))
          .isOf(EnchantTransferMod.TRANSFER_TABLE_BLOCK)) {
          state.dirToTable = dir;
          break;
        }
      }
    }

    long worldTime = entity.getWorld() != null ? entity.getWorld().getTime() : 0L;
    state.animTime = (worldTime + tickDelta) * 0.08f;

    // Drip plays whenever a card is being processed, regardless of the tank
    // level — visually it represents the XP being extracted from the card
    // and falling toward the tank, so it has to be visible even when the
    // tank is starting from empty.
    if (state.isProcessing) {
      int tickInCycle    = (int)(worldTime % DRIP_CYCLE_TICKS);
      state.showDrip     = tickInCycle < DRIP_VISIBLE_TICKS;
      state.dripProgress = (tickInCycle + tickDelta) / DRIP_VISIBLE_TICKS;
      if (state.dripProgress > 1f) state.dripProgress = 1f;

      // Fluid bump: after the drop hits (ticks DRIP_VISIBLE_TICKS..DRIP_CYCLE_TICKS),
      // the fluid level rises momentarily then settles back.
      if (tickInCycle >= DRIP_VISIBLE_TICKS) {
        int rippleTicks = DRIP_CYCLE_TICKS - DRIP_VISIBLE_TICKS;
        float t = (tickInCycle - DRIP_VISIBLE_TICKS + tickDelta) / rippleTicks;
        state.fluidBump     = Math.max(0f, 1f - t);
        state.rippleProgress = Math.min(1f, t);
      } else {
        state.fluidBump      = 0f;
        state.rippleProgress = 0f;
      }
    } else {
      state.showDrip       = false;
      state.dripProgress   = 0f;
      state.fluidBump      = 0f;
      state.rippleProgress = 0f;
    }
  }

  @Override
  public void render(InfusionCoilRenderState state,
                     MatrixStack matrices,
                     OrderedRenderCommandQueue queue,
                     CameraRenderState cameraState) {

    // Two render layers with WHITE_TEXTURE (1×1 solid white → final colour
    // = vertex colour):
    //
    //   • fluidLayer (entityCutout) — OPAQUE, writes depth.  Used only for
    //     the fluid mass.  This is the key to the Thaumcraft-jar look: the
    //     fluid commits its depth values before the world translucent pass
    //     runs, so the glass renders translucent on top, blending its tint
    //     over the opaque fluid behind it.  Translucent emissive cannot
    //     produce this effect because it doesn't write depth — the glass
    //     just overpaints it and you see "glass tinted slightly green".
    //
    //   • layer (entityTranslucentEmissive) — translucent + always full-
    //     bright.  Used for the drip drop, the knob pulse skin, and the
    //     tube.  These are all overlay effects that should not occlude.
    RenderLayer fluidLayer = RenderLayers.entityCutoutNoCull(WHITE_TEXTURE);
    RenderLayer layer      = RenderLayers.entityTranslucentEmissive(WHITE_TEXTURE);

    // ── 1. FLUID (XP liquid) ─────────────────────────────────────────────
    // Diagnostic mode removed.  Renders only when the tank actually has XP,
    // clamping the visible column height to at least FLUID_MIN_VISIBLE_HEIGHT
    // so a single card's worth of XP (~7/1000) still shows a visible sliver.
    // Diagnostic confirmed the BER cutout pipeline works — the fluid is now
    // back to depending on state.fillRatio, with the min-height clamp so a
    // single card's worth of XP still shows a visible sliver.
    if (state.fillRatio > 0f) {
      float linearTop = FLUID_Y_BASE + state.fillRatio * (FLUID_Y_MAX - FLUID_Y_BASE);
      float fluidTopY = Math.min(FLUID_Y_MAX, Math.max(linearTop, FLUID_Y_BASE + FLUID_MIN_VISIBLE_HEIGHT));
      // Bump: fluid rises momentarily after the drip impacts the surface.
      float bumpHeight = state.fluidBump * 1.2f / 16f;
      float finalTopY = Math.min(FLUID_Y_MAX, fluidTopY + bumpHeight);
      queue.submitCustom(matrices, fluidLayer, (entry, vc) ->
        drawBox(entry, vc,
          FLUID_X_MIN, FLUID_Y_BASE, FLUID_Z_MIN,
          FLUID_X_MAX, finalTopY,    FLUID_Z_MAX,
          FLUID_R, FLUID_G, FLUID_B, FLUID_A));
    }

    // ── 2. DRIP ANIMATION ────────────────────────────────────────────────
    // A small green bead falls along the central axis of the jar, from
    // the neck down to the current fluid surface.  Uses drawBoxFlat
    // (no directional shading) so the tiny bead reads as a uniform
    // luminous drop instead of looking asymmetric.
    if (state.showDrip && state.fillRatio < 1.0f) {
      float fluidSurfaceY = FLUID_Y_BASE + state.fillRatio * (FLUID_Y_MAX - FLUID_Y_BASE);
      float dropStartY    = 8.5f / 16f;
      if (dropStartY > fluidSurfaceY) {
        float dropBottomY = dropStartY + state.dripProgress * (fluidSurfaceY - dropStartY);
        float dropY0 = dropBottomY;
        float dropY1 = dropBottomY + DRIP_HEIGHT;
        final float dy0 = dropY0, dy1 = dropY1;
        queue.submitCustom(matrices, layer, (entry, vc) ->
          drawBoxFlat(entry, vc,
            0.5f - DRIP_HALF_WIDTH, dy0, 0.5f - DRIP_HALF_DEPTH,
            0.5f + DRIP_HALF_WIDTH, dy1, 0.5f + DRIP_HALF_DEPTH,
            FLUID_R, FLUID_G, FLUID_B, FLUID_A));
      }
    }

    // ── 2b. RIPPLE RING on fluid surface after drip impact ───────────────
    // Expanding ring drawn well above the opaque fluid (0.5/16 offset)
    // to avoid z-fighting with the depth-writing fluid layer.
    if (state.rippleProgress > 0f && state.rippleProgress < 1f && state.fillRatio > 0f) {
      float fluidSurfaceY = FLUID_Y_BASE + state.fillRatio * (FLUID_Y_MAX - FLUID_Y_BASE);
      float bumpH = state.fluidBump * 1.2f / 16f;
      float rippleY = Math.max(fluidSurfaceY + bumpH, FLUID_Y_BASE + FLUID_MIN_VISIBLE_HEIGHT) + 0.5f / 16f;
      float maxRadius = 3.0f / 16f;
      float radius = 0.5f / 16f + state.rippleProgress * maxRadius;
      float rippleAlpha = 0.85f * (1f - state.rippleProgress);
      float rippleH = 0.4f / 16f;

      final float rY0 = rippleY, rY1 = rippleY + rippleH;
      final float rOut = radius;
      final float rA = rippleAlpha;
      final float rR = Math.min(1f, FLUID_R * 1.5f);
      final float rG = Math.min(1f, FLUID_G * 1.2f);
      final float rB = Math.min(1f, FLUID_B * 1.3f);

      queue.submitCustom(matrices, layer, (entry, vc) ->
        drawBoxFlat(entry, vc,
                0.5f - rOut, rY0, 0.5f - rOut,
                0.5f + rOut, rY1, 0.5f + rOut,
                rR, rG, rB, rA));
    }

    // ── 3. KNOB PULSE (only while processing) ────────────────────────────
    // The knob itself is drawn by the baked active model (diamond_block
    // texture).  Here we add a slightly-inflated box wrapping the knob with
    // a pulsing white alpha — its faces live in the 0.05/16 air gap between
    // themselves and the knob's surface, so they don't fight with any baked
    // geometry.  Effect: the knob appears to breathe brighter → dimmer.
    // The outer "halo" aura is emitted as particles from the block entity.
    if (state.isProcessing) {
      float pulse = 0.5f + 0.5f * (float) Math.sin(state.animTime); // 0..1

      float skinAlpha = 0.12f + 0.48f * pulse;
      queue.submitCustom(matrices, layer, (entry, vc) ->
        drawBox(entry, vc,
          6.95f / 16f, 17.02f / 16f, 6.95f / 16f,
          9.05f / 16f, 18.05f / 16f, 9.05f / 16f,
          PULSE_R, PULSE_G, PULSE_B, skinAlpha));
    }

    // ── 4. CONNECTION TUBE ───────────────────────────────────────────────
    // Three segments along the axis pointing at the Transfer Table:
    //   • flange at the neighbour face   (bright copper, 5-px cross)
    //   • central pipe                   (darker copper, 3-px cross)
    //   • flange at the coil-interior end (bright copper, 5-px cross)
    //
    // Only emitted for HORIZONTAL connections: the coil's centre column is
    // occupied by the flask geometry (rim_bottom, body, neck, collars, cap,
    // knob), so a vertical tube would unavoidably cut through the bottle.
    // For vertical neighbours we let the table render a small brida on its
    // side of the boundary instead — see TransferTableRenderer.
    if (state.dirToTable != null && state.dirToTable.getAxis().isHorizontal()) {
      final Direction dir = state.dirToTable;
      final float[] flangeNear =
        tubeSegment(dir, TUBE_FLANGE_MIN, TUBE_FLANGE_MAX, 0f, TUBE_FLANGE_DEPTH);
      final float[] pipe =
        tubeSegment(dir, TUBE_PIPE_MIN,   TUBE_PIPE_MAX,
                    TUBE_FLANGE_DEPTH, TUBE_LENGTH - TUBE_FLANGE_DEPTH);
      final float[] flangeFar =
        tubeSegment(dir, TUBE_FLANGE_MIN, TUBE_FLANGE_MAX,
                    TUBE_LENGTH - TUBE_FLANGE_DEPTH, TUBE_LENGTH);

      // entityCutout writes depth — prevents translucent-sorting glitches
      // where back faces show through at oblique camera angles.
      RenderLayer tubeLayer = RenderLayers.entityCutout(WHITE_TEXTURE);
      queue.submitCustom(matrices, tubeLayer, (entry, vc) -> {
        drawBox(entry, vc,
          flangeNear[0], flangeNear[1], flangeNear[2],
          flangeNear[3], flangeNear[4], flangeNear[5],
          TUBE_FLANGE_R, TUBE_FLANGE_G, TUBE_FLANGE_B, 1.0f);
        drawBox(entry, vc,
          pipe[0], pipe[1], pipe[2],
          pipe[3], pipe[4], pipe[5],
          TUBE_PIPE_R, TUBE_PIPE_G, TUBE_PIPE_B, 1.0f);
        drawBox(entry, vc,
          flangeFar[0], flangeFar[1], flangeFar[2],
          flangeFar[3], flangeFar[4], flangeFar[5],
          TUBE_FLANGE_R, TUBE_FLANGE_G, TUBE_FLANGE_B, 1.0f);
      });
    }
  }

  // ── Helpers ──────────────────────────────────────────────────────────────

  /**
   * Builds the bounding box of one tube segment along the axis pointing at
   * the neighbouring Transfer Table.
   *
   * @param dir       direction from the coil toward the table
   * @param cmin      cross-section min (perpendicular to the tube axis)
   * @param cmax      cross-section max
   * @param axisStart distance from the neighbour face where this segment begins
   * @param axisEnd   distance from the neighbour face where this segment ends
   * @return {@code [x0, y0, z0, x1, y1, z1]} suitable for {@link #drawBox}
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

  private static void drawBox(MatrixStack.Entry entry, VertexConsumer vc,
                              float x0, float y0, float z0,
                              float x1, float y1, float z1,
                              float r,  float g,  float b,  float a) {
    // Winding matches outward-facing for entityCutout backface culling.
    // Directional shading so the box reads as 3D.
    float sY0 = 0.35f, sY1 = 1.00f, sZ = 0.70f, sX = 0.50f;
    quad(entry, vc, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, r*sY0,g*sY0,b*sY0,a,  0f,-1f, 0f); // -Y
    quad(entry, vc, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, r*sY1,g*sY1,b*sY1,a,  0f, 1f, 0f); // +Y
    quad(entry, vc, x0,y1,z0, x1,y1,z0, x1,y0,z0, x0,y0,z0, r*sZ, g*sZ, b*sZ, a,  0f, 0f,-1f); // -Z
    quad(entry, vc, x1,y1,z1, x0,y1,z1, x0,y0,z1, x1,y0,z1, r*sZ, g*sZ, b*sZ, a,  0f, 0f, 1f); // +Z
    quad(entry, vc, x0,y1,z1, x0,y1,z0, x0,y0,z0, x0,y0,z1, r*sX, g*sX, b*sX, a, -1f, 0f, 0f); // -X
    quad(entry, vc, x1,y1,z0, x1,y1,z1, x1,y0,z1, x1,y0,z0, r*sX, g*sX, b*sX, a,  1f, 0f, 0f); // +X
  }

  /** Uniform-colour box — no directional shading. Used for small emissive beads. */
  private static void drawBoxFlat(MatrixStack.Entry entry, VertexConsumer vc,
                                   float x0, float y0, float z0,
                                   float x1, float y1, float z1,
                                   float r,  float g,  float b,  float a) {
    quad(entry, vc, x0,y0,z0, x1,y0,z0, x1,y0,z1, x0,y0,z1, r,g,b,a,  0f,-1f, 0f);
    quad(entry, vc, x0,y1,z1, x1,y1,z1, x1,y1,z0, x0,y1,z0, r,g,b,a,  0f, 1f, 0f);
    quad(entry, vc, x0,y1,z0, x1,y1,z0, x1,y0,z0, x0,y0,z0, r,g,b,a,  0f, 0f,-1f);
    quad(entry, vc, x1,y1,z1, x0,y1,z1, x0,y0,z1, x1,y0,z1, r,g,b,a,  0f, 0f, 1f);
    quad(entry, vc, x0,y1,z1, x0,y1,z0, x0,y0,z0, x0,y0,z1, r,g,b,a, -1f, 0f, 0f);
    quad(entry, vc, x1,y1,z0, x1,y1,z1, x1,y0,z1, x1,y0,z0, r,g,b,a,  1f, 0f, 0f);
  }

  private static void quad(MatrixStack.Entry entry, VertexConsumer vc,
                            float ax, float ay, float az,
                            float bx, float by, float bz,
                            float cx, float cy, float cz,
                            float dx, float dy, float dz,
                            float r,  float g,  float b,  float a,
                            float nx, float ny, float nz) {
    vc.vertex(entry, ax, ay, az).color(r,g,b,a).texture(0f,0f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx, ny, nz);
    vc.vertex(entry, bx, by, bz).color(r,g,b,a).texture(1f,0f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx, ny, nz);
    vc.vertex(entry, cx, cy, cz).color(r,g,b,a).texture(1f,1f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx, ny, nz);
    vc.vertex(entry, dx, dy, dz).color(r,g,b,a).texture(0f,1f).overlay(OverlayTexture.DEFAULT_UV).light(FULL_LIGHT).normal(entry, nx, ny, nz);
  }
}
