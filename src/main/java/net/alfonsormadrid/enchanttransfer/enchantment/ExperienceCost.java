package net.alfonsormadrid.enchanttransfer.enchantment;

import net.minecraft.world.entity.player.Player;

/**
 * Charges experience in (possibly fractional) levels, measured from where the player
 * stands: half a level at level 30 costs more points than at level 5, like vanilla's
 * level curve. Free in creative.
 */
final class ExperienceCost {
    private ExperienceCost() {}

    static boolean canAfford(Player player, double levels) {
        return player.hasInfiniteMaterials() || currentLevels(player) >= levels;
    }

    /** Whole multiples of {@code levelsEach} the player can pay for (no limit in creative). */
    static int affordableUnits(Player player, double levelsEach) {
        return player.hasInfiniteMaterials() ? Integer.MAX_VALUE : (int) Math.floor(currentLevels(player) / levelsEach);
    }

    static void charge(Player player, double levels) {
        if (player.hasInfiniteMaterials() || levels <= 0) {
            return;
        }
        double current = currentLevels(player);
        int points = (int) Math.round(totalPoints(current) - totalPoints(Math.max(0, current - levels)));
        player.giveExperiencePoints(-points);
    }

    private static double currentLevels(Player player) {
        return player.experienceLevel + player.experienceProgress;
    }

    /** Experience points that make up {@code levels} levels, from zero. */
    private static double totalPoints(double levels) {
        int whole = (int) Math.floor(levels);
        double points = 0;
        for (int level = 0; level < whole; level++) {
            points += pointsToNextLevel(level);
        }
        return points + (levels - whole) * pointsToNextLevel(whole);
    }

    /** Same curve as {@code Player.getXpNeededForNextLevel}. */
    private static int pointsToNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        }
        return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
    }
}
