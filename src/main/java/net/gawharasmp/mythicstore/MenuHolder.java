package net.gawharasmp.mythicstore;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

final class MenuHolder implements InventoryHolder {
    enum Page { HOME, BUNDLES, WEAPONS, TOOLS, CONFIRM }

    private final Page page;
    private final ShopProduct product;
    private int quantity;
    private Inventory inventory;
    private final Map<Integer, ShopProduct> productSlots = new HashMap<>();

    MenuHolder(Page page) {
        this(page, null, 1);
    }

    MenuHolder(Page page, ShopProduct product, int quantity) {
        this.page = page;
        this.product = product;
        this.quantity = quantity;
    }

    Page page() { return page; }
    ShopProduct product() { return product; }
    int quantity() { return quantity; }
    void setQuantity(int quantity) { this.quantity = quantity; }
    void setInventory(Inventory inventory) { this.inventory = inventory; }
    void addProduct(int slot, ShopProduct product) { productSlots.put(slot, product); }
    ShopProduct productAt(int slot) { return productSlots.get(slot); }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
