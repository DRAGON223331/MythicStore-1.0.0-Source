package net.gawharasmp.mythicstore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class MythicStorePlugin extends JavaPlugin implements Listener {
    private static final int[] BUNDLE_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};
    private static final int[] ITEM_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };
    private static final long MIN_PRICE = 300_000L;
    private static final long MAX_PRICE = 1_000_000L;
    private static final int MAX_QUANTITY_HARD_CAP = 4;

    private final NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        if (!getServer().getPluginManager().isPluginEnabled("GawharaItems")) {
            getLogger().severe("GawharaItems is required. Install GawharaItems 1.3.1 or newer before MythicStore.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(this, this);
        PluginCommand command = getCommand("mythicstore");
        if (command == null) {
            getLogger().severe("Command 'mythicstore' is missing from plugin.yml. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        command.setExecutor(this);

        getLogger().info("MythicStore enabled: 9 armor bundles and "
                + (ShopCatalog.WEAPONS.size() + ShopCatalog.TOOLS.size())
                + " GawharaItems weapons/tools are available.");
        if (EconomyHook.find() == null) {
            getLogger().warning("No Vault economy provider was found. Install Vault and an economy plugin (for example EssentialsX Economy).");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mythicstore.admin")) {
                sender.sendMessage(color("&cليس لديك صلاحية.") );
                return true;
            }
            reloadConfig();
            sender.sendMessage(prefix() + color("&aتم تحديث أسعار MythicStore."));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can open MythicStore.");
            return true;
        }
        if (!player.hasPermission("mythicstore.use")) {
            player.sendMessage(prefix() + color("&cليس لديك صلاحية استخدام المتجر."));
            return true;
        }
        if (!getServer().getPluginManager().isPluginEnabled("GawharaItems")) {
            player.sendMessage(prefix() + color("&cإضافة GawharaItems غير مفعلة."));
            return true;
        }
        if (EconomyHook.find() == null) {
            player.sendMessage(prefix() + color("&cنظام الأموال غير متصل. تأكد من تثبيت Vault وإضافة اقتصاد."));
            return true;
        }
        openHome(player);
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof MenuHolder holder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) return;

        int slot = event.getRawSlot();
        switch (holder.page()) {
            case HOME -> handleHomeClick(player, slot);
            case BUNDLES, WEAPONS, TOOLS -> handleListingClick(player, holder, slot);
            case CONFIRM -> handleConfirmClick(player, holder, slot);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof MenuHolder) {
            event.setCancelled(true);
        }
    }

    private void handleHomeClick(Player player, int slot) {
        switch (slot) {
            case 11 -> openListing(player, MenuHolder.Page.BUNDLES);
            case 13 -> openListing(player, MenuHolder.Page.WEAPONS);
            case 15 -> openListing(player, MenuHolder.Page.TOOLS);
            case 22 -> player.sendMessage(prefix() + color("&7كل القطع هنا أصلية من إضافة GawharaItems. الشراء فقط، ولا يوجد بيع عكسي."));
            case 26 -> player.closeInventory();
            default -> { }
        }
    }

    private void handleListingClick(Player player, MenuHolder holder, int slot) {
        if (slot == 45) {
            openHome(player);
            return;
        }
        if (slot == 49) {
            openHome(player);
            return;
        }
        if (slot == 53) {
            player.closeInventory();
            return;
        }
        ShopProduct product = holder.productAt(slot);
        if (product != null) openConfirm(player, product, 1);
    }

    private void handleConfirmClick(Player player, MenuHolder holder, int slot) {
        if (slot == 9) {
            holder.setQuantity(Math.max(1, holder.quantity() - 1));
            openConfirm(player, holder.product(), holder.quantity());
            return;
        }
        if (slot == 15) {
            holder.setQuantity(Math.min(maxQuantity(), holder.quantity() + 1));
            openConfirm(player, holder.product(), holder.quantity());
            return;
        }
        if (slot == 22) {
            purchase(player, holder.product(), holder.quantity());
            return;
        }
        if (slot == 26) {
            player.closeInventory();
            return;
        }
        if (slot == 18) {
            MenuHolder.Page returnPage = holder.product().category() == ShopProduct.Category.BUNDLES
                    ? MenuHolder.Page.BUNDLES
                    : holder.product().category() == ShopProduct.Category.WEAPONS
                    ? MenuHolder.Page.WEAPONS : MenuHolder.Page.TOOLS;
            openListing(player, returnPage);
        }
    }

    private void openHome(Player player) {
        MenuHolder holder = new MenuHolder(MenuHolder.Page.HOME);
        Inventory inventory = Bukkit.createInventory(holder, 27, color("&8✦ &6&lMythicStore &8✦"));
        holder.setInventory(inventory);
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE);

        inventory.setItem(11, button(Material.NETHERITE_CHESTPLATE, "&6&lARMOR BUNDLES",
                "&7تسعة أطقم أسطورية كاملة.", "&eالسعر: &f300,000$ &7- &f1,000,000$", "&aاضغط لعرض الباندلات"));
        inventory.setItem(13, button(Material.NETHERITE_SWORD, "&c&lWEAPONS & SWORDS",
                "&7السيوف والأسلحة المميزة.", "&eالأسعار مرتبة من الأقل للأعلى.", "&aاضغط لعرض الأسلحة"));
        inventory.setItem(15, button(Material.NETHERITE_PICKAXE, "&b&lPICKAXES & AXES",
                "&7كل المعاول والفؤوس المخصصة.", "&eالأسعار مرتبة من الأقل للأعلى.", "&aاضغط لعرض الأدوات"));
        inventory.setItem(22, button(Material.BOOK, "&fمعلومات المتجر",
                "&7كل قطعة تُسلّم من إضافة GawharaItems.", "&7كل الباندل يحتوي 4 قطع درع + سلاح + معول.",
                "&7الشراء فقط ولا يوجد بيع عكسي.", "&8الحد الأدنى 300,000$ والأعلى 1,000,000$"));
        inventory.setItem(26, button(Material.BARRIER, "&cإغلاق", "&7إغلاق MythicStore"));
        player.openInventory(inventory);
    }

    private void openListing(Player player, MenuHolder.Page page) {
        List<ShopProduct> products = productsFor(page);
        products.sort(Comparator.comparingLong(this::price).thenComparing(ShopProduct::id));

        MenuHolder holder = new MenuHolder(page);
        String pageName = switch (page) {
            case BUNDLES -> "&6Bundles";
            case WEAPONS -> "&cWeapons & Swords";
            case TOOLS -> "&bPickaxes & Axes";
            default -> "&6MythicStore";
        };
        Inventory inventory = Bukkit.createInventory(holder, 54,
                color("&8MythicStore &7» " + pageName));
        holder.setInventory(inventory);
        fill(inventory, Material.BLACK_STAINED_GLASS_PANE);

        int[] slots = page == MenuHolder.Page.BUNDLES ? BUNDLE_SLOTS : ITEM_SLOTS;
        for (int index = 0; index < products.size() && index < slots.length; index++) {
            ShopProduct product = products.get(index);
            int slot = slots[index];
            inventory.setItem(slot, productIcon(product, price(product)));
            holder.addProduct(slot, product);
        }

        inventory.setItem(45, button(Material.ARROW, "&fرجوع", "&7العودة للقائمة الرئيسية"));
        inventory.setItem(49, button(Material.NETHER_STAR, "&6&lMythicStore", "&7القائمة الرئيسية"));
        inventory.setItem(53, button(Material.BARRIER, "&cإغلاق", "&7إغلاق المتجر"));
        player.openInventory(inventory);
    }

    private void openConfirm(Player player, ShopProduct product, int quantity) {
        int normalizedQuantity = Math.max(1, Math.min(maxQuantity(), quantity));
        MenuHolder holder = new MenuHolder(MenuHolder.Page.CONFIRM, product, normalizedQuantity);
        Inventory inventory = Bukkit.createInventory(holder, 27,
                color("&8شراء &7» " + product.name()));
        holder.setInventory(inventory);
        fill(inventory, Material.GRAY_STAINED_GLASS_PANE);

        long unitPrice = price(product);
        long total = unitPrice * normalizedQuantity;
        int requiredSlots = product.inventorySlotsPerPurchase() * normalizedQuantity;

        inventory.setItem(11, productIcon(product, unitPrice));
        inventory.setItem(9, button(Material.RED_CONCRETE, "&c- تقليل الكمية", "&7الكمية الحالية: &f" + normalizedQuantity));
        inventory.setItem(13, button(Material.PAPER, "&e&lالكمية: &f" + normalizedQuantity,
                "&7سعر القطعة/الباندل: &6$" + money(unitPrice),
                "&7الإجمالي: &a$" + money(total),
                "&7خانات مطلوبة في الحقيبة: &f" + requiredSlots));
        inventory.setItem(15, button(Material.LIME_CONCRETE, "&a+ زيادة الكمية", "&7الحد الأقصى: &f" + maxQuantity()));
        inventory.setItem(18, button(Material.ARROW, "&fرجوع للمتجر", "&7إلغاء والعودة للمنتجات"));
        inventory.setItem(22, button(Material.EMERALD_BLOCK, "&a&lتأكيد الشراء",
                "&7الإجمالي: &6$" + money(total), "&7الكمية: &f" + normalizedQuantity,
                "&eاضغط لإتمام عملية الشراء"));
        inventory.setItem(26, button(Material.BARRIER, "&cإغلاق", "&7إغلاق المتجر"));
        player.openInventory(inventory);
    }

    private void purchase(Player player, ShopProduct product, int quantity) {
        if (!getServer().getPluginManager().isPluginEnabled("GawharaItems")) {
            player.sendMessage(prefix() + color("&cإضافة GawharaItems غير متاحة. لم يتم خصم أي أموال."));
            player.closeInventory();
            return;
        }
        EconomyHook economy = EconomyHook.find();
        if (economy == null) {
            player.sendMessage(prefix() + color("&cتعذر الاتصال بنظام الاقتصاد. لم يتم خصم أي أموال."));
            player.closeInventory();
            return;
        }

        int safeQuantity = Math.max(1, Math.min(maxQuantity(), quantity));
        int requiredSlots = product.inventorySlotsPerPurchase() * safeQuantity;
        int freeSlots = countFreeStorageSlots(player);
        if (freeSlots < requiredSlots) {
            player.sendMessage(prefix() + color("&cمساحة الحقيبة غير كافية. تحتاج إلى " + requiredSlots
                    + " خانات فارغة، والمتاح " + freeSlots + "."));
            return;
        }

        long unitPrice = price(product);
        long total;
        try {
            total = Math.multiplyExact(unitPrice, safeQuantity);
        } catch (ArithmeticException exception) {
            player.sendMessage(prefix() + color("&cحدث خطأ في حساب السعر."));
            return;
        }

        try {
            if (!economy.has(player, total)) {
                double balance = economy.balance(player);
                player.sendMessage(prefix() + color("&cرصيدك غير كافٍ. السعر: &6$" + money(total)
                        + " &c| رصيدك: &e$" + money((long) balance)));
                return;
            }
            if (!economy.withdraw(player, total)) {
                player.sendMessage(prefix() + color("&cتعذر خصم المبلغ من حسابك. لم يتم تسليم العنصر."));
                return;
            }
        } catch (ReflectiveOperationException exception) {
            getLogger().severe("Vault economy call failed: " + exception.getMessage());
            player.sendMessage(prefix() + color("&cحدث خطأ في نظام الأموال. تواصل مع الإدارة."));
            return;
        }

        boolean granted = grantProduct(player, product, safeQuantity);
        if (!granted) {
            try {
                economy.deposit(player, total);
                player.sendMessage(prefix() + color("&cفشل تسليم العنصر، وتم رد المبلغ بالكامل."));
            } catch (ReflectiveOperationException exception) {
                getLogger().severe("Could not refund failed MythicStore purchase for " + player.getName()
                        + ": " + exception.getMessage());
                player.sendMessage(prefix() + color("&cفشل التسليم ورد المبلغ تلقائيًا. يرجى التواصل مع الإدارة فورًا."));
            }
            return;
        }

        player.closeInventory();
        player.sendMessage(prefix() + color("&aتم الشراء بنجاح: &f" + ChatColor.stripColor(color(product.name()))
                + " &7× &f" + safeQuantity + " &7| المدفوع: &6$" + money(total)));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.15f);
    }

    private boolean grantProduct(Player player, ShopProduct product, int quantity) {
        if (product.isBundle()) {
            for (int index = 0; index < quantity; index++) {
                if (!Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        "gitems bundle " + player.getName() + " " + product.id())) {
                    return false;
                }
            }
            return true;
        }
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                "gitems give " + player.getName() + " " + product.id() + " " + quantity);
    }

    private List<ShopProduct> productsFor(MenuHolder.Page page) {
        return new ArrayList<>(switch (page) {
            case BUNDLES -> ShopCatalog.BUNDLES;
            case WEAPONS -> ShopCatalog.WEAPONS;
            case TOOLS -> ShopCatalog.TOOLS;
            default -> List.of();
        });
    }

    private ItemStack productIcon(ShopProduct product, long price) {
        ItemStack item = new ItemStack(product.iconMaterial());
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(product.name()));
            try {
                meta.setItemModel(new NamespacedKey("gawharaitems", product.modelId()));
            } catch (IllegalArgumentException exception) {
                getLogger().warning("Invalid model key for " + product.id() + ": " + exception.getMessage());
            }
            List<String> lore = new ArrayList<>();
            lore.add(color("&8━━━━━━━━━━━━━━━━"));
            lore.add(color(product.description()));
            lore.add(" ");
            lore.add(color("&eالسعر: &6$" + money(price)));
            lore.add(color("&7الكمية: &fاختر من شاشة الشراء"));
            if (product.isBundle()) {
                lore.add(color("&7المحتويات: &f4 قطع درع + سلاح + معول"));
                lore.add(color("&7الطقم: &f" + product.id().toUpperCase(Locale.ROOT)));
            } else {
                lore.add(color("&7معرّف القطعة: &8" + product.id()));
            }
            lore.add(" ");
            lore.add(color("&aاضغط لاختيار الكمية والشراء"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack button(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(color(name));
            List<String> lines = new ArrayList<>();
            for (String line : lore) lines.add(color(line));
            meta.setLore(lines);
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inventory, Material material) {
        ItemStack pane = button(material, " ");
        for (int slot = 0; slot < inventory.getSize(); slot++) inventory.setItem(slot, pane);
    }

    private long price(ShopProduct product) {
        String path = product.isBundle()
                ? "prices.bundles." + product.id()
                : "prices.items." + product.id();
        long configured = getConfig().getLong(path, MIN_PRICE);
        if (configured < MIN_PRICE || configured > MAX_PRICE) {
            getLogger().warning("Price for " + product.id() + " must be between " + MIN_PRICE + " and "
                    + MAX_PRICE + ". It has been clamped to that range.");
            return Math.max(MIN_PRICE, Math.min(MAX_PRICE, configured));
        }
        return configured;
    }

    private int maxQuantity() {
        return Math.max(1, Math.min(MAX_QUANTITY_HARD_CAP,
                getConfig().getInt("settings.max-quantity", MAX_QUANTITY_HARD_CAP)));
    }

    private int countFreeStorageSlots(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType().isAir()) count++;
        }
        return count;
    }

    private String money(long amount) {
        return numberFormat.format(amount);
    }

    private String prefix() {
        return color(getConfig().getString("settings.prefix", "&8[&6MythicStore&8] &7"));
    }

    private String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text == null ? "" : text);
    }
}
