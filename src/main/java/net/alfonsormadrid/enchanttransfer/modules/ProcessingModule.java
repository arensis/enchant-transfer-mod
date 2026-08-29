package net.alfonsormadrid.enchanttransfer.modules;

/**
 * Marker for modules that execute non-instant operations with visible progress
 * (furnace-style).
 * <p>
 * The contract is intentionally read-only: the actual tick logic lives in the
 * concrete block entity. This interface only exposes the data needed by the
 * GUI to draw the progress bar without knowing what's being processed.
 */
public interface ProcessingModule extends TransferTableModule {

    int getProgress();

    int getMaxProgress();

    default float getProgressRatio() {
        int max = getMaxProgress();
        return max <= 0 ? 0f : Math.min(1f, (float) getProgress() / max);
    }

    boolean isProcessing();
}
