package net.alfonsormadrid.enchanttransfer.modules;

import java.util.Optional;
import net.minecraft.resources.Identifier;

/**
 * Snapshot of a module's state for display in the core's hub and nav-row.
 * <p>
 * Modules build a fresh preview each time the core asks for one — the preview
 * never mutates after construction. Keeps the hub UI decoupled from the
 * internal state of each module.
 */
public record ModulePreview(
        Identifier iconTexture,
        Optional<Integer> primaryValue,
        Optional<Integer> maxValue,
        Optional<Float> progress,
        Optional<Integer> statusColor
) {

    public static ModulePreview iconOnly(Identifier icon) {
        return new ModulePreview(icon, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty());
    }

    public static Builder builder(Identifier icon) {
        return new Builder(icon);
    }

    public static final class Builder {
        private final Identifier icon;
        private Integer primaryValue;
        private Integer maxValue;
        private Float progress;
        private Integer statusColor;

        private Builder(Identifier icon) {
            this.icon = icon;
        }

        public Builder value(int current, int max) {
            this.primaryValue = current;
            this.maxValue = max;
            return this;
        }

        public Builder progress(float progress) {
            this.progress = Math.clamp(progress, 0f, 1f);
            return this;
        }

        public Builder statusColor(int rgb) {
            this.statusColor = rgb;
            return this;
        }

        public ModulePreview build() {
            return new ModulePreview(
                    icon,
                    Optional.ofNullable(primaryValue),
                    Optional.ofNullable(maxValue),
                    Optional.ofNullable(progress),
                    Optional.ofNullable(statusColor)
            );
        }
    }
}
