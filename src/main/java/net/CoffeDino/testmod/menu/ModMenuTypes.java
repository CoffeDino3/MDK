package net.CoffeDino.testmod.menu;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Lunacy.MOD_ID);

    public static final RegistryObject<MenuType<SculkStorageMenu>> SCULK_STORAGE = MENUS.register("sculk_storage",
            () -> IForgeMenuType.create((windowId, inv, data) ->
                    new SculkStorageMenu(windowId, inv, inv.player)));
}