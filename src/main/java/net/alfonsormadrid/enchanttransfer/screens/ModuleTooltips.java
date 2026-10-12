package net.alfonsormadrid.enchanttransfer.screens;

import net.alfonsormadrid.enchanttransfer.blocks.infusioncoil.InfusionCoilBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Localised hover tooltips for the module sockets / nav dots shared by the
 * Selector, Transfer Table and Infusion Coil screens.
 */
@Environment(EnvType.CLIENT)
public final class ModuleTooltips {

    private ModuleTooltips() {}

    /** "Empty (North)" — no module attached on that face. */
    public static Component empty(Direction dir) {
        return Component.translatable("enchanttransfer.tooltip.module_empty", NavDotRenderer.dirName(dir))
                .withStyle(ChatFormatting.GRAY);
    }

    /** Shown on the hub dot when the coil isn't attached to any Transfer Table. */
    public static Component disconnected() {
        return Component.translatable("enchanttransfer.tooltip.disconnected").withStyle(ChatFormatting.GRAY);
    }

    /** "X / Y XP" tank level. */
    public static MutableComponent xpLevel(int stored, int capacity) {
        return Component.translatable("enchanttransfer.tooltip.xp_level", stored, capacity);
    }

    /**
     * Three-line tooltip for an attached Infusion Coil: name + direction,
     * tank level and processing state.
     *
     * @param coil     the coil's block entity, or {@code null} if it isn't loaded client-side
     * @param isThisOne appends "(this)" when the tooltip is shown from that same coil's screen
     */
    public static List<Component> infusionCoil(Direction dir, @Nullable InfusionCoilBlockEntity coil, boolean isThisOne) {
        MutableComponent title = Component.translatable("module.enchanttransfer.infusion_coil")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(" "))
                .append(Component.translatable("enchanttransfer.tooltip.direction", NavDotRenderer.dirName(dir))
                        .withStyle(ChatFormatting.GRAY));
        if (isThisOne) {
            title.append(Component.literal(" "))
                    .append(Component.translatable("enchanttransfer.tooltip.this_module").withStyle(ChatFormatting.GOLD));
        }

        List<Component> lines = new ArrayList<>(3);
        lines.add(title);
        lines.add(coil != null
                ? xpLevel(coil.getStored(), coil.getCapacity()).withStyle(ChatFormatting.GRAY)
                : Component.literal("?").withStyle(ChatFormatting.GRAY));
        lines.add(coil != null && coil.isProcessing()
                ? Component.translatable("enchanttransfer.tooltip.state.infusing").withStyle(ChatFormatting.YELLOW)
                : Component.translatable("enchanttransfer.tooltip.state.idle").withStyle(ChatFormatting.GREEN));
        return lines;
    }
}
