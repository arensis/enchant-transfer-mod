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
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

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
        // TRANSLUCENT render type for the infusion coil and zinc smelter is now
        // declared data-side via "render_type": "minecraft:translucent" in their
        // block model JSONs (Fabric's BlockRenderLayerMap was removed in 26.2).

        // ── Custom networking ─────────────────────────────────────────────────
        // S2C: open the Selector screen for the given Transfer Table position
        ClientPlayNetworking.registerGlobalReceiver(OpenSelectorPayload.ID, (payload, context) ->
                context.client().execute(() ->
                        context.client().setScreenAndShow(new SelectorScreen(payload.tablePos()))));
    }
}
