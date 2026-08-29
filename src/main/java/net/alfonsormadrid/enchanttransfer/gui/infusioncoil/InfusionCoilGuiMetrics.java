package net.alfonsormadrid.enchanttransfer.gui.infusioncoil;

import net.alfonsormadrid.enchanttransfer.gui.common.SlotPosition;

/**
 * Non-slot GUI element coordinates and sizes for the Infusion Coil screen.
 * Kept separate from {@link InfusionCoilSlotPositions} because slots have
 * their own structural meaning (interactive inventory cells) while these are
 * pure renderable widgets.
 */
public class InfusionCoilGuiMetrics {
    public static final int BACKGROUND_WIDTH  = 176;
    public static final int BACKGROUND_HEIGHT = 200;

    public static final SlotPosition progressArrow = new SlotPosition(56, 42);
    public static final int PROGRESS_WIDTH  = 22;
    public static final int PROGRESS_HEIGHT = 8;

    public static final SlotPosition bottleTap = new SlotPosition(52, 68);
    public static final int TAP_WIDTH  = 12;
    public static final int TAP_HEIGHT = 12;

    public static final SlotPosition xpTank = new SlotPosition(130, 32);
    public static final int TANK_WIDTH  = 14;
    public static final int TANK_HEIGHT = 56;

    public static final SlotPosition playerInventory = new SlotPosition(8, 118);
    public static final SlotPosition playerHotbar    = new SlotPosition(8, 176);
}
