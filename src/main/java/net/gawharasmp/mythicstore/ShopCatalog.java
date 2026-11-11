package net.gawharasmp.mythicstore;

import org.bukkit.Material;

import java.util.List;

final class ShopCatalog {
    private ShopCatalog() { }

    static final List<ShopProduct> BUNDLES = List.of(
            bundle("viking", "&6&lViking Bundle", "&7فرو وجلد وحديد بطابع فايكنج حقيقي.", Material.NETHERITE_HELMET),
            bundle("warrior", "&f&lWarrior Bundle", "&7طقم الفارس الحديدي وسلاحه ومعوله.", Material.NETHERITE_HELMET),
            bundle("gladiator", "&c&lGladiator Bundle", "&7درع المصارع الروماني والعُرف الأحمر.", Material.NETHERITE_HELMET),
            bundle("shadow", "&5&lShadow Bundle", "&7درع الظلال مع الخنجر ومعول الظلام.", Material.NETHERITE_HELMET),
            bundle("ice", "&b&lIce Bundle", "&7درع الجليد ورمح الصقيع ومعول الجليد.", Material.NETHERITE_HELMET),
            bundle("angel", "&f&lAngel Bundle", "&7درع الملاك والهالة وسلاح الرمح.", Material.NETHERITE_HELMET),
            bundle("royal", "&6&lRoyal Bundle", "&7تاج ملكي وفأس ملكي ومعول المنقب.", Material.NETHERITE_HELMET),
            bundle("demon", "&4&lDemon Bundle", "&7قرون شيطانية وهراوة ومعول كاسر.", Material.NETHERITE_HELMET),
            bundle("dragon", "&c&lDragon Bundle", "&7أقوى طقم: درع التنين وسيفه ومعوله.", Material.NETHERITE_HELMET)
    );

    static final List<ShopProduct> WEAPONS = List.of(
            item("frost_blade", "&bFrost Blade", "&7سيف جليدي يبطئ الأعداء.", Material.DIAMOND_SWORD),
            item("gladiator_weapon", "&cGladiator Gladius", "&7سيف المصارع الروماني.", Material.NETHERITE_SWORD),
            item("viking_weapon", "&6Viking Warhammer", "&7مطرقة حرب فايكنج ثقيلة.", Material.NETHERITE_AXE),
            item("warrior_weapon", "&fWarrior Battleaxe", "&7فأس فارس متوازن للمعارك.", Material.NETHERITE_AXE),
            item("venom_blade", "&2Venom Fang", "&7سيف سام يسبب تأثير السم.", Material.NETHERITE_SWORD),
            item("hunter_crossbow", "&aHunter Crossbow", "&7نشّاب صيد سريع ومطوّر.", Material.CROSSBOW),
            item("spirit_blade", "&fSoulreaper", "&7سيف روحاني يضعف الأعداء.", Material.NETHERITE_SWORD),
            item("shadow_weapon", "&5Shadow Daggers", "&7خناجر الظلال السريعة.", Material.NETHERITE_SWORD),
            item("battle_spear", "&6Battle Spear", "&7رمح حربي بمدى طويل.", Material.TRIDENT),
            item("vital_blade", "&dLifebinder", "&7سيف يضر الأعداء ويساعد على استعادة الصحة.", Material.NETHERITE_SWORD),
            item("ice_weapon", "&bFrost Spear", "&7رمح فولاذي بارد.", Material.TRIDENT),
            item("shadow_blade", "&5Umbral Edge", "&7سيف الظلال يعمي ويضعف الأعداء.", Material.NETHERITE_SWORD),
            item("angel_weapon", "&fAngel Lance", "&7رمح الملاك بالذهب والعاج.", Material.TRIDENT),
            item("flame_blade", "&6Flame Blade", "&7سيف ناري يحرق الأعداء القريبين.", Material.NETHERITE_SWORD),
            item("lightning_blade", "&eStormbreaker", "&7سيف العاصفة يستدعي البرق.", Material.NETHERITE_SWORD),
            item("royal_weapon", "&6Royal War Axe", "&7فأس ملكي بحواف ذهبية.", Material.NETHERITE_AXE),
            item("war_hammer", "&7War Hammer", "&7مطرقة حربية ثقيلة.", Material.NETHERITE_AXE),
            item("demon_weapon", "&4Demon War Mace", "&7هراوة شيطانية من الحديد القاتم.", Material.NETHERITE_AXE),
            item("dragon_blade", "&cWyrmfang", "&7سيف التنين يطلق موجة نارية.", Material.NETHERITE_SWORD),
            item("dragon_weapon", "&cDragon Greatsword", "&7سيف التنين العظيم، سلاح نادر للغاية.", Material.NETHERITE_SWORD)
    );

    static final List<ShopProduct> TOOLS = List.of(
            item("miner_pickaxe", "&bMiner's War Pickaxe", "&7معول تعدين حربي يمنح Haste عند تعدين الخامات.", Material.DIAMOND_PICKAXE),
            item("lumber_axe", "&aLumber War Axe", "&7فأس أخشاب حربي يمنح Haste عند قطع جذوع الأشجار.", Material.DIAMOND_AXE),
            item("warrior_tool", "&fWarrior Miner Pick", "&7معول الفارس بتصميم فولاذي.", Material.NETHERITE_PICKAXE),
            item("viking_tool", "&6Viking Stonebreaker", "&7معول فايكنج بمقبض ملفوف بالجلد.", Material.NETHERITE_PICKAXE),
            item("gladiator_tool", "&cArena Digger Pick", "&7معول المصارع بحافة برونزية.", Material.NETHERITE_PICKAXE),
            item("shadow_tool", "&5Shadow Ore Pick", "&7معول الظلال للفؤوس والخامات.", Material.NETHERITE_PICKAXE),
            item("ice_tool", "&bFrostbreaker Pick", "&7معول الصقيع الفولاذي.", Material.NETHERITE_PICKAXE),
            item("angel_tool", "&fSeraph Pick", "&7معول ملائكي بالعاج والذهب.", Material.NETHERITE_PICKAXE),
            item("royal_tool", "&6Royal Prospector Pick", "&7معول ملكي مزخرف بالذهب.", Material.NETHERITE_PICKAXE),
            item("demon_tool", "&4Demon Breaker Pick", "&7معول شيطاني ثقيل بحواف قرمزية.", Material.NETHERITE_PICKAXE),
            item("dragon_tool", "&cDragonbone Pick", "&7معول التنين النادر.", Material.NETHERITE_PICKAXE)
    );

    private static ShopProduct bundle(String set, String name, String description, Material icon) {
        return new ShopProduct(set, name, description, icon, set + "_helmet",
                ShopProduct.Category.BUNDLES, ShopProduct.Kind.BUNDLE);
    }

    private static ShopProduct item(String id, String name, String description, Material icon) {
        return new ShopProduct(id, name, description, icon, id,
                id.endsWith("_tool") || id.equals("miner_pickaxe") || id.equals("lumber_axe")
                        ? ShopProduct.Category.TOOLS : ShopProduct.Category.WEAPONS,
                ShopProduct.Kind.ITEM);
    }
}
