package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.modules.ModuleConnectionRegistry;
import net.alfonsormadrid.enchanttransfer.modules.TransferTableModule;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class TransferTableBlockEntity extends BlockEntity implements NamedScreenHandlerFactory {

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
        markDirty();
    }

    public void detachModule(Direction coreFace) {
        moduleRegistry.detach(coreFace);
        markDirty();
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new TransferTableScreenHandler(syncId, playerInventory);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }
}
