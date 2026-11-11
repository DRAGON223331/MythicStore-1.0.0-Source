package net.gawharasmp.mythicstore;

import org.bukkit.Material;

/** A catalogue entry. IDs intentionally match the public IDs in GawharaItems 1.3.1. */
public record ShopProduct(
        String id,
        String name,
        String description,
        Material iconMaterial,
        String modelId,
        Category category,
        Kind kind
) {
    public enum Category { BUNDLES, WEAPONS, TOOLS }
    public enum Kind { BUNDLE, ITEM }

    public boolean isBundle() {
        return kind == Kind.BUNDLE;
    }

    public int inventorySlotsPerPurchase() {
        // GawharaItems /gitems bundle grants four armor pieces, one signature weapon and one pickaxe.
        return isBundle() ? 6 : 1;
    }
}
