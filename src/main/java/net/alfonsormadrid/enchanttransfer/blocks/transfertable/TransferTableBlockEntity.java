package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.modules.ModuleConnectionRegistry;
import net.alfonsormadrid.enchanttransfer.modules.TransferTableModule;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreenHandler;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TransferTableBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos> {

    private final ModuleConnectionRegistry moduleRegistry = new ModuleConnectionRegistry();

    public TransferTableBlockEntity(BlockPos pos, BlockState state) {
        super(EnchantTransferMod.TRANSFER_TABLE_BLOCK_ENTITY, pos, state);
    }

    public ModuleConnectionRegistry getModuleRegistry() {
        return moduleRegistry;
    }

    /**
     * Called by a module's block entity when it attaches to this core.
     * The {@code coreFace} is the face of <em>this</em> core through which the
     * module sits.
     */
    public void attachModule(Direction coreFace, TransferTableModule module) {
        moduleRegistry.attach(coreFace, module);
        setChanged();
    }

    public void detachModule(Direction coreFace) {
        moduleRegistry.detach(coreFace);
        setChanged();
    }

    /** Sends the table's own position to the client so the screen can access adjacent modules. */
    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new TransferTableScreenHandler(syncId, playerInventory, this.worldPosition);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }
}
