package net.alfonsormadrid.enchanttransfer.blocks.transfertable;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;

public class TransferTableBlock extends Block {
    public TransferTableBlock() {
        super(
            Block.Settings
                .of(Material.STONE)
                .sounds(new BlockSoundGroup(5.0F, 5.0F, SoundEvents.ENTITY_LIGHTNING_BOLT_IMPACT, SoundEvents.BLOCK_ANVIL_STEP, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundEvents.BLOCK_CHAIN_HIT, SoundEvents.BLOCK_SLIME_BLOCK_FALL))
                .strength(2, 2.0F)
                .ticksRandomly()
        );
    }
}
