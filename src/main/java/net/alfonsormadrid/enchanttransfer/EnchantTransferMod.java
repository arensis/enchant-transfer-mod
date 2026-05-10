package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlock;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableItem;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class EnchantTransferMod implements ModInitializer {
	public static final String MOD_ID = "enchanttransfer";
	public static final Identifier TRANSFER_TABLE_BLOCK_IDENTIFIER = Identifier.of(MOD_ID, "transfer_table_block");

	public static final TransferTableBlock TRANSFER_TABLE_BLOCK = new TransferTableBlock();

	public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
			RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MOD_ID, "general"));

	public static final ScreenHandlerType<TransferTableScreenHandler> TRANSFER_TABLE_SCREEN_HANDLER =
			Registry.register(Registries.SCREEN_HANDLER, TRANSFER_TABLE_BLOCK_IDENTIFIER,
					new ScreenHandlerType<TransferTableScreenHandler>((syncId, inv) -> new TransferTableScreenHandler(syncId, inv), FeatureFlags.VANILLA_FEATURES));

	public static final TransferTableItem TRANSFER_TABLE_ITEM = new TransferTableItem(TRANSFER_TABLE_BLOCK);
	public static BlockEntityType<TransferTableBlockEntity> TRANSFER_TABLE_BLOCK_ENTITY =
			BlockEntityType.Builder.create(TransferTableBlockEntity::new, TRANSFER_TABLE_BLOCK).build();

	public static final MagicCardItem MAGIC_CARD_ITEM = new MagicCardItem();

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK);
		Registry.register(Registries.ITEM, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK_ENTITY);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_item"), MAGIC_CARD_ITEM);

		Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, FabricItemGroup.builder()
				.icon(() -> new ItemStack(TRANSFER_TABLE_BLOCK))
				.displayName(Text.translatable("itemGroup.enchanttransfer.general"))
				.build());

		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register(entries -> {
			entries.add(TRANSFER_TABLE_ITEM);
			entries.add(MAGIC_CARD_ITEM);
		});
	}
}
