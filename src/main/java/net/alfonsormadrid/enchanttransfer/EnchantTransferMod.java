package net.alfonsormadrid.enchanttransfer;

import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlock;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilItem;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlockEntity;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlock;
import net.alfonsormadrid.enchanttransfer.blocks.zincsmelter.ZincSmelterBlockEntity;
import net.alfonsormadrid.enchanttransfer.network.OpenSelectorPayload;
import net.alfonsormadrid.enchanttransfer.network.RequestOpenGuiPayload;
import net.alfonsormadrid.enchanttransfer.screens.infusioncoil.InfusionCoilScreenHandler;
import net.alfonsormadrid.enchanttransfer.screens.zincsmelter.ZincSmelterScreenHandler;
import net.alfonsormadrid.enchanttransfer.item.CardType;
import net.alfonsormadrid.enchanttransfer.item.MagicCardItem;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableBlock;
import net.alfonsormadrid.enchanttransfer.blocks.transfertable.TransferTableItem;
import net.alfonsormadrid.enchanttransfer.screens.transfertable.TransferTableScreenHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class EnchantTransferMod implements ModInitializer {
	public static final String MOD_ID = "enchanttransfer";

	// ── Transfer Table ─────────────────────────────────────────────────────
	public static final Identifier TRANSFER_TABLE_BLOCK_IDENTIFIER = Identifier.of(MOD_ID, "transfer_table_block");

	public static final RegistryKey<Block> TRANSFER_TABLE_BLOCK_KEY =
			RegistryKey.of(RegistryKeys.BLOCK, TRANSFER_TABLE_BLOCK_IDENTIFIER);
	public static final RegistryKey<Item> TRANSFER_TABLE_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, TRANSFER_TABLE_BLOCK_IDENTIFIER);
	public static final RegistryKey<Item> MAGIC_CARD_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_item"));
	// One registry key per coloured-card variant — derived from CardType.colorId()
	// so the lookup stays in sync if we ever rename a category.
	public static final RegistryKey<Item> MAGIC_CARD_BLUE_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_blue"));
	public static final RegistryKey<Item> MAGIC_CARD_GREEN_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_green"));
	public static final RegistryKey<Item> MAGIC_CARD_RED_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_red"));
	public static final RegistryKey<Item> MAGIC_CARD_YELLOW_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_yellow"));
	public static final RegistryKey<Item> MAGIC_CARD_PURPLE_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_purple"));
	public static final RegistryKey<Item> MAGIC_CARD_BLACK_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "magic_card_black"));
	public static final RegistryKey<BlockEntityType<?>> TRANSFER_TABLE_BLOCK_ENTITY_KEY =
			RegistryKey.of(RegistryKeys.BLOCK_ENTITY_TYPE, TRANSFER_TABLE_BLOCK_IDENTIFIER);

	public static final TransferTableBlock TRANSFER_TABLE_BLOCK = new TransferTableBlock(TRANSFER_TABLE_BLOCK_KEY);

	public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY =
			RegistryKey.of(RegistryKeys.ITEM_GROUP, Identifier.of(MOD_ID, "general"));

	/**
	 * Extends the vanilla type so the server can ship the table's BlockPos to the
	 * client when the screen is opened (required for nav-row rendering).
	 */
	public static final ExtendedScreenHandlerType<TransferTableScreenHandler, BlockPos> TRANSFER_TABLE_SCREEN_HANDLER =
			Registry.register(Registries.SCREEN_HANDLER, TRANSFER_TABLE_BLOCK_IDENTIFIER,
					new ExtendedScreenHandlerType<>((syncId, inv, pos) -> new TransferTableScreenHandler(syncId, inv, pos),
							BlockPos.PACKET_CODEC));

	public static final TransferTableItem TRANSFER_TABLE_ITEM = new TransferTableItem(TRANSFER_TABLE_BLOCK, TRANSFER_TABLE_ITEM_KEY);
	public static BlockEntityType<TransferTableBlockEntity> TRANSFER_TABLE_BLOCK_ENTITY =
			FabricBlockEntityTypeBuilder.create(TransferTableBlockEntity::new, TRANSFER_TABLE_BLOCK).build();

	public static final MagicCardItem MAGIC_CARD_ITEM   = new MagicCardItem(MAGIC_CARD_ITEM_KEY);
	public static final MagicCardItem MAGIC_CARD_BLUE   = new MagicCardItem(MAGIC_CARD_BLUE_KEY,   CardType.BLUE);
	public static final MagicCardItem MAGIC_CARD_GREEN  = new MagicCardItem(MAGIC_CARD_GREEN_KEY,  CardType.GREEN);
	public static final MagicCardItem MAGIC_CARD_RED    = new MagicCardItem(MAGIC_CARD_RED_KEY,    CardType.RED);
	public static final MagicCardItem MAGIC_CARD_YELLOW = new MagicCardItem(MAGIC_CARD_YELLOW_KEY, CardType.YELLOW);
	public static final MagicCardItem MAGIC_CARD_PURPLE = new MagicCardItem(MAGIC_CARD_PURPLE_KEY, CardType.PURPLE);
	public static final MagicCardItem MAGIC_CARD_BLACK  = new MagicCardItem(MAGIC_CARD_BLACK_KEY,  CardType.BLACK);

	// ── Zinc Smelter ───────────────────────────────────────────────────────
	public static final Identifier ZINC_SMELTER_BLOCK_IDENTIFIER = Identifier.of(MOD_ID, "zinc_smelter");

	public static final RegistryKey<Block> ZINC_SMELTER_BLOCK_KEY =
			RegistryKey.of(RegistryKeys.BLOCK, ZINC_SMELTER_BLOCK_IDENTIFIER);
	public static final RegistryKey<Item> ZINC_SMELTER_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, ZINC_SMELTER_BLOCK_IDENTIFIER);
	public static final RegistryKey<BlockEntityType<?>> ZINC_SMELTER_BLOCK_ENTITY_KEY =
			RegistryKey.of(RegistryKeys.BLOCK_ENTITY_TYPE, ZINC_SMELTER_BLOCK_IDENTIFIER);

	public static final ZincSmelterBlock ZINC_SMELTER_BLOCK = new ZincSmelterBlock(ZINC_SMELTER_BLOCK_KEY);
	public static final Item ZINC_SMELTER_ITEM = new net.minecraft.item.BlockItem(
			ZINC_SMELTER_BLOCK, new Item.Settings().registryKey(ZINC_SMELTER_ITEM_KEY).useBlockPrefixedTranslationKey());
	public static BlockEntityType<ZincSmelterBlockEntity> ZINC_SMELTER_BLOCK_ENTITY =
			FabricBlockEntityTypeBuilder.create(ZincSmelterBlockEntity::new, ZINC_SMELTER_BLOCK).build();

	public static final net.minecraft.screen.ScreenHandlerType<ZincSmelterScreenHandler> ZINC_SMELTER_SCREEN_HANDLER =
			Registry.register(Registries.SCREEN_HANDLER, ZINC_SMELTER_BLOCK_IDENTIFIER,
					new net.minecraft.screen.ScreenHandlerType<>(ZincSmelterScreenHandler::new,
							net.minecraft.resource.featuretoggle.FeatureFlags.VANILLA_FEATURES));

	// ── Zinc Smelter items ─────────────────────────────────────────────────
	public static final RegistryKey<Item> ZINC_OXIDE_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "zinc_oxide"));
	public static final RegistryKey<Item> ZINC_SHEET_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "zinc_sheet"));
	public static final RegistryKey<Item> BRASS_INGOT_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, Identifier.of(MOD_ID, "brass_ingot"));

	public static final Item ZINC_OXIDE_ITEM  = new Item(new Item.Settings().registryKey(ZINC_OXIDE_ITEM_KEY));
	public static final Item ZINC_SHEET_ITEM  = new Item(new Item.Settings().registryKey(ZINC_SHEET_ITEM_KEY));
	public static final Item BRASS_INGOT_ITEM = new Item(new Item.Settings().registryKey(BRASS_INGOT_ITEM_KEY));

	// ── Infusion Coil ──────────────────────────────────────────────────────
	public static final Identifier INFUSION_COIL_BLOCK_IDENTIFIER = Identifier.of(MOD_ID, "infusion_coil_block");

	public static final RegistryKey<Block> INFUSION_COIL_BLOCK_KEY =
			RegistryKey.of(RegistryKeys.BLOCK, INFUSION_COIL_BLOCK_IDENTIFIER);
	public static final RegistryKey<Item> INFUSION_COIL_ITEM_KEY =
			RegistryKey.of(RegistryKeys.ITEM, INFUSION_COIL_BLOCK_IDENTIFIER);
	public static final RegistryKey<BlockEntityType<?>> INFUSION_COIL_BLOCK_ENTITY_KEY =
			RegistryKey.of(RegistryKeys.BLOCK_ENTITY_TYPE, INFUSION_COIL_BLOCK_IDENTIFIER);

	public static final InfusionCoilBlock INFUSION_COIL_BLOCK = new InfusionCoilBlock(INFUSION_COIL_BLOCK_KEY);
	public static final InfusionCoilItem INFUSION_COIL_ITEM = new InfusionCoilItem(INFUSION_COIL_BLOCK, INFUSION_COIL_ITEM_KEY);
	public static BlockEntityType<InfusionCoilBlockEntity> INFUSION_COIL_BLOCK_ENTITY =
			FabricBlockEntityTypeBuilder.create(InfusionCoilBlockEntity::new, INFUSION_COIL_BLOCK).build();

	public static final ExtendedScreenHandlerType<InfusionCoilScreenHandler, BlockPos> INFUSION_COIL_SCREEN_HANDLER =
			Registry.register(Registries.SCREEN_HANDLER, INFUSION_COIL_BLOCK_IDENTIFIER,
					new ExtendedScreenHandlerType<>((syncId, inv, pos) -> new InfusionCoilScreenHandler(syncId, inv, pos),
							BlockPos.PACKET_CODEC));

	@Override
	public void onInitialize() {
		Registry.register(Registries.BLOCK, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK);
		Registry.register(Registries.ITEM, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK_ENTITY);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_item"),   MAGIC_CARD_ITEM);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_blue"),   MAGIC_CARD_BLUE);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_green"),  MAGIC_CARD_GREEN);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_red"),    MAGIC_CARD_RED);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_yellow"), MAGIC_CARD_YELLOW);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_purple"), MAGIC_CARD_PURPLE);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "magic_card_black"),  MAGIC_CARD_BLACK);

		Registry.register(Registries.BLOCK, INFUSION_COIL_BLOCK_IDENTIFIER, INFUSION_COIL_BLOCK);
		Registry.register(Registries.ITEM, INFUSION_COIL_BLOCK_IDENTIFIER, INFUSION_COIL_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, INFUSION_COIL_BLOCK_IDENTIFIER, INFUSION_COIL_BLOCK_ENTITY);

		Registry.register(Registries.BLOCK, ZINC_SMELTER_BLOCK_IDENTIFIER, ZINC_SMELTER_BLOCK);
		Registry.register(Registries.ITEM, ZINC_SMELTER_BLOCK_IDENTIFIER, ZINC_SMELTER_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, ZINC_SMELTER_BLOCK_IDENTIFIER, ZINC_SMELTER_BLOCK_ENTITY);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "zinc_oxide"),  ZINC_OXIDE_ITEM);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "zinc_sheet"),  ZINC_SHEET_ITEM);
		Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "brass_ingot"), BRASS_INGOT_ITEM);

		Registry.register(Registries.ITEM_GROUP, ITEM_GROUP_KEY, FabricItemGroup.builder()
				.icon(() -> new ItemStack(TRANSFER_TABLE_BLOCK))
				.displayName(Text.translatable("itemGroup.enchanttransfer.general"))
				.build());

		ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register(entries -> {
			entries.add(TRANSFER_TABLE_ITEM);
			entries.add(INFUSION_COIL_ITEM);
			entries.add(MAGIC_CARD_ITEM);
			entries.add(MAGIC_CARD_BLUE);
			entries.add(MAGIC_CARD_GREEN);
			entries.add(MAGIC_CARD_RED);
			entries.add(MAGIC_CARD_YELLOW);
			entries.add(MAGIC_CARD_PURPLE);
			entries.add(MAGIC_CARD_BLACK);
			entries.add(ZINC_SMELTER_ITEM);
			entries.add(ZINC_OXIDE_ITEM);
			entries.add(ZINC_SHEET_ITEM);
			entries.add(BRASS_INGOT_ITEM);
		});

		// ── Custom networking ──────────────────────────────────────────────────
		// S2C: server → client to open the Selector screen
		PayloadTypeRegistry.playS2C().register(OpenSelectorPayload.ID, OpenSelectorPayload.CODEC);
		// C2S: client → server to ask the server to open a GUI for a given block pos
		PayloadTypeRegistry.playC2S().register(RequestOpenGuiPayload.ID, RequestOpenGuiPayload.CODEC);

		// Server-side handler: open the appropriate GUI for whatever block is at targetPos
		ServerPlayNetworking.registerGlobalReceiver(RequestOpenGuiPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				BlockPos targetPos = payload.blockPos();
				// Validate that the player is close enough
				double dx = player.getX() - targetPos.getX();
				double dy = player.getY() - targetPos.getY();
				double dz = player.getZ() - targetPos.getZ();
				if (dx * dx + dy * dy + dz * dz <= 64.0) { // within 8 blocks
					NamedScreenHandlerFactory factory =
							world.getBlockState(targetPos).createScreenHandlerFactory(world, targetPos);
					if (factory != null) {
						player.openHandledScreen(factory);
					}
				}
			});
		});
	}
}
