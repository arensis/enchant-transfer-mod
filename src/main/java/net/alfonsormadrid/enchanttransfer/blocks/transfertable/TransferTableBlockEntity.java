package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;

public class TransferTableBlockEntity extends BlockEntity {
    private int number = 7;

    public TransferTableBlockEntity() {
        super(EnchantTransferMod.TRANSFER_TABLE_BLOCK_ENTITY);
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);

        tag.putInt("number", number);

        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag) {
        super.fromTag(state, tag);
        number = tag.getInt("number");
    }
}
