package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlock;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableItem;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.loot.v1.FabricLootPoolBuilder;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConstantLootTableRange;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class EnchantTransferMod implements ModInitializer {
	public static final String MOD_ID = "enchanttransfer";
	public static final Identifier TRANSFER_TABLE_BLOCK_IDENTIFIER = new Identifier(MOD_ID, "transfer_table_block");
	public static final Identifier TRANSFER_TABLE_BLOCK_LOOT_TABLE_ID = new Identifier(MOD_ID, "blocks/transfer_table_block");

	public static final TransferTableBlock TRANSFER_TABLE_BLOCK = new TransferTableBlock();
	public static final ItemGroup ENCHANT_TRANSFER_ITEM_GROUP = FabricItemGroupBuilder.build(
			new Identifier(MOD_ID, "general"),
			() -> new ItemStack(TRANSFER_TABLE_BLOCK)
	);

	public static final ScreenHandlerType<TransferTableScreenHandler> TRANSFER_TABLE_SCREEN_HANDLER;
	static {
		TRANSFER_TABLE_SCREEN_HANDLER = ScreenHandlerRegistry.registerSimple(TRANSFER_TABLE_BLOCK_IDENTIFIER, TransferTableScreenHandler::new);
	}

	public static final TransferTableItem TRANSFER_TABLE_ITEM = new TransferTableItem(TRANSFER_TABLE_BLOCK);
	public static BlockEntityType<TransferTableBlockEntity> TRANSFER_TABLE_BLOCK_ENTITY =
			BlockEntityType.Builder.create(TransferTableBlockEntity::new, TRANSFER_TABLE_BLOCK).build(null);

	public static final MagicCardItem MAGIC_CARD_ITEM = new MagicCardItem();

	@Override
	public void onInitialize() {
		Registry.register(Registry.BLOCK, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK);
		Registry.register(Registry.ITEM, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_ITEM);

		Registry.register(Registry.BLOCK_ENTITY_TYPE, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK_ENTITY);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "magic_card_item"), MAGIC_CARD_ITEM);

		modifyLootTables();
	}

	private void modifyLootTables() {
		LootTableLoadingCallback.EVENT.register((resourceManager, lootManager, id, supplier, setter) -> {
			//Checks for transfer_table_block loot table
			if (TRANSFER_TABLE_BLOCK_LOOT_TABLE_ID.equals(id)) {
				//Add single individual item
				FabricLootPoolBuilder poolBuilder = FabricLootPoolBuilder.builder()
						.rolls(ConstantLootTableRange.create(1))
						.with(LootTableEntry.builder(TRANSFER_TABLE_BLOCK_LOOT_TABLE_ID));

				supplier.withPool(poolBuilder.build());
			}
		});
	}
}
