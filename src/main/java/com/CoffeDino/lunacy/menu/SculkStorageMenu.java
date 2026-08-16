package com.CoffeDino.lunacy.menu;

import com.CoffeDino.lunacy.capability.ModAttachments;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SculkStorageMenu extends AbstractContainerMenu {
    public static final MenuType<SculkStorageMenu> TYPE = ModMenuTypes.SCULK_STORAGE.get();
    public static final int VISIBLE_ROWS = 3;
    private static final int STORAGE_SLOT_COUNT = VISIBLE_ROWS * 9;
    private final Player player;
    private final int rowCount;
    private int scrollRows = 0;
    public SculkStorageMenu(int windowId, Inventory playerInventory, Player player, int rowCount) {
        super(TYPE, windowId);
        if (!player.isAlive()) {
            throw new IllegalStateException("Cannot open menu for dead player");
        }
        this.player = player;
        this.rowCount = Math.max(1, rowCount);

        for (int row = 0; row < VISIBLE_ROWS; ++row) {
            for (int col = 0; col < 9; ++col) {
                int viewportSlot = col + row * 9;
                int absoluteIndex = viewportSlot < this.rowCount * 9 ? viewportSlot : -1;
                SculkStorageSlot slot = new SculkStorageSlot(player, absoluteIndex, 8 + col * 18, 18 + row * 18);
                this.addSlot(slot);
            }
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 85 + row * 18 + (VISIBLE_ROWS - 3) * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 143 + (VISIBLE_ROWS - 3) * 18));
        }
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getScrollRows() {
        return scrollRows;
    }

    public int getMaxScroll() {
        return Math.max(0, rowCount - VISIBLE_ROWS);
    }
    public void remapVisibleSlots(int newScrollRows) {
        int clamped = Math.max(0, Math.min(getMaxScroll(), newScrollRows));
        if (clamped == this.scrollRows) return;
        this.scrollRows = clamped;

        int totalStorageSlots = rowCount * 9;
        for (int viewportRow = 0; viewportRow < VISIBLE_ROWS; viewportRow++) {
            for (int col = 0; col < 9; col++) {
                int viewportSlotListIndex = col + viewportRow * 9;
                Slot slot = this.slots.get(viewportSlotListIndex);
                if (!(slot instanceof SculkStorageSlot sculkSlot)) continue;

                int absoluteRow = viewportRow + this.scrollRows;
                int absoluteIndex = col + absoluteRow * 9;
                sculkSlot.setSlotIndex(absoluteIndex < totalStorageSlots ? absoluteIndex : -1);
            }
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < STORAGE_SLOT_COUNT) {
                if (!this.moveItemStackTo(itemstack1, STORAGE_SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, STORAGE_SLOT_COUNT, false)) {
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
        return player.isAlive() && !player.getData(ModAttachments.SCULK_STORAGE).isOnCooldown();
    }
    public void broadcastFullState() {
        this.broadcastChanges();
    }
}