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



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}