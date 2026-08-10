package com.CoffeDino.lunacy.item;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.item.Custom.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, Lunacy.MODID);

    public static final DeferredHolder<Item, Item> CUMMINGTONITE = ITEMS.register("cummingtonite",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> CUMMINGTONITE_INGOT = ITEMS.register("cummingtonite_ingot",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, Item> STORAGE_GEM = ITEMS.register("storage_gem",
            () -> new Item(new Item.Properties()));
    public static final DeferredHolder<Item, Item> STACK_STAR = ITEMS.register("stack_star",
            () -> new Item(new Item.Properties()));

    public static final DeferredHolder<Item, WandItem> THE_WAND = ITEMS.register("the_wand",
            () -> new WandItem(new Item.Properties().durability(200)));
    public static final DeferredHolder<Item, SpecialWandItem> SPECIAL_WAND = ITEMS.register("special_wand",
            () -> new SpecialWandItem(new Item.Properties().durability(10)));
    public static final DeferredHolder<Item, RaceResetScrollItem> RACE_RESET_SCROLL = ITEMS.register("race_reset_scroll",
            () -> new RaceResetScrollItem(new Item.Properties().stacksTo(1)));
    public static final DeferredHolder<Item, GunItem> LAMENT_GUN = ITEMS.register("lament_gun",
            () -> new GunItem(new Item.Properties().durability(500)));

    public static final DeferredHolder<Item, BulletItem> LAMENT_BULLET = ITEMS.register("lament_bullet",
            () -> new BulletItem(new Item.Properties().stacksTo(64)));
    public static final DeferredHolder<Item, ViridyumGreatswordItem> VIRIDYUM_GREATSWORD = ITEMS.register("viridyum_greatsword",
            () -> new ViridyumGreatswordItem(Tiers.NETHERITE, new Item.Properties().durability(2031).fireResistant()));
    public static final DeferredHolder<Item, FireSpearItem> AGNIS_FURY = ITEMS.register("agnis_fury",
            () -> new FireSpearItem(Tiers.NETHERITE, new Item.Properties().durability(2031).fireResistant()));
    public static final DeferredHolder<Item, SoulScytheItem> SOUL_SCYTHE = ITEMS.register("soul_scythe",
            () -> new SoulScytheItem(Tiers.NETHERITE, new Item.Properties().durability(2031).fireResistant()));

    public static final DeferredHolder<Item, ShiBowItem> SHI_BOW = ITEMS.register("shi_bow",
            () -> new ShiBowItem(new Item.Properties().durability(2000)));
    public static final DeferredHolder<Item, ShiArrowItem> SHI_ARROW = ITEMS.register("shi_arrow",
            () -> new ShiArrowItem(new Item.Properties()));
    public static final DeferredHolder<Item, AmethystRapierItem> AMETHYST_RAPIER = ITEMS.register("amethyst_rapier",
            () -> new AmethystRapierItem(Tiers.NETHERITE, new Item.Properties().durability(2031)));
    public static final DeferredHolder<Item, ObsidiaItem> OBSIDIA = ITEMS.register("obsidia",
            () -> new ObsidiaItem(Tiers.NETHERITE, new Item.Properties().durability(2031)));
    public static final DeferredHolder<Item, BorontItem> BORONT_AXE = ITEMS.register("boront_axe",
            () -> new BorontItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(AxeItem.createAttributes(Tiers.NETHERITE, 9.0F, -3.0F))
                            .durability(2031)
                            .fireResistant()));
    public static final DeferredHolder<Item, RocaItem> ROCA = ITEMS.register("roca",
            () -> new RocaItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 5.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));
    public static final DeferredHolder<Item, GruckItem> GRUCK = ITEMS.register("gruck",
            () -> new GruckItem(new Item.Properties().durability(10000)));
    public static final DeferredHolder<Item, CharybdisItem> CHARYBDIS = ITEMS.register("charybdis",
            () -> new CharybdisItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));
    public static final DeferredHolder<Item, HeliosItem> HELIOS = ITEMS.register("helios",
            () -> new HeliosItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, JoroItem> JORO = ITEMS.register("joro",
            () -> new JoroItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, ErinyesItem> ERINYES = ITEMS.register("erinyes",
            () -> new ErinyesItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, MoiraiItem> MOIRAI = ITEMS.register("moirai",
            () -> new MoiraiItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, BoreasItem> BOREAS = ITEMS.register("boreas",
            () -> new BoreasItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, PhaetonItem> PHAETON = ITEMS.register("phaeton",
            () -> new PhaetonItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, PerunItem> PERUN = ITEMS.register("perun",
            () -> new PerunItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));

    public static final DeferredHolder<Item, AmphitriteItem> AMPHITRITE = ITEMS.register("amphitrite",
            () -> new AmphitriteItem(Tiers.NETHERITE,
                    new Item.Properties()
                            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 4.0F, -2.4F))
                            .durability(2031)
                            .fireResistant()));
    // --- Daggers ---
    public static final DeferredHolder<Item, DaggerItem> WOOD_DAGGER = ITEMS.register("wood_dagger",
            () -> new DaggerItem(Tiers.WOOD, 2.0f, -1.6f, new Item.Properties().durability(59)));
    public static final DeferredHolder<Item, DaggerItem> STONE_DAGGER = ITEMS.register("stone_dagger",
            () -> new DaggerItem(Tiers.STONE, 2.6f, -1.6f, new Item.Properties().durability(131)));
    public static final DeferredHolder<Item, DaggerItem> IRON_DAGGER = ITEMS.register("iron_dagger",
            () -> new DaggerItem(Tiers.IRON, 3.0f, -1.6f, new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, DaggerItem> GOLD_DAGGER = ITEMS.register("gold_dagger",
            () -> new DaggerItem(Tiers.GOLD, 2.2f, -1.6f, new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, DaggerItem> DIAMOND_DAGGER = ITEMS.register("diamond_dagger",
            () -> new DaggerItem(Tiers.DIAMOND, 3.6f, -1.6f, new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, DaggerItem> NETHERITE_DAGGER = ITEMS.register("netherite_dagger",
            () -> new DaggerItem(Tiers.NETHERITE, 4.2f, -1.6f, new Item.Properties().durability(2031).fireResistant()));

    // --- Rapiers ---
    public static final DeferredHolder<Item, RapierItem> WOOD_RAPIER = ITEMS.register("wood_rapier",
            () -> new RapierItem(Tiers.WOOD, 2.6f, -1.0f, new Item.Properties().durability(59)));
    public static final DeferredHolder<Item, RapierItem> STONE_RAPIER = ITEMS.register("stone_rapier",
            () -> new RapierItem(Tiers.STONE, 3.4f, -1.0f, new Item.Properties().durability(131)));
    public static final DeferredHolder<Item, RapierItem> IRON_RAPIER = ITEMS.register("iron_rapier",
            () -> new RapierItem(Tiers.IRON, 4.0f, -1.0f, new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, RapierItem> GOLD_RAPIER = ITEMS.register("gold_rapier",
            () -> new RapierItem(Tiers.GOLD, 3.0f, -1.0f, new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, RapierItem> DIAMOND_RAPIER = ITEMS.register("diamond_rapier",
            () -> new RapierItem(Tiers.DIAMOND, 4.8f, -1.0f, new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, RapierItem> NETHERITE_RAPIER = ITEMS.register("netherite_rapier",
            () -> new RapierItem(Tiers.NETHERITE, 5.6f, -1.0f, new Item.Properties().durability(2031).fireResistant()));

    // --- Greatswords ---
    public static final DeferredHolder<Item, GreatswordItem> WOOD_GREATSWORD = ITEMS.register("wood_greatsword",
            () -> new GreatswordItem(Tiers.WOOD, 4.5f, -3.2f, new Item.Properties().durability(59)));
    public static final DeferredHolder<Item, GreatswordItem> STONE_GREATSWORD = ITEMS.register("stone_greatsword",
            () -> new GreatswordItem(Tiers.STONE, 6.0f, -3.2f, new Item.Properties().durability(131)));
    public static final DeferredHolder<Item, GreatswordItem> IRON_GREATSWORD = ITEMS.register("iron_greatsword",
            () -> new GreatswordItem(Tiers.IRON, 7.0f, -3.2f, new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, GreatswordItem> GOLD_GREATSWORD = ITEMS.register("gold_greatsword",
            () -> new GreatswordItem(Tiers.GOLD, 5.2f, -3.2f, new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, GreatswordItem> DIAMOND_GREATSWORD = ITEMS.register("diamond_greatsword",
            () -> new GreatswordItem(Tiers.DIAMOND, 8.4f, -3.2f, new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, GreatswordItem> NETHERITE_GREATSWORD = ITEMS.register("netherite_greatsword",
            () -> new GreatswordItem(Tiers.NETHERITE, 9.8f, -3.2f, new Item.Properties().durability(2031).fireResistant()));

    // --- Scythes ---
    public static final DeferredHolder<Item, ScytheItem> WOOD_SCYTHE = ITEMS.register("wood_scythe",
            () -> new ScytheItem(Tiers.WOOD, 3.3f, -2.6f, new Item.Properties().durability(59)));
    public static final DeferredHolder<Item, ScytheItem> STONE_SCYTHE = ITEMS.register("stone_scythe",
            () -> new ScytheItem(Tiers.STONE, 4.3f, -2.6f, new Item.Properties().durability(131)));
    public static final DeferredHolder<Item, ScytheItem> IRON_SCYTHE = ITEMS.register("iron_scythe",
            () -> new ScytheItem(Tiers.IRON, 5.0f, -2.6f, new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, ScytheItem> GOLD_SCYTHE = ITEMS.register("gold_scythe",
            () -> new ScytheItem(Tiers.GOLD, 3.8f, -2.6f, new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, ScytheItem> DIAMOND_SCYTHE = ITEMS.register("diamond_scythe",
            () -> new ScytheItem(Tiers.DIAMOND, 6.0f, -2.6f, new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, ScytheItem> NETHERITE_SCYTHE = ITEMS.register("netherite_scythe",
            () -> new ScytheItem(Tiers.NETHERITE, 7.0f, -2.6f, new Item.Properties().durability(2031).fireResistant()));

    // --- Spears ---
    public static final DeferredHolder<Item, SpearItem> WOOD_SPEAR = ITEMS.register("wood_spear",
            () -> new SpearItem(Tiers.WOOD, 3.0f, -2.0f, new Item.Properties().durability(59)));
    public static final DeferredHolder<Item, SpearItem> STONE_SPEAR = ITEMS.register("stone_spear",
            () -> new SpearItem(Tiers.STONE, 3.8f, -2.0f, new Item.Properties().durability(131)));
    public static final DeferredHolder<Item, SpearItem> IRON_SPEAR = ITEMS.register("iron_spear",
            () -> new SpearItem(Tiers.IRON, 4.5f, -2.0f, new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, SpearItem> GOLD_SPEAR = ITEMS.register("gold_spear",
            () -> new SpearItem(Tiers.GOLD, 3.4f, -2.0f, new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, SpearItem> DIAMOND_SPEAR = ITEMS.register("diamond_spear",
            () -> new SpearItem(Tiers.DIAMOND, 5.4f, -2.0f, new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, SpearItem> NETHERITE_SPEAR = ITEMS.register("netherite_spear",
            () -> new SpearItem(Tiers.NETHERITE, 6.3f, -2.0f, new Item.Properties().durability(2031).fireResistant()));

    // --- Spellblades ---
    public static final DeferredHolder<Item, SpellbladeItem> WOOD_SPELLBLADE = ITEMS.register("wood_spellblade",
            () -> new SpellbladeItem(Tiers.WOOD, 2.6f, -2.4f, new Item.Properties().durability(59)));
    public static final DeferredHolder<Item, SpellbladeItem> STONE_SPELLBLADE = ITEMS.register("stone_spellblade",
            () -> new SpellbladeItem(Tiers.STONE, 3.4f, -2.4f, new Item.Properties().durability(131)));
    public static final DeferredHolder<Item, SpellbladeItem> IRON_SPELLBLADE = ITEMS.register("iron_spellblade",
            () -> new SpellbladeItem(Tiers.IRON, 4.0f, -2.4f, new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, SpellbladeItem> GOLD_SPELLBLADE = ITEMS.register("gold_spellblade",
            () -> new SpellbladeItem(Tiers.GOLD, 3.0f, -2.4f, new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, SpellbladeItem> DIAMOND_SPELLBLADE = ITEMS.register("diamond_spellblade",
            () -> new SpellbladeItem(Tiers.DIAMOND, 4.8f, -2.4f, new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, SpellbladeItem> NETHERITE_SPELLBLADE = ITEMS.register("netherite_spellblade",
            () -> new SpellbladeItem(Tiers.NETHERITE, 5.6f, -2.4f, new Item.Properties().durability(2031).fireResistant()));
    public static final DeferredHolder<Item, GunItem> IRON_GUN = ITEMS.register("iron_gun",
            () -> new GunItem(new Item.Properties().durability(250)));
    public static final DeferredHolder<Item, GunItem> GOLD_GUN = ITEMS.register("gold_gun",
            () -> new GunItem(new Item.Properties().durability(32)));
    public static final DeferredHolder<Item, GunItem> DIAMOND_GUN = ITEMS.register("diamond_gun",
            () -> new GunItem(new Item.Properties().durability(1561)));
    public static final DeferredHolder<Item, GunItem> NETHERITE_GUN = ITEMS.register("netherite_gun",
            () -> new GunItem(new Item.Properties().durability(2031).fireResistant()));




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}