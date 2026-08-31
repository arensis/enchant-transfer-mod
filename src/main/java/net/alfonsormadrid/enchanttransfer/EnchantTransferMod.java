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
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class EnchantTransferMod implements ModInitializer {
	public static final String MOD_ID = "enchanttransfer";

	// ── Transfer Table ─────────────────────────────────────────────────────
	public static final Identifier TRANSFER_TABLE_BLOCK_IDENTIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "transfer_table_block");

	public static final ResourceKey<Block> TRANSFER_TABLE_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, TRANSFER_TABLE_BLOCK_IDENTIFIER);
	public static final ResourceKey<Item> TRANSFER_TABLE_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, TRANSFER_TABLE_BLOCK_IDENTIFIER);
	public static final ResourceKey<Item> MAGIC_CARD_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_item"));
	// One registry key per coloured-card variant — derived from CardType.colorId()
	// so the lookup stays in sync if we ever rename a category.
	public static final ResourceKey<Item> MAGIC_CARD_BLUE_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_blue"));
	public static final ResourceKey<Item> MAGIC_CARD_GREEN_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_green"));
	public static final ResourceKey<Item> MAGIC_CARD_RED_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_red"));
	public static final ResourceKey<Item> MAGIC_CARD_YELLOW_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_yellow"));
	public static final ResourceKey<Item> MAGIC_CARD_PURPLE_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_purple"));
	public static final ResourceKey<Item> MAGIC_CARD_BLACK_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_black"));
	public static final ResourceKey<BlockEntityType<?>> TRANSFER_TABLE_BLOCK_ENTITY_KEY =
			ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, TRANSFER_TABLE_BLOCK_IDENTIFIER);

	public static final TransferTableBlock TRANSFER_TABLE_BLOCK = new TransferTableBlock(TRANSFER_TABLE_BLOCK_KEY);

	public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(MOD_ID, "general"));

	/**
	 * Extends the vanilla type so the server can ship the table's BlockPos to the
	 * client when the screen is opened (required for nav-row rendering).
	 */
	public static final ExtendedMenuType<TransferTableScreenHandler, BlockPos> TRANSFER_TABLE_SCREEN_HANDLER =
			Registry.register(BuiltInRegistries.MENU, TRANSFER_TABLE_BLOCK_IDENTIFIER,
					new ExtendedMenuType<>((syncId, inv, pos) -> new TransferTableScreenHandler(syncId, inv, pos),
							BlockPos.STREAM_CODEC));

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
	public static final Identifier ZINC_SMELTER_BLOCK_IDENTIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "zinc_smelter");

	public static final ResourceKey<Block> ZINC_SMELTER_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, ZINC_SMELTER_BLOCK_IDENTIFIER);
	public static final ResourceKey<Item> ZINC_SMELTER_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, ZINC_SMELTER_BLOCK_IDENTIFIER);
	public static final ResourceKey<BlockEntityType<?>> ZINC_SMELTER_BLOCK_ENTITY_KEY =
			ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, ZINC_SMELTER_BLOCK_IDENTIFIER);

	public static final ZincSmelterBlock ZINC_SMELTER_BLOCK = new ZincSmelterBlock(ZINC_SMELTER_BLOCK_KEY);
	public static final Item ZINC_SMELTER_ITEM = new net.minecraft.world.item.BlockItem(
			ZINC_SMELTER_BLOCK, new Item.Properties().setId(ZINC_SMELTER_ITEM_KEY).useBlockDescriptionPrefix());
	public static BlockEntityType<ZincSmelterBlockEntity> ZINC_SMELTER_BLOCK_ENTITY =
			FabricBlockEntityTypeBuilder.create(ZincSmelterBlockEntity::new, ZINC_SMELTER_BLOCK).build();

	public static final net.minecraft.world.inventory.MenuType<ZincSmelterScreenHandler> ZINC_SMELTER_SCREEN_HANDLER =
			Registry.register(BuiltInRegistries.MENU, ZINC_SMELTER_BLOCK_IDENTIFIER,
					new net.minecraft.world.inventory.MenuType<>(ZincSmelterScreenHandler::new,
							net.minecraft.world.flag.FeatureFlags.VANILLA_SET));

	// ── Zinc Smelter items ─────────────────────────────────────────────────
	public static final ResourceKey<Item> ZINC_OXIDE_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "zinc_oxide"));
	public static final ResourceKey<Item> ZINC_SHEET_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "zinc_sheet"));
	public static final ResourceKey<Item> BRASS_INGOT_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "brass_ingot"));

	public static final Item ZINC_OXIDE_ITEM  = new Item(new Item.Properties().setId(ZINC_OXIDE_ITEM_KEY));
	public static final Item ZINC_SHEET_ITEM  = new Item(new Item.Properties().setId(ZINC_SHEET_ITEM_KEY));
	public static final Item BRASS_INGOT_ITEM = new Item(new Item.Properties().setId(BRASS_INGOT_ITEM_KEY));

	// ── Infusion Coil ──────────────────────────────────────────────────────
	public static final Identifier INFUSION_COIL_BLOCK_IDENTIFIER = Identifier.fromNamespaceAndPath(MOD_ID, "infusion_coil_block");

	public static final ResourceKey<Block> INFUSION_COIL_BLOCK_KEY =
			ResourceKey.create(Registries.BLOCK, INFUSION_COIL_BLOCK_IDENTIFIER);
	public static final ResourceKey<Item> INFUSION_COIL_ITEM_KEY =
			ResourceKey.create(Registries.ITEM, INFUSION_COIL_BLOCK_IDENTIFIER);
	public static final ResourceKey<BlockEntityType<?>> INFUSION_COIL_BLOCK_ENTITY_KEY =
			ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, INFUSION_COIL_BLOCK_IDENTIFIER);

	public static final InfusionCoilBlock INFUSION_COIL_BLOCK = new InfusionCoilBlock(INFUSION_COIL_BLOCK_KEY);
	public static final InfusionCoilItem INFUSION_COIL_ITEM = new InfusionCoilItem(INFUSION_COIL_BLOCK, INFUSION_COIL_ITEM_KEY);
	public static BlockEntityType<InfusionCoilBlockEntity> INFUSION_COIL_BLOCK_ENTITY =
			FabricBlockEntityTypeBuilder.create(InfusionCoilBlockEntity::new, INFUSION_COIL_BLOCK).build();

	public static final ExtendedMenuType<InfusionCoilScreenHandler, BlockPos> INFUSION_COIL_SCREEN_HANDLER =
			Registry.register(BuiltInRegistries.MENU, INFUSION_COIL_BLOCK_IDENTIFIER,
					new ExtendedMenuType<>((syncId, inv, pos) -> new InfusionCoilScreenHandler(syncId, inv, pos),
							BlockPos.STREAM_CODEC));

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.BLOCK, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_ITEM);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, TRANSFER_TABLE_BLOCK_IDENTIFIER, TRANSFER_TABLE_BLOCK_ENTITY);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_item"),   MAGIC_CARD_ITEM);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_blue"),   MAGIC_CARD_BLUE);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_green"),  MAGIC_CARD_GREEN);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_red"),    MAGIC_CARD_RED);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_yellow"), MAGIC_CARD_YELLOW);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_purple"), MAGIC_CARD_PURPLE);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "magic_card_black"),  MAGIC_CARD_BLACK);

		Registry.register(BuiltInRegistries.BLOCK, INFUSION_COIL_BLOCK_IDENTIFIER, INFUSION_COIL_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, INFUSION_COIL_BLOCK_IDENTIFIER, INFUSION_COIL_ITEM);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, INFUSION_COIL_BLOCK_IDENTIFIER, INFUSION_COIL_BLOCK_ENTITY);

		Registry.register(BuiltInRegistries.BLOCK, ZINC_SMELTER_BLOCK_IDENTIFIER, ZINC_SMELTER_BLOCK);
		Registry.register(BuiltInRegistries.ITEM, ZINC_SMELTER_BLOCK_IDENTIFIER, ZINC_SMELTER_ITEM);
		Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, ZINC_SMELTER_BLOCK_IDENTIFIER, ZINC_SMELTER_BLOCK_ENTITY);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "zinc_oxide"),  ZINC_OXIDE_ITEM);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "zinc_sheet"),  ZINC_SHEET_ITEM);
		Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(MOD_ID, "brass_ingot"), BRASS_INGOT_ITEM);

		// Populate the tab directly via displayItems: a custom tab with no
		// display generator produces no output, so CreativeModeTabEvents never
		// fires for it and Minecraft hides the (empty) tab.
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ITEM_GROUP_KEY, FabricCreativeModeTab.builder()
				.icon(() -> new ItemStack(TRANSFER_TABLE_BLOCK))
				.title(Component.translatable("itemGroup.enchanttransfer.general"))
				.displayItems((params, entries) -> {
					entries.accept(TRANSFER_TABLE_ITEM);
					entries.accept(INFUSION_COIL_ITEM);
					entries.accept(MAGIC_CARD_ITEM);
					entries.accept(MAGIC_CARD_BLUE);
					entries.accept(MAGIC_CARD_GREEN);
					entries.accept(MAGIC_CARD_RED);
					entries.accept(MAGIC_CARD_YELLOW);
					entries.accept(MAGIC_CARD_PURPLE);
					entries.accept(MAGIC_CARD_BLACK);
					entries.accept(ZINC_SMELTER_ITEM);
					entries.accept(ZINC_OXIDE_ITEM);
					entries.accept(ZINC_SHEET_ITEM);
					entries.accept(BRASS_INGOT_ITEM);
				})
				.build());

		// ── Custom networking ──────────────────────────────────────────────────
		// S2C: server → client to open the Selector screen
		PayloadTypeRegistry.clientboundPlay().register(OpenSelectorPayload.ID, OpenSelectorPayload.CODEC);
		// C2S: client → server to ask the server to open a GUI for a given block pos
		PayloadTypeRegistry.serverboundPlay().register(RequestOpenGuiPayload.ID, RequestOpenGuiPayload.CODEC);

		// Server-side handler: open the appropriate GUI for whatever block is at targetPos
		ServerPlayNetworking.registerGlobalReceiver(RequestOpenGuiPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayer player = context.player();
				ServerLevel world = (ServerLevel) player.level();
				BlockPos targetPos = payload.blockPos();
				// Validate that the player is close enough
				double dx = player.getX() - targetPos.getX();
				double dy = player.getY() - targetPos.getY();
				double dz = player.getZ() - targetPos.getZ();
				if (dx * dx + dy * dy + dz * dz <= 64.0) { // within 8 blocks
					MenuProvider factory =
							world.getBlockState(targetPos).getMenuProvider(world, targetPos);
					if (factory != null) {
						player.openMenu(factory);
					}
				}
			});
		});
	}
}
