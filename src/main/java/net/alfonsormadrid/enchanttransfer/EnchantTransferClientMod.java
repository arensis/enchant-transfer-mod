package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;

@Environment(EnvType.CLIENT)
public class EnchantTransferClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, TransferTableScreen::new);
    }
}