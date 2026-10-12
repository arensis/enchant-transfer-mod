package net.alfonsormadrid.enchanttransfer.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.List;
import net.alfonsormadrid.enchanttransfer.library.DeckInfluenceView;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Marks the Enchanting Table options the Card Decks changed (see
 * {@link DeckInfluenceView}): a small deck card in the option's top-right
 * corner and an extra tooltip line.  Options the decks lack the reserve to
 * pay for are dimmed and say why in the tooltip (the menu refuses them).
 * <p>
 * Option rows match vanilla: 108×19 at (60, 14 + 19·i).  The corner is free:
 * the glyph text wraps before it and the level cost sits at the bottom.
 */
@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin extends AbstractContainerScreen<EnchantmentMenu> {

    @Unique private static final int OPTION_X = 60, OPTION_Y = 14, OPTION_STEP = 19;
    @Unique private static final int OPTION_W = 108;

    @Unique private static final int DIM = 0xA0101010;

    // Mark: 5×7 card, 2 px from the option's top-right corner
    @Unique private static final int MARK_W = 5, MARK_H = 7, MARK_INSET = 2;
    @Unique private static final int MARK_BORDER = 0xFFD2962A, MARK_FACE = 0xFF8B5CF6, MARK_GEM = 0xFFF6E0FF;

    private EnchantmentScreenMixin(EnchantmentMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, 0, 0);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void enchanttransfer$drawInfluenceMarks(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float delta,
                                                    CallbackInfo ci) {
        for (int option = 0; option < 3; option++) {
            if (!enchanttransfer$isMarked(option)) continue;
            if (enchanttransfer$lacksReserve(option)) {
                int ox = leftPos + OPTION_X, oy = topPos + OPTION_Y + OPTION_STEP * option;
                ctx.fill(ox + 1, oy + 1, ox + OPTION_W - 1, oy + OPTION_STEP - 1, DIM);
            }
            int x = leftPos + OPTION_X + OPTION_W - MARK_INSET - MARK_W;
            int y = topPos + OPTION_Y + OPTION_STEP * option + MARK_INSET;
            ctx.fill(x, y, x + MARK_W, y + MARK_H, MARK_BORDER);
            ctx.fill(x + 1, y + 1, x + MARK_W - 1, y + MARK_H - 1, MARK_FACE);
            ctx.fill(x + 2, y + 3, x + 3, y + 4, MARK_GEM);
        }
    }

    @WrapOperation(method = "extractRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;setComponentTooltipForNextFrame(Lnet/minecraft/client/gui/Font;Ljava/util/List;II)V"))
    private void enchanttransfer$addInfluenceLine(GuiGraphicsExtractor ctx, Font font, List<Component> lines,
                                                  int mouseX, int mouseY, Operation<Void> original) {
        int option = enchanttransfer$optionAt(mouseX, mouseY);
        if (option >= 0 && enchanttransfer$isMarked(option)) {
            lines = new ArrayList<>(lines);
            lines.add(Component.translatable("enchanttransfer.enchanting.deck_influenced")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
            if (enchanttransfer$lacksReserve(option)) {
                lines.add(Component.translatable("enchanttransfer.enchanting.no_reserve")
                        .withStyle(ChatFormatting.RED));
            }
        }
        original.call(ctx, font, lines, mouseX, mouseY);
    }

    @Unique
    private boolean enchanttransfer$isMarked(int option) {
        return menu.costs[option] > 0 && ((DeckInfluenceView) menu).enchanttransfer$isDeckInfluenced(option);
    }

    @Unique
    private boolean enchanttransfer$lacksReserve(int option) {
        return ((DeckInfluenceView) menu).enchanttransfer$lacksReserve(option);
    }

    @Unique
    private int enchanttransfer$optionAt(int mouseX, int mouseY) {
        int dx = mouseX - leftPos - OPTION_X;
        int dy = mouseY - topPos - OPTION_Y;
        if (dx < 0 || dx >= OPTION_W || dy < 0 || dy >= OPTION_STEP * 3) return -1;
        return dy / OPTION_STEP;
    }
}
