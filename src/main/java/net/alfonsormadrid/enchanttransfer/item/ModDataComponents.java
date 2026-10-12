package net.alfonsormadrid.enchanttransfer.item;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

/**
 * Data components propios del mod.  Se registran al cargar la clase, que
 * ocurre cuando el primer ítem que los usa construye sus {@code Properties}
 * (dentro de la inicialización del mod, con los registros aún abiertos).
 */
public final class ModDataComponents {

    public static final DataComponentType<DeckContents> DECK_CONTENTS = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "deck_contents"),
            DataComponentType.<DeckContents>builder()
                    .persistent(DeckContents.CODEC)
                    .networkSynchronized(DeckContents.STREAM_CODEC)
                    .cacheEncoding()
                    .build());

    /**
     * Wear a Magic Card has taken in a Card Deck, in {@link CardWear#UNITS_PER_POINT}
     * units of card points.  Absent on unworn cards.
     */
    public static final DataComponentType<Integer> CARD_WEAR = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(EnchantTransferMod.MOD_ID, "card_wear"),
            DataComponentType.<Integer>builder()
                    .persistent(ExtraCodecs.POSITIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    private ModDataComponents() {
    }

    /** Fuerza la carga de la clase (y por tanto el registro). */
    public static void init() {
    }
}
