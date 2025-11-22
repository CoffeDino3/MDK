package net.CoffeDino.testmod.item;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.item.Custom.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, TestingCoffeDinoMod.MOD_ID);

    public static final RegistryObject<Item> CUMMINGTONITE = ITEMS.register("cummingtonite",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> CUMMINGTONITE_INGOT = ITEMS.register("cummingtonite_ingot",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> STORAGE_GEM = ITEMS.register("storage_gem",
            () -> new Item(new Item.Properties()));
    public static final RegistryObject<Item> STACK_STAR = ITEMS.register("stack_star",
            () -> new Item(new Item.Properties()));

    public static final RegistryObject<Item> THE_WAND = ITEMS.register("the_wand",
            () -> new WandItem(new Item.Properties().durability(200)));
    public static final RegistryObject<Item> SPECIAL_WAND = ITEMS.register("special_wand",
            () -> new SpecialWandItem(new Item.Properties().durability(10)));
    public static final RegistryObject<Item> RACE_RESET_SCROLL = ITEMS.register("race_reset_scroll",
            () -> new RaceResetScrollItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> LAMENT_GUN = ITEMS.register("lament_gun",
            () -> new GunItem(new Item.Properties().durability(500)));

    public static final RegistryObject<Item> LAMENT_BULLET = ITEMS.register("lament_bullet",
            () -> new BulletItem(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> VIRIDYUM_GREATSWORD = ITEMS.register("viridyum_greatsword",
            () -> new ViridyumGreatswordItem(Tiers.NETHERITE, new Item.Properties().durability(2031).fireResistant()));
    public static final RegistryObject<Item> AGNIS_FURY = ITEMS.register("agnis_fury",
            () -> new FireSpearItem(Tiers.NETHERITE, new Item.Properties().durability(2031).fireResistant()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
