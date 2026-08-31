package net.alfonsormadrid.enchanttransfer.blocks.zincsmelter;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Hard-coded recipe registry for the Zinc Smelter.
 * <p>
 * Three smelting recipes form a linear progression:
 * <ol>
 *   <li>Calcination: Calcite &rarr; Zinc Oxide (&times;2)</li>
 *   <li>Reduction: Zinc Oxide + Coal &rarr; Zinc Sheet</li>
 *   <li>Alloying: Copper Ingot + Zinc Sheet &rarr; Brass Ingot</li>
 * </ol>
 * Each recipe can have 1 or 2 required inputs.
 * The match order is deterministic: 2-input recipes are checked first.
 */
public final class ZincSmeltingRecipeRegistry {

    /**
     * @param input1   required item in slot 0
     * @param input2   required item in slot 1, or {@code null} for single-input recipes
     * @param result   output ItemStack (copied on craft)
     * @param fuelTime ticks to complete this recipe
     */
    public record Recipe(Item input1, @Nullable Item input2, ItemStack result, int fuelTime) {}

    private static final List<Recipe> RECIPES = List.of(
            // ③ Alloying (2-input, checked first)
            new Recipe(Items.COPPER_INGOT, EnchantTransferMod.ZINC_SHEET_ITEM,
                    new ItemStack(EnchantTransferMod.BRASS_INGOT_ITEM, 1), 400),
            // ② Reduction (2-input)
            new Recipe(EnchantTransferMod.ZINC_OXIDE_ITEM, Items.COAL,
                    new ItemStack(EnchantTransferMod.ZINC_SHEET_ITEM, 1), 300),
            // ① Calcination (1-input)
            new Recipe(Items.CALCITE, null,
                    new ItemStack(EnchantTransferMod.ZINC_OXIDE_ITEM, 2), 200)
    );

    /**
     * Returns the first matching recipe, or {@code null} if no recipe matches.
     * Two-input recipes require both slots to hold the correct items; single-input
     * recipes only check slot 0 (slot 1 may be empty or hold anything).
     * Inputs may also be swapped (slot 0 ↔ slot 1).
     */
    @Nullable
    public static Recipe match(ItemStack slot0, ItemStack slot1) {
        for (Recipe r : RECIPES) {
            if (matches(r, slot0, slot1)) return r;
            // Also try swapped inputs for 2-input recipes
            if (r.input2 != null && matches(r, slot1, slot0)) return r;
        }
        return null;
    }

    private static boolean matches(Recipe r, ItemStack slot0, ItemStack slot1) {
        if (!slot0.is(r.input1)) return false;
        if (r.input2 == null) return true;
        return slot1.is(r.input2);
    }

    /** Returns all recipes — used by JEI/EMI integration or tooltip display. */
    public static List<Recipe> allRecipes() {
        return RECIPES;
    }

    private ZincSmeltingRecipeRegistry() {}
}
