package net.alfonsormadrid.enchanttransfer.item;

import net.alfonsormadrid.enchanttransfer.EnchantTransferMod;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

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

    private ModDataComponents() {
    }

    /** Fuerza la carga de la clase (y por tanto el registro). */
    public static void init() {
    }
}
