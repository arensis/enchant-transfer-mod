package net.alfonsormadrid.enchanttransfer.enchantment;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Optional;

/**
 * The mod's own enchantments. They are data-driven (JSON under
 * {@code data/enchanttransfer/enchantment/}); the code only holds their keys and
 * implements the effects vanilla's effect components can't express.
 */
public final class ModEnchantments {
    public static final ResourceKey<Enchantment> MAGNETISM = key("magnetism");
    public static final ResourceKey<Enchantment> HARVESTING = key("harvesting");

    private ModEnchantments() {}

    public static void init() {
        HarvestingGameRules.init();
        MagnetismHandler.register();
        HarvestingHandler.register();
        TreeHarvestingHandler.register();
        // Saplings per log, from datapacks (reloads with /reload)
        DataResourceLoader.get().registerReloadListener(SaplingRules.ID, SaplingRules::new);
    }

    /** The enchantment's holder in this level, if a datapack hasn't removed it. */
    static Optional<Holder.Reference<Enchantment>> holder(Level level, ResourceKey<Enchantment> key) {
        return level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key);
    }

    /** The enchantment's level on the stack; 0 when absent or removed by a datapack. */
    static int level(Level level, ResourceKey<Enchantment> key, ItemStack stack) {
        return holder(level, key).map(enchantment -> EnchantmentHelper.getItemEnchantmentLevel(enchantment, stack)).orElse(0);
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, path));
    }
}
