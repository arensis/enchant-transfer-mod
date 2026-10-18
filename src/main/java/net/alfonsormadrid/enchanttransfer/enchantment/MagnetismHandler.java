package net.alfonsormadrid.enchanttransfer.enchantment;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Magnetism: items and XP orbs within reach drift towards the player wearing the
 * enchanted metal armour (the highest level worn counts). Sneaking switches it off, and items the player
 * threw themselves are left alone, so dropping something on purpose still works.
 */
final class MagnetismHandler {
    /** Reach in blocks: I = 4, II = 6, III = 8, IV = 10, V = 12. */
    static final int BASE_RADIUS = 2;
    static final int RADIUS_PER_LEVEL = 2;
    private static final double PULL_SPEED = 0.35;
    /** Close enough for vanilla pickup to take over. */
    private static final double MIN_DISTANCE = 0.75;

    private MagnetismHandler() {}

    static void register() {
        ServerTickEvents.END_LEVEL_TICK.register(MagnetismHandler::tick);
    }

    private static void tick(ServerLevel level) {
        var magnetism = ModEnchantments.holder(level, ModEnchantments.MAGNETISM);
        if (magnetism.isEmpty()) {
            return;
        }
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || player.isShiftKeyDown() || !player.isAlive()) {
                continue;
            }
            int enchantLevel = EnchantmentHelper.getEnchantmentLevel(magnetism.get(), player);
            if (enchantLevel > 0) {
                pullTowards(level, player, BASE_RADIUS + RADIUS_PER_LEVEL * enchantLevel);
            }
        }
    }

    private static void pullTowards(ServerLevel level, ServerPlayer player, double radius) {
        Vec3 target = player.position().add(0, player.getBbHeight() / 2, 0);
        AABB area = player.getBoundingBox().inflate(radius);

        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area,
                item -> !item.hasPickUpDelay() && item.getOwner() != player)) {
            pull(item, target, radius);
        }
        for (ExperienceOrb orb : level.getEntitiesOfClass(ExperienceOrb.class, area, Entity::isAlive)) {
            pull(orb, target, radius);
        }
    }

    private static void pull(Entity entity, Vec3 target, double radius) {
        Vec3 offset = target.subtract(entity.position());
        double distance = offset.length();
        if (distance < MIN_DISTANCE || distance > radius) {
            return;
        }
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.5).add(offset.normalize().scale(PULL_SPEED)));
        // Sends the new motion to clients this tick instead of at the next periodic sync.
        entity.hurtMarked = true;
    }
}
