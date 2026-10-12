package net.alfonsormadrid.enchanttransfer.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
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

    private static final int BAR_WIDTH = 13;

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

    // ── Card data (shared by the Transfer Table, decks and libraries) ──────

    /** El encantamiento de una carta, si es una carta con exactamente uno. */
    public static Optional<Holder<Enchantment>> enchantmentOf(ItemStack stack) {
        if (!(stack.getItem() instanceof MagicCardItem)) return Optional.empty();
        ItemEnchantments enchants = stack.getEnchantments();
        if (enchants.size() != 1) return Optional.empty();
        return enchants.keySet().stream().findFirst();
    }

    /** Nivel del encantamiento de la carta (0 si no tiene). */
    public static int levelOf(ItemStack stack) {
        return enchantmentOf(stack).map(e -> stack.getEnchantments().getLevel(e)).orElse(0);
    }

    /**
     * Valor de una sola carta en puntos: {@code 2^(nivel-1)}.  Es la misma
     * equivalencia que aplica {@code CombineCardService} (dos cartas de
     * nivel N dan una de nivel N+1), así que guardar cartas combinadas o
     * sin combinar vale lo mismo.
     */
    public static int pointsOf(ItemStack stack) {
        int level = levelOf(stack);
        return level <= 0 ? 0 : 1 << Math.clamp(level - 1, 0, 10);
    }

    // ── Wear bar & tooltip (see CardWear) ───────────────────────────────

    @Override
    public boolean isBarVisible(ItemStack card) {
        return CardWear.wornOf(card) > 0;
    }

    @Override
    public int getBarWidth(ItemStack card) {
        int reserve = CardWear.reserveOf(card);
        return reserve <= 0 ? 0 : Math.round(BAR_WIDTH * (float) CardWear.remainingOf(card) / reserve);
    }

    @Override
    public int getBarColor(ItemStack card) {
        int reserve = CardWear.reserveOf(card);
        float left = reserve <= 0 ? 0 : (float) CardWear.remainingOf(card) / reserve;
        return Mth.hsvToRgb(left / 3.0F, 1.0F, 1.0F);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack card, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        if (CardWear.wornOf(card) <= 0) return;
        tooltip.accept(Component.translatable("enchanttransfer.card.reserve",
                CardWear.format(CardWear.remainingOf(card)), CardWear.format(CardWear.reserveOf(card)))
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult use(Level world, Player playerEntity, InteractionHand hand) {
        playerEntity.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER, 1.0F, 1.0F);
        return InteractionResult.SUCCESS;
    }
}
