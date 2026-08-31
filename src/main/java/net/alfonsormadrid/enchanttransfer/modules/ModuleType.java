package net.alfonsormadrid.enchanttransfer.modules;

import net.minecraft.resources.Identifier;

/**
 * Identifies a kind of module that can be attached to the Transfer Table core.
 * <p>
 * Used by the network registry to know what's connected without depending on
 * concrete classes — this keeps the core open for extension (new modules can
 * be added without touching existing code).
 */
public record ModuleType(Identifier id) {

    public String getTranslationKey() {
        return "module." + id.getNamespace() + "." + id.getPath();
    }
}
