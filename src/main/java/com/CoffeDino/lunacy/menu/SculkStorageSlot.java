package com.CoffeDino.lunacy.menu;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.leveling.ClientPlayerLevelData;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.Slot;

public class SculkStorageSlot extends Slot {
    private final Player player;
    private int slotIndex;

    public SculkStorageSlot(Player player, int index, int x, int y) {
        super(new SculkStorageContainer(player), index, x, y);
        this.player = player;
        this.slotIndex = index;
    }

    public void setSlotIndex(int newIndex) {
        this.slotIndex = newIndex;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public boolean isActive() {
        return slotIndex >= 0;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive();
    }
    @Override
    public int getMaxStackSize(ItemStack stack) {
        if (!isActive()) return stack.getMaxStackSize();
        int level = getOwnerLevel();
        return player.getData(ModAttachments.SCULK_STORAGE).getEffectiveMaxStackSize(stack, level);
    }

    @Override
    public int getMaxStackSize() {
        return getMaxStackSize(ItemStack.EMPTY);
    }

    private int getOwnerLevel() {
        if (player instanceof ServerPlayer serverPlayer) {
            return PlayerLevels.getLevel(serverPlayer);
        }
        return ClientPlayerLevelData.getLevel();
    }

    @Override
    public ItemStack getItem() {
        if (!isActive()) return ItemStack.EMPTY;
        return player.getData(ModAttachments.SCULK_STORAGE).getItem(slotIndex);
    }

    @Override
    public boolean hasItem() {
        return isActive() && !getItem().isEmpty();
    }

    @Override
    public void set(ItemStack stack) {
        if (!isActive()) return;
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
        if (!isActive()) return ItemStack.EMPTY;
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