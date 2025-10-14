package net.CoffeDino.testmod.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface ISculkStorage {
    ItemStack getItem(int slot);
    void setItem(int slot, ItemStack stack);
    int getContainerSize();
    boolean stillValid(Player player);
    void setRows(int rows);
    int getRows();
    void startCooldown();
    boolean isOnCooldown();
    long getCooldownEndTime();
    void saveData(CompoundTag tag, HolderLookup.Provider provider);
    void loadData(CompoundTag tag, HolderLookup.Provider provider);
    int getItemCount();
}