package com.CoffeDino.lunacy.menu;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Lunacy.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<SculkStorageMenu>> SCULK_STORAGE = MENUS.register("sculk_storage",
            () -> IMenuTypeExtension.create((windowId, inv, data) ->
                    new SculkStorageMenu(windowId, inv, inv.player)));
}