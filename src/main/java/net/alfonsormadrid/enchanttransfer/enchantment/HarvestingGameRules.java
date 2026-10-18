package net.alfonsormadrid.enchanttransfer.enchantment;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

/**
 * Per-world experience costs of Harvesting, in levels (0 makes it free). Set with
 * {@code /gamerule enchanttransfer:harvesting_field_xp_cost 1.0} or on world creation.
 */
public final class HarvestingGameRules {
    /** Levels per started 9×9 of a whole-field harvest (doubled with bone meal at IV). */
    public static final GameRule<Double> FIELD_XP_COST = register("harvesting_field_xp_cost", 0.5);
    /** Levels per whole-tree felling. */
    public static final GameRule<Double> TREE_XP_COST = register("harvesting_tree_xp_cost", 0.5);

    private HarvestingGameRules() {}

    /** Loads the class, which registers the rules. */
    static void init() {}

    static double get(ServerLevel level, GameRule<Double> rule) {
        return level.getGameRules().get(rule);
    }

    private static GameRule<Double> register(String path, double defaultLevels) {
        return GameRuleBuilder.forDouble(defaultLevels)
                .minValue(0.0)
                .category(GameRuleCategory.PLAYER)
                .buildAndRegister(Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, path));
    }
}
