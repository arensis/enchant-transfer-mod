package net.alfonsormadrid.enchanttransfer.item;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
 * <em>nuevos</em> del mod (Harvesting, Momentum, Magnetism, Freezing)
 * todavía no existen como {@code RegistryKey}, así que aparecen comentados
 * en cada tipo hasta que se registren.
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
                    Enchantments.FORTUNE
                    // + Harvesting (mod-custom, pendiente de registrar)
            )),

    RED("red", "combat",
            List.of(
                    Enchantments.SHARPNESS,
                    Enchantments.SMITE,
                    Enchantments.BANE_OF_ARTHROPODS,
                    Enchantments.IMPALING,
                    Enchantments.FIRE_ASPECT,
                    Enchantments.LOOTING,
                    Enchantments.THORNS,
                    Enchantments.SWEEPING_EDGE
                    // + Momentum (mod-custom, pendiente de registrar)
            )),

    YELLOW("yellow", "utility",
            List.of(
                    Enchantments.EFFICIENCY,
                    Enchantments.UNBREAKING,
                    Enchantments.MENDING,
                    Enchantments.SWIFT_SNEAK,
                    Enchantments.INFINITY,
                    Enchantments.POWER,
                    Enchantments.PUNCH
                    // + Magnetism (mod-custom, pendiente de registrar)
            )),

    PURPLE("purple", "arcane",
            List.of(
                    Enchantments.CHANNELING,
                    Enchantments.RIPTIDE,
                    Enchantments.LOYALTY,
                    Enchantments.FROST_WALKER
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
    private final List<RegistryKey<Enchantment>> allowedEnchantments;

    CardType(String colorId, String categoryKey,
             List<RegistryKey<Enchantment>> allowedEnchantments) {
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
    public List<RegistryKey<Enchantment>> allowedEnchantments() {
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
    public static @Nullable CardType forEnchantment(RegistryKey<Enchantment> key) {
        for (CardType t : values()) {
            if (t.allowedEnchantments.contains(key)) return t;
        }
        return null;
    }
}
