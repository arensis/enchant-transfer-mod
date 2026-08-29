package net.alfonsormadrid.enchanttransfer.blocks.zincsmelter;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.alfonsormadrid.enchanttransfer.screens.zincsmelter.ZincSmelterScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Block entity for the Zinc Smelter — a standalone furnace-style block
 * that smelts zinc ores and creates brass alloys.
 * <p>
 * 4 slots: input1 (0), input2 (1), fuel (2), output (3).
 * Fuel is limited to lava buckets and blaze rods.
 * Recipes are matched via {@link ZincSmeltingRecipeRegistry}.
 */
public class ZincSmelterBlockEntity extends BlockEntity
        implements SidedInventory, NamedScreenHandlerFactory {

    // ── Slot indices ───────────────────────────────────────────────────────
    public static final int SLOT_INPUT1 = 0;
    public static final int SLOT_INPUT2 = 1;
    public static final int SLOT_FUEL   = 2;
    public static final int SLOT_OUTPUT = 3;
    public static final int INVENTORY_SIZE = 4;

    // ── Fuel whitelist ─────────────────────────────────────────────────────
    private static final Set<Item> VALID_FUELS = Set.of(Items.LAVA_BUCKET, Items.BLAZE_ROD);
    private static final int LAVA_BUCKET_BURN_TIME = 20000; // 1000 seconds — same as vanilla
    private static final int BLAZE_ROD_BURN_TIME   = 2400;  // 120 seconds — same as vanilla

    // ── PropertyDelegate indices ───────────────────────────────────────────
    public static final int PROP_LIT_TIME      = 0;
    public static final int PROP_LIT_DURATION  = 1;
    public static final int PROP_COOK_PROGRESS = 2;
    public static final int PROP_COOK_TOTAL    = 3;
    public static final int PROPERTY_COUNT     = 4;

    // ── NBT keys ───────────────────────────────────────────────────────────
    private static final String NBT_LIT_TIME       = "LitTime";
    private static final String NBT_LIT_DURATION   = "LitDuration";
    private static final String NBT_COOK_PROGRESS  = "CookProgress";
    private static final String NBT_COOK_TOTAL     = "CookTotal";

    // ── State ──────────────────────────────────────────────────────────────
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(INVENTORY_SIZE, ItemStack.EMPTY);
    private int litTime;
    private int litDuration;
    private int cookingProgress;
    private int cookingTotalTime;

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override public int get(int index) {
            return switch (index) {
                case PROP_LIT_TIME      -> litTime;
                case PROP_LIT_DURATION  -> litDuration;
                case PROP_COOK_PROGRESS -> cookingProgress;
                case PROP_COOK_TOTAL    -> cookingTotalTime;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {
            switch (index) {
                case PROP_LIT_TIME      -> litTime = value;
                case PROP_LIT_DURATION  -> litDuration = value;
                case PROP_COOK_PROGRESS -> cookingProgress = value;
                case PROP_COOK_TOTAL    -> cookingTotalTime = value;
            }
        }
        @Override public int size() { return PROPERTY_COUNT; }
    };

    public ZincSmelterBlockEntity(BlockPos pos, BlockState state) {
        super(EnchantTransferMod.ZINC_SMELTER_BLOCK_ENTITY, pos, state);
    }

    // ── Tick ───────────────────────────────────────────────────────────────

    public static void serverTick(World world, BlockPos pos, BlockState state,
                                  ZincSmelterBlockEntity be) {
        boolean wasLit = be.litTime > 0;
        boolean dirty = false;

        // ── FUEL PRESENCE CHECK ──────────────────────────────────────────
        // The Zinc Smelter requires fuel to be PRESENT in the slot at all
        // times.  If the player removes the lava bucket / blaze rod, the
        // smelter immediately stops — no residual burn time.
        boolean hasFuel = isValidFuel(be.items.get(SLOT_FUEL));
        if (!hasFuel && be.litTime > 0) {
            be.litTime = 0;
            be.litDuration = 0;
            dirty = true;
        }

        // Burn down existing fuel
        if (be.litTime > 0) {
            be.litTime--;
            dirty = true;
        }

        // Find matching recipe
        ZincSmeltingRecipeRegistry.Recipe recipe = ZincSmeltingRecipeRegistry.match(
                be.items.get(SLOT_INPUT1), be.items.get(SLOT_INPUT2));

        if (recipe != null && be.canAcceptOutput(recipe) && hasFuel) {
            // Try to ignite if not burning
            if (be.litTime <= 0) {
                be.litTime = be.tryConsumeFuel();
                be.litDuration = be.litTime;
                dirty = true;
            }

            // Only cook while burning
            if (be.litTime > 0) {
                be.cookingTotalTime = recipe.fuelTime();
                be.cookingProgress++;
                if (be.cookingProgress >= be.cookingTotalTime) {
                    be.craftRecipe(recipe);
                    be.cookingProgress = 0;
                }
                dirty = true;
            } else if (be.cookingProgress > 0) {
                be.cookingProgress = 0;
                dirty = true;
            }
        } else {
            // No valid recipe or output full — stop everything immediately
            if (be.cookingProgress > 0) {
                be.cookingProgress = 0;
                dirty = true;
            }
            if (be.litTime > 0) {
                be.litTime = 0;
                be.litDuration = 0;
                dirty = true;
            }
        }

        boolean isLit = be.litTime > 0;
        updateLitState(world, pos, state, isLit, wasLit);

        // Particles while lit
        if (isLit && world instanceof ServerWorld sw) {
            long tick = world.getTime();
            // Smoke from chimney (every ~4 ticks)
            if (tick % 4 == 0) {
                double cx = pos.getX() + 0.5;
                double cy = pos.getY() + 1.5;
                double cz = pos.getZ() + 0.5;
                sw.spawnParticles(ParticleTypes.SMOKE, cx, cy, cz,
                        1, 0.1, 0.1, 0.1, 0.01);
                if (tick % 12 == 0) {
                    sw.spawnParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 0.2, cz,
                            1, 0.05, 0.05, 0.05, 0.005);
                }
            }
            // Flame + lava sparks from the mirilla (every ~3 ticks).
            // The door is on the OPPOSITE face from facing: when
            // facing=NORTH the door faces SOUTH (+Z).
            if (tick % 3 == 0) {
                Direction doorDir = state.get(ZincSmelterBlock.FACING).getOpposite();
                double fx = pos.getX() + 0.5 + doorDir.getOffsetX() * 0.6;
                double fy = pos.getY() + 0.38;
                double fz = pos.getZ() + 0.5 + doorDir.getOffsetZ() * 0.6;
                sw.spawnParticles(ParticleTypes.FLAME, fx, fy, fz,
                        1, 0.06, 0.04, 0.06, 0.005);
                if (tick % 9 == 0) {
                    sw.spawnParticles(ParticleTypes.LAVA, fx, fy, fz,
                            1, 0.04, 0.02, 0.04, 0.0);
                }
            }
        }

        if (dirty) be.markDirty();
    }

    private static void updateLitState(World world, BlockPos pos, BlockState state,
                                       boolean isLit, boolean wasLit) {
        if (isLit != wasLit && world instanceof ServerWorld sw) {
            sw.setBlockState(pos, state.with(ZincSmelterBlock.LIT, isLit));
        }
    }

    private boolean canAcceptOutput(ZincSmeltingRecipeRegistry.Recipe recipe) {
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) return true;
        if (!output.isOf(recipe.result().getItem())) return false;
        return output.getCount() + recipe.result().getCount() <= output.getMaxCount();
    }

    private void craftRecipe(ZincSmeltingRecipeRegistry.Recipe recipe) {
        // Consume inputs
        items.get(SLOT_INPUT1).decrement(1);
        if (recipe.input2() != null) {
            items.get(SLOT_INPUT2).decrement(1);
        }
        // Produce output
        ItemStack output = items.get(SLOT_OUTPUT);
        if (output.isEmpty()) {
            items.set(SLOT_OUTPUT, recipe.result().copy());
        } else {
            output.increment(recipe.result().getCount());
        }
    }

    /**
     * Consumes one unit of fuel and returns its burn time.
     * <p>
     * Unlike vanilla furnaces, the Zinc Smelter does NOT consume the lava
     * bucket — it stays in the slot as a continuous fuel source (like a
     * gas burner).  The burn time returned is just one recipe's worth,
     * so the smelter re-checks the fuel slot every cycle.  Removing the
     * bucket immediately stops the smelter on the next tick.
     * <p>
     * Blaze rods ARE consumed (one per recipe cycle).
     */
    private int tryConsumeFuel() {
        ItemStack fuel = items.get(SLOT_FUEL);
        if (fuel.isEmpty()) return 0;
        if (!VALID_FUELS.contains(fuel.getItem())) return 0;

        if (fuel.isOf(Items.BLAZE_ROD)) {
            fuel.decrement(1);
        }
        // Lava bucket stays in slot — not consumed.
        // Return a short burn window: enough for one recipe cycle + margin.
        // The smelter will re-check fuel presence each cycle.
        return 400; // 20 seconds — covers the longest recipe (brass alloying)
    }

    // ── Fuel check (for slot validation + quickMove) ──────────────────────

    public static boolean isValidFuel(ItemStack stack) {
        return VALID_FUELS.contains(stack.getItem());
    }

    // ── NBT (1.21.11 ReadView/WriteView API) ──────────────────────────────

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Inventories.readData(view, items);
        litTime          = view.getInt(NBT_LIT_TIME, 0);
        litDuration      = view.getInt(NBT_LIT_DURATION, 0);
        cookingProgress  = view.getInt(NBT_COOK_PROGRESS, 0);
        cookingTotalTime = view.getInt(NBT_COOK_TOTAL, 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, items);
        view.putInt(NBT_LIT_TIME, litTime);
        view.putInt(NBT_LIT_DURATION, litDuration);
        view.putInt(NBT_COOK_PROGRESS, cookingProgress);
        view.putInt(NBT_COOK_TOTAL, cookingTotalTime);
    }

    // ── Inventory ──────────────────────────────────────────────────────────

    @Override public int size() { return INVENTORY_SIZE; }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack result = Inventories.splitStack(items, slot, amount);
        if (!result.isEmpty()) markDirty();
        return result;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(items, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
        stack.capCount(getMaxCountPerStack());
        markDirty();
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return world != null
                && world.getBlockEntity(pos) == this
                && player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clear() { items.clear(); }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_FUEL   -> isValidFuel(stack);
            case SLOT_OUTPUT -> false; // output is read-only
            default -> true;          // input1, input2 accept anything
        };
    }

    // ── SidedInventory (hopper automation) ─────────────────────────────────

    private static final int[] INPUT_SLOTS  = {SLOT_INPUT1, SLOT_INPUT2};
    private static final int[] FUEL_SLOTS   = {SLOT_FUEL};
    private static final int[] OUTPUT_SLOTS = {SLOT_OUTPUT};

    @Override
    public int[] getAvailableSlots(Direction side) {
        return switch (side) {
            case DOWN  -> OUTPUT_SLOTS;
            case UP    -> INPUT_SLOTS;
            default    -> FUEL_SLOTS;
        };
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return slot == SLOT_OUTPUT;
    }

    // ── Screen ─────────────────────────────────────────────────────────────

    @Override
    public Text getDisplayName() {
        return Text.translatable("block.enchanttransfer.zinc_smelter");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new ZincSmelterScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    // ── Accessors for client-side rendering ────────────────────────────────

    public boolean isLit() {
        return litTime > 0;
    }
}
