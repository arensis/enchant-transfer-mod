package net.alfonsormadrid.enchanttransfer.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Ítem de carta mágica.
 *
 * <p>Las cartas no son crafteables: la única forma de obtenerlas es
 * extrayendo un encantamiento de un ítem encantado a través de la
 * Transfer Table.  El color de la carta de salida lo determina la
 * categoría del encantamiento extraído (ver {@link CardType}).
 *
 * <p>Una carta puede ser:
 * <ul>
 *   <li><strong>Base/blanco</strong> — {@code cardType == null}: representa
 *       el aspecto neutro / placeholder usado por la GUI antes de saber
 *       qué encantamiento se va a extraer.</li>
 *   <li><strong>Coloreada</strong> — {@code cardType != null}: pertenece a
 *       una de las 6 categorías definidas en {@link CardType}.  La
 *       categoría determina qué encantamientos puede transferir / contener.</li>
 * </ul>
 *
 * <p>El tipo se fija en el constructor — hay un {@code MagicCardItem} por
 * cada color registrado en {@code EnchantTransferMod}, no se almacena en NBT.
 */
public class MagicCardItem extends Item {

    private final @Nullable CardType cardType;

    /** Construye una carta base/blanco (sin categoría). */
    public MagicCardItem(RegistryKey<Item> registryKey) {
        this(registryKey, null);
    }

    /** Construye una carta coloreada que pertenece a {@code cardType}. */
    public MagicCardItem(RegistryKey<Item> registryKey, @Nullable CardType cardType) {
        super(new Item.Settings().registryKey(registryKey).fireproof());
        this.cardType = cardType;
    }

    /**
     * @return el tipo de la carta o {@code null} si es la carta base/blanco.
     *         Útil para validar compatibilidad de encantamientos en la
     *         Transfer Table.
     */
    public @Nullable CardType getCardType() {
        return cardType;
    }

    @Override
    public ActionResult use(World world, PlayerEntity playerEntity, Hand hand) {
        playerEntity.playSound(SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        return ActionResult.SUCCESS;
    }
}
