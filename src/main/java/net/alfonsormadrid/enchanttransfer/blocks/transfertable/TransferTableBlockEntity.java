package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreenHandler;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class TransferTableBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {
    public TransferTableBlockEntity() {
        super(EnchantTransferMod.TRANSFER_TABLE_BLOCK_ENTITY);
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TransferTableScreenHandler(syncId, playerInventory);
    }

    @Override
    public Text getDisplayName() {
        return new TranslatableText(getCachedState().getBlock().getTranslationKey());
    }
}
