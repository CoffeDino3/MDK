package net.CoffeDino.testmod.menu;

import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkStorageMenuProvider implements MenuProvider {
    private final Player player;

    public SculkStorageMenuProvider(Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Component.literal("Sculk Storage");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, @NotNull Inventory playerInventory, @NotNull Player player) {
        return new SculkStorageMenu(windowId, playerInventory, this.player);
    }
}