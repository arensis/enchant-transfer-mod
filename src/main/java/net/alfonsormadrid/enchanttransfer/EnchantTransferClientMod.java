package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.network.OpenSelectorPayload;
import net.alfonsormadrid.enchanttransfer.renderers.InfusionCoilRenderer;
import net.alfonsormadrid.enchanttransfer.renderers.TransferTableRenderer;
import net.alfonsormadrid.enchanttransfer.screens.infusioncoil.InfusionCoilScreen;
import net.alfonsormadrid.enchanttransfer.screens.selector.SelectorScreen;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreen;
import net.alfonsormadrid.enchanttransfer.renderers.ZincSmelterRenderer;
import net.alfonsormadrid.enchanttransfer.screens.zincsmelter.ZincSmelterScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

@Environment(EnvType.CLIENT)
public class EnchantTransferClientMod implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // ── GUI screens ───────────────────────────────────────────────────────
        MenuScreens.register(
                EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER,
                TransferTableScreen::new);

        MenuScreens.register(
                EnchantTransferMod.INFUSION_COIL_SCREEN_HANDLER,
                InfusionCoilScreen::new);

        MenuScreens.register(
                EnchantTransferMod.ZINC_SMELTER_SCREEN_HANDLER,
                ZincSmelterScreen::new);

        // ── Block entity renderers ────────────────────────────────────────────
        BlockEntityRenderers.register(
                EnchantTransferMod.TRANSFER_TABLE_BLOCK_ENTITY,
                ctx -> new TransferTableRenderer(ctx));

        BlockEntityRenderers.register(
                EnchantTransferMod.INFUSION_COIL_BLOCK_ENTITY,
                ctx -> new InfusionCoilRenderer(ctx));

        BlockEntityRenderers.register(
                EnchantTransferMod.ZINC_SMELTER_BLOCK_ENTITY,
                ctx -> new ZincSmelterRenderer(ctx));

        // ── Render layers ─────────────────────────────────────────────────────
        // TRANSLUCENT: the infusor_glass.png texture has 196 semi-transparent
        // pixels and zero fully-transparent ones.  CUTOUT would render every
        // pixel as fully opaque, completely hiding the BER fluid fill inside.
        // TRANSLUCENT renders those pixels with alpha blending so the fluid is
        // visible through the glass body.  Z-fighting is avoided by the 0.5px
        // inset on the BER fluid box AND by the VIEW_OFFSET_Z_LAYERING used by
        // the debugFilledBox render layer.
        BlockRenderLayerMap.putBlock(
                EnchantTransferMod.INFUSION_COIL_BLOCK,
                ChunkSectionLayer.TRANSLUCENT);

        // The smelter_glass.png mirilla texture has alpha — TRANSLUCENT
        // lets the BER fire glow show through the glass when lit.
        BlockRenderLayerMap.putBlock(
                EnchantTransferMod.ZINC_SMELTER_BLOCK,
                ChunkSectionLayer.TRANSLUCENT);

        // ── Custom networking ─────────────────────────────────────────────────
        // S2C: open the Selector screen for the given Transfer Table position
        ClientPlayNetworking.registerGlobalReceiver(OpenSelectorPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreen(new SelectorScreen(payload.tablePos()))));
    }
}
