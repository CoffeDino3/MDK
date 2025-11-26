package net.CoffeDino.testmod.menu;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.capability.SculkStorageProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkStorageMenu extends AbstractContainerMenu {
    public static final MenuType<SculkStorageMenu> TYPE = ModMenuTypes.SCULK_STORAGE.get();

    private final Player player;
    private final int rowCount;
    public SculkStorageMenu(int windowId, Inventory playerInventory, Player player) {
        super(TYPE, windowId);
        if (!player.isAlive()) {
            throw new IllegalStateException("Cannot open menu for dead player");
        }
        this.player = player;
        this.rowCount = player.getCapability(Lunacy.SCULK_STORAGE)
                .map(storage -> storage.getRows())
                .orElse(3);
        for (int row = 0; row < rowCount; ++row) {
            for (int col = 0; col < 9; ++col) {
                int index = col + row * 9;
                this.addSlot(new SculkStorageSlot(player, index, 8 + col * 18, 18 + row * 18));
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 85 + row * 18 + (rowCount - 3) * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 143 + (rowCount - 3) * 18));
        }
    }


    public SculkStorageMenu(int windowId, Inventory playerInventory, net.minecraft.network.FriendlyByteBuf extraData) {
        this(windowId, playerInventory, playerInventory.player);
    }

    public int getRowCount() {
        return rowCount;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            int storageSize = rowCount * 9;
            if (index < storageSize) {
                if (!this.moveItemStackTo(itemstack1, storageSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, storageSize, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive() && player.getCapability(Lunacy.SCULK_STORAGE)
                .map(storage -> !storage.isOnCooldown())
                .orElse(false);
    }
}