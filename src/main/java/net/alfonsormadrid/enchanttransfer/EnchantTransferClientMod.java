package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class EnchantTransferClientMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(EnchantTransferMod.TRANSFER_TABLE_SCREEN_HANDLER, TransferTableScreen::new);
    }
}
