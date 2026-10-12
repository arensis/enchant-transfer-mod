package net.alfonsormadrid.enchanttransfer.blocks.decklectern;

import com.mojang.serialization.MapCodec;
import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Deck Lectern: a low brass-and-wood base with a tilted velvet tray for
 * 3 Card Decks, under a stepped glass bell.
 * <p>
 * Blockstate drives everything the model shows:
 * <ul>
 *   <li>{@code facing} — oriented by the player, like a furnace.</li>
 *   <li>{@code slot_0..2} — one indicator lamp per slot ({@link LampState}).</li>
 *   <li>{@code active} — lights the amethyst on top of the bell; only active
 *       lecterns count for nearby enchanting tables. Off by default. Shift +
 *       right-click switches it on (only with a deck that has cards) or off;
 *       it switches itself off when no deck has cards left, never on.</li>
 * </ul>
 * The decks on the tray are drawn by the block entity renderer.
 */
public class DeckLecternBlock extends BaseEntityBlock {

    public static final MapCodec<DeckLecternBlock> CODEC = simpleCodec(DeckLecternBlock::new);

    /** One rune every this many client ticks (on average, ~1.5 s) while lit. */
    private static final int RUNE_CHANCE = 30;

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final EnumProperty<LampState> SLOT_0 = EnumProperty.create("slot_0", LampState.class);
    public static final EnumProperty<LampState> SLOT_1 = EnumProperty.create("slot_1", LampState.class);
    public static final EnumProperty<LampState> SLOT_2 = EnumProperty.create("slot_2", LampState.class);
    public static final java.util.List<EnumProperty<LampState>> SLOTS = java.util.List.of(SLOT_0, SLOT_1, SLOT_2);

    /** Base + glass bell, approximated with the bell's tiers. */
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0, 0, 0, 16, 5, 16),
            Block.box(1, 5, 1, 15, 12, 15),
            Block.box(3, 12, 3, 13, 14.7, 13),
            Block.box(6, 14.7, 6, 10, 16, 10));

    public DeckLecternBlock(ResourceKey<Block> registryKey) {
        this(BlockBehaviour.Properties.of()
                .setId(registryKey)
                .mapColor(MapColor.WOOD)
                .sound(SoundType.WOOD)
                .strength(2.5f)
                .lightLevel(state -> isLit(state) ? 5 : 0)
                .noOcclusion());
    }

    private DeckLecternBlock(BlockBehaviour.Properties settings) {
        super(settings);
        registerDefaultState(getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(SLOT_0, LampState.NONE)
                .setValue(SLOT_1, LampState.NONE)
                .setValue(SLOT_2, LampState.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ACTIVE, SLOT_0, SLOT_1, SLOT_2);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public static boolean hasCards(BlockState state) {
        return SLOTS.stream().anyMatch(slot -> state.getValue(slot) == LampState.FULL);
    }

    /**
     * Whether the lectern is effectively on: switched on <em>and</em> holding a
     * deck with cards.  Everything that shows or applies its effect checks
     * this, so a stale {@code active=true} without cards (e.g. from an older
     * world) never lights up or counts.
     */
    public static boolean isLit(BlockState state) {
        return state.getValue(ACTIVE) && hasCards(state);
    }

    /** Switches the lectern off once no deck has cards left (it never switches itself on). */
    public static BlockState switchOffIfEmpty(BlockState state) {
        return hasCards(state) ? state : state.setValue(ACTIVE, false);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        // Nothing can sit on a lectern (see DeckLecternStackingMixin), so it
        // can't go under a block either: it fills its whole column.
        if (!ctx.getLevel().getBlockState(ctx.getClickedPos().above()).canBeReplaced()) return null;
        return defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DeckLecternBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** Right-click opens the 3-slot screen; Shift + right-click switches it on (needs cards) or off. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            boolean active = !state.getValue(ACTIVE);
            if (active && !hasCards(state)) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_HIT, SoundSource.BLOCKS, 0.6f, 0.5f);
                return InteractionResult.CONSUME;
            }
            level.setBlock(pos, state.setValue(ACTIVE, active), Block.UPDATE_ALL);
            level.playSound(null, pos, active ? SoundEvents.AMETHYST_BLOCK_CHIME : SoundEvents.AMETHYST_BLOCK_HIT,
                    SoundSource.BLOCKS, 1.0f, active ? 1.2f : 0.8f);
            return InteractionResult.CONSUME;
        }
        var factory = state.getMenuProvider(level, pos);
        if (factory != null) {
            player.openMenu(factory);
        }
        return InteractionResult.CONSUME;
    }

    /** Client-side ticker for the runes; nothing ticks on the server. */
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) return null;
        return createTickerHelper(type, EnchantTransferMod.DECK_LECTERN_BLOCK_ENTITY,
                (lvl, pos, st, be) -> spawnRunes(lvl, pos, st, lvl.getRandom()));
    }

    /**
     * While lit, sends the same runes vanilla bookshelves send to a nearby
     * enchanting table (same positions and air gap).  Runs every client tick
     * — unlike animateTick, which only reaches randomly sampled blocks — so
     * one in {@value #RUNE_CHANCE} ticks gives about one rune every 1.5 s per lectern
     * (several lecterns around a table add up, like bookshelves).
     */
    private static void spawnRunes(Level level, BlockPos pos, BlockState state, RandomSource random) {
        if (!isLit(state) || random.nextInt(RUNE_CHANCE) != 0) return;
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            BlockPos table = pos.subtract(offset);
            if (!level.getBlockState(table).is(Blocks.ENCHANTING_TABLE)) continue;
            BlockPos between = table.offset(offset.getX() / 2, offset.getY() / 2, offset.getZ() / 2);
            if (!level.getBlockState(between).is(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) continue;
            level.addParticle(ParticleTypes.ENCHANT,
                    table.getX() + 0.5, table.getY() + 2.0, table.getZ() + 0.5,
                    offset.getX() + random.nextFloat() - 0.5,
                    offset.getY() - random.nextFloat() - 1.0,
                    offset.getZ() + random.nextFloat() - 0.5);
        }
    }

    @Override
    public void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean moved) {
        if (!state.is(this) || moved) {
            super.affectNeighborsAfterRemoval(state, level, pos, moved);
            return;
        }
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof Container inventory) {
            Containers.dropContents(level, pos, inventory);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, moved);
    }
}
