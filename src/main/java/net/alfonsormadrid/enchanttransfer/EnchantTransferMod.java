package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlock;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EnchantTransferMod implements ModInitializer {
	public static final String MOD_ID = "enchanttransfer";

	public static final TransferTableBlock TRANSFER_TABLE_BLOCK = new TransferTableBlock();
	public static final ItemGroup ENCHANT_TRANSFER = FabricItemGroupBuilder.build(
			new Identifier(MOD_ID, "general"),
			() -> new ItemStack(TRANSFER_TABLE_BLOCK)
	);
	public static final TransferTableItem TRANSFER_TABLE_ITEM = new TransferTableItem(TRANSFER_TABLE_BLOCK);
	public static BlockEntityType<TransferTableBlockEntity> TRANSFER_TABLE_BLOCK_ENTITY =
			BlockEntityType.Builder.create(TransferTableBlockEntity::new, TRANSFER_TABLE_BLOCK).build(null);
	public static final MagicCardItem MAGIC_CARD_ITEM = new MagicCardItem();

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "magic_card_item"), MAGIC_CARD_ITEM);
		Registry.register(Registry.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "transfer_table_block"), TRANSFER_TABLE_BLOCK_ENTITY);
		Registry.register(Registry.BLOCK,new Identifier(MOD_ID, "transfer_table_block"), TRANSFER_TABLE_BLOCK);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "transfer_table_block"), TRANSFER_TABLE_ITEM);
	}
}
