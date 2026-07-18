package com.CoffeDino.lunacy.menu;

import com.CoffeDino.lunacy.capability.ModAttachments;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class SculkStorageSlot extends Slot {
    private final Player player;
    private final int slotIndex;

    public SculkStorageSlot(Player player, int index, int x, int y) {
        super(new SculkStorageContainer(player), index, x, y);
        this.player = player;
        this.slotIndex = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getItem() {
        return player.getData(ModAttachments.SCULK_STORAGE).getItem(slotIndex);
    }

    @Override
    public void set(ItemStack stack) {
        player.getData(ModAttachments.SCULK_STORAGE).setItem(slotIndex, stack);
        this.setChanged();
    }

    @Override
    public void setChanged() {
        if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            serverPlayer.inventoryMenu.broadcastChanges();
        }
    }

    @Override
    public ItemStack remove(int amount) {
        ItemStack current = getItem();
        if (current.isEmpty()) return ItemStack.EMPTY;

        int extractAmount = Math.min(amount, current.getCount());
        ItemStack result = current.split(extractAmount);

        if (current.isEmpty()) {
            set(ItemStack.EMPTY);
        } else {
            set(current);
        }

        return result;
    }

    private static class SculkStorageContainer implements net.minecraft.world.Container {
        private final Player player;

        public SculkStorageContainer(Player player) {
            this.player = player;
        }

        @Override
        public int getContainerSize() {
            return player.getData(ModAttachments.SCULK_STORAGE).getContainerSize();
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < getContainerSize(); i++) {
                if (!getItem(i).isEmpty()) return false;
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return player.getData(ModAttachments.SCULK_STORAGE).getItem(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            ItemStack current = getItem(slot);
            if (current.isEmpty()) return ItemStack.EMPTY;

            int extractAmount = Math.min(amount, current.getCount());
            ItemStack result = current.split(extractAmount);

            if (current.isEmpty()) {
                setItem(slot, ItemStack.EMPTY);
            } else {
                setItem(slot, current);
            }

            return result;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack item = getItem(slot);
            setItem(slot, ItemStack.EMPTY);
            return item;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            player.getData(ModAttachments.SCULK_STORAGE).setItem(slot, stack);
        }

        @Override
        public void setChanged() {}

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < getContainerSize(); i++) {
                setItem(i, ItemStack.EMPTY);
            }
        }
    }
}