package net.alfonsormadrid.enchanttransfer.mixin;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Nothing can be placed on top of a Deck Lectern: its glass bell takes the
 * whole column, which is why a lectern is worth two stacked bookshelves at
 * the table ({@code LibraryResonanceService}).  Covers every block placed
 * from an item (players, and anything else going through {@link BlockItem}).
 */
@Mixin(BlockItem.class)
public abstract class DeckLecternStackingMixin {

    @Inject(method = "canPlace", at = @At("HEAD"), cancellable = true)
    private void enchanttransfer$nothingOnLectern(BlockPlaceContext context, BlockState state,
                                                  CallbackInfoReturnable<Boolean> cir) {
        if (context.getLevel().getBlockState(context.getClickedPos().below()).is(EnchantTransferMod.DECK_LECTERN_BLOCK)) {
            cir.setReturnValue(false);
        }
    }
}
