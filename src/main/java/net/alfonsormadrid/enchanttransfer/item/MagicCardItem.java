package net.alfonsormadrid.enchanttransfer.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
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
    public MagicCardItem(ResourceKey<Item> registryKey) {
        this(registryKey, null);
    }

    /** Construye una carta coloreada que pertenece a {@code cardType}. */
    public MagicCardItem(ResourceKey<Item> registryKey, @Nullable CardType cardType) {
        super(new Item.Properties().setId(registryKey).fireResistant());
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
    public InteractionResult use(Level world, Player playerEntity, InteractionHand hand) {
        playerEntity.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }
}
