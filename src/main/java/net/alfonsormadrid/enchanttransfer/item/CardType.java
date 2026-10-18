package net.alfonsormadrid.enchanttransfer.item;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import net.alfonsormadrid.enchanttransfer.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Categoria de una {@link MagicCardItem}.  Cada tipo de carta:
 * <ul>
 *   <li>Tiene un <strong>color identificador</strong> ({@link #colorId}) que
 *       se usa como sufijo en el path de la textura
 *       ({@code item/magic_card_<colorId>}), key de receta y lang key.</li>
 *   <li>Tiene una <strong>categoría temática</strong> ({@link #categoryKey})
 *       — Protección, Naturaleza, Combate, Utilidad, Arcano, Maldición —
 *       que se traduce vía
 *       {@code enchanttransfer.card_category.<categoryKey>}.</li>
 *   <li>Define una <strong>lista de encantamientos permitidos</strong>
 *       ({@link #allowedEnchantments()}) que servirá a la Transfer Table
 *       para validar qué cartas pueden transferir qué encantamientos.</li>
 * </ul>
 *
 * <p>Mapeo derivado de {@code DESIGN.md} sección "Categorías de
 * encantamientos (por color de cristal/gema)".  Los encantamientos
 * <em>nuevos</em> del mod que ya existen ({@link ModEnchantments}) están en
 * su tipo; los pendientes (Momentum, Freezing) aparecen comentados hasta que
 * se registren.
 */
public enum CardType {

    BLUE("blue", "protection",
            List.of(
                    Enchantments.PROTECTION,
                    Enchantments.FIRE_PROTECTION,
                    Enchantments.BLAST_PROTECTION,
                    Enchantments.PROJECTILE_PROTECTION,
                    Enchantments.FEATHER_FALLING
            )),

    GREEN("green", "nature",
            List.of(
                    Enchantments.RESPIRATION,
                    Enchantments.AQUA_AFFINITY,
                    Enchantments.DEPTH_STRIDER,
                    Enchantments.SILK_TOUCH,
                    Enchantments.FORTUNE,
                    Enchantments.LUCK_OF_THE_SEA,
                    Enchantments.LURE,
                    ModEnchantments.HARVESTING
            )),

    RED("red", "combat",
            List.of(
                    Enchantments.SHARPNESS,
                    Enchantments.SMITE,
                    Enchantments.BANE_OF_ARTHROPODS,
                    Enchantments.IMPALING,
                    Enchantments.LOOTING,
                    Enchantments.THORNS,
                    Enchantments.SWEEPING_EDGE,
                    Enchantments.KNOCKBACK,
                    Enchantments.DENSITY,
                    Enchantments.BREACH,
                    Enchantments.LUNGE,
                    // Bow and crossbow attacks
                    Enchantments.POWER,
                    Enchantments.PUNCH,
                    Enchantments.MULTISHOT,
                    Enchantments.PIERCING
                    // + Momentum (mod-custom, pendiente de registrar)
            )),

    YELLOW("yellow", "utility",
            List.of(
                    // Speed and durability of the tool, weapon or wearer
                    Enchantments.EFFICIENCY,
                    Enchantments.UNBREAKING,
                    Enchantments.QUICK_CHARGE,
                    Enchantments.SWIFT_SNEAK,
                    ModEnchantments.MAGNETISM
            )),

    PURPLE("purple", "arcane",
            List.of(
                    // Magic: lightning, water, wind, ice, fire, souls and experience
                    Enchantments.CHANNELING,
                    Enchantments.RIPTIDE,
                    Enchantments.LOYALTY,
                    Enchantments.FROST_WALKER,
                    Enchantments.WIND_BURST,
                    Enchantments.FIRE_ASPECT,
                    Enchantments.FLAME,
                    Enchantments.INFINITY,
                    Enchantments.MENDING,
                    Enchantments.SOUL_SPEED
                    // + Freezing (mod-custom, pendiente de registrar)
                    // + futuros encantamientos arcanos del mod
            )),

    BLACK("black", "curse",
            List.of(
                    Enchantments.VANISHING_CURSE,
                    Enchantments.BINDING_CURSE
                    // + maldiciones propias del mod cuando se definan
            ));

    private final String colorId;
    private final String categoryKey;
    private final List<ResourceKey<Enchantment>> allowedEnchantments;

    CardType(String colorId, String categoryKey,
             List<ResourceKey<Enchantment>> allowedEnchantments) {
        this.colorId             = colorId;
        this.categoryKey         = categoryKey;
        this.allowedEnchantments = allowedEnchantments;
    }

    /** Suffix used in texture path, registry id and lang keys (e.g. {@code "blue"}). */
    public String colorId() {
        return colorId;
    }

    /**
     * Subkey of {@code enchanttransfer.card_category.<...>} — the lang file
     * resolves it to the user-facing category name (e.g. "Protección").
     */
    public String categoryKey() {
        return categoryKey;
    }

    /**
     * Vanilla enchantments this card category accepts for storage / transfer.
     * Returned list is immutable.  Mod-custom enchantments will be appended
     * here once their registry keys exist.
     */
    public List<ResourceKey<Enchantment>> allowedEnchantments() {
        return allowedEnchantments;
    }

    /**
     * Reverse lookup: which category contains this enchantment?
     * Used by the Transfer Table extraction flow to decide which coloured
     * card variant to produce when stripping a specific enchantment off an
     * item.
     *
     * @return the matching {@code CardType}, or {@code null} if the
     *         enchantment isn't categorised yet (e.g. modded enchantment
     *         from another mod, or a vanilla one we haven't mapped).
     */
    public static @Nullable CardType forEnchantment(Holder<Enchantment> enchantment) {
        return enchantment.unwrapKey().map(CardType::forEnchantment).orElse(null);
    }

    public static @Nullable CardType forEnchantment(ResourceKey<Enchantment> key) {
        for (CardType t : values()) {
            if (t.allowedEnchantments.contains(key)) return t;
        }
        return null;
    }
}
