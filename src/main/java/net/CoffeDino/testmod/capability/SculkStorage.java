package net.CoffeDino.testmod.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SculkStorage implements ISculkStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(SculkStorage.class);
    private static final int DEFAULT_ROWS = 3;

    private ItemStack[] items = new ItemStack[9 * DEFAULT_ROWS];
    private int rows = DEFAULT_ROWS;
    private long cooldownEndTime = 0;

    public SculkStorage() {
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }
        LOGGER.debug("Created new SculkStorage with {} slots", items.length);
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= items.length) return ItemStack.EMPTY;
        return items[slot];
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.length) {
            items[slot] = stack;
        }
    }

    @Override
    public int getContainerSize() {
        return 9 * rows;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void setRows(int rows) {
        this.rows = Math.max(1, Math.min(6, rows));
        resizeStorage();
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public void startCooldown() {
        this.cooldownEndTime = System.currentTimeMillis() + 500;
    }

    @Override
    public boolean isOnCooldown() {
        return System.currentTimeMillis() < cooldownEndTime;
    }

    @Override
    public long getCooldownEndTime() {
        return cooldownEndTime;
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (ItemStack item : items) {
            if (item != null && !item.isEmpty()) count++;
        }
        return count;
    }

    private void resizeStorage() {
        ItemStack[] newItems = new ItemStack[9 * rows];
        for (int i = 0; i < newItems.length; i++) {
            if (i < items.length && items[i] != null) {
                newItems[i] = items[i];
            } else {
                newItems[i] = ItemStack.EMPTY;
            }
        }
        items = newItems;
    }

    @Override
    public void saveData(CompoundTag tag, HolderLookup.Provider provider) {
        tag.putInt("Rows", rows);
        tag.putLong("CooldownEnd", cooldownEndTime);

        ListTag list = new ListTag();
        int itemCount = 0;
        for (int i = 0; i < items.length; i++) {
            ItemStack stack = items[i];
            if (stack != null && !stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                Tag stackTag = stack.save(provider);
                if (stackTag instanceof CompoundTag compoundStackTag) {
                    itemTag.put("Item", compoundStackTag);
                } else {
                    CompoundTag fallbackTag = new CompoundTag();
                    stack.save(provider, fallbackTag);
                    itemTag.put("Item", fallbackTag);
                }

                list.add(itemTag);
                itemCount++;

                LOGGER.debug("SAVE: Saved item at slot {}: {} (count: {})", i, stack.getDisplayName().getString(), stack.getCount());
            }
        }
        tag.put("Items", list);
        LOGGER.info("SAVE: Saved {} items to NBT for {} slots ({} rows)", itemCount, items.length, rows);
    }

    @Override
    public void loadData(CompoundTag tag, HolderLookup.Provider provider) {
        this.rows = tag.getInt("Rows");
        if (this.rows < 1) this.rows = DEFAULT_ROWS;
        this.cooldownEndTime = tag.getLong("CooldownEnd");

        LOGGER.info("LOAD: Loading storage with {} rows, cooldown: {}", rows, cooldownEndTime);
        resizeStorage();
        for (int i = 0; i < items.length; i++) {
            items[i] = ItemStack.EMPTY;
        }

        if (tag.contains("Items", 9)) {
            ListTag list = tag.getList("Items", 10);
            int loadedItems = 0;

            for (int i = 0; i < list.size(); i++) {
                CompoundTag itemTag = list.getCompound(i);
                int slot = itemTag.getInt("Slot");

                if (slot >= 0 && slot < items.length && itemTag.contains("Item", 10)) {
                    CompoundTag stackTag = itemTag.getCompound("Item");

                    try {
                        ItemStack stack = ItemStack.parse(provider, stackTag).orElse(ItemStack.EMPTY);

                        if (!stack.isEmpty()) {
                            items[slot] = stack;
                            loadedItems++;
                            LOGGER.debug("LOAD: Loaded item at slot {}: {}", slot, stack.getDisplayName().getString());
                        } else {
                            LOGGER.warn("LOAD: Empty item parsed from slot {} - tag: {}", slot, stackTag);
                        }
                    } catch (Exception e) {
                        LOGGER.error("LOAD: Failed to parse item at slot {}: {}", slot, e.getMessage());
                        try {
                            ItemStack stack = ItemStack.parse(provider, stackTag).orElse(ItemStack.EMPTY);
                            if (!stack.isEmpty()) {
                                items[slot] = stack;
                                loadedItems++;
                                LOGGER.debug("LOAD: Loaded fallback item at slot {}: {}", slot, stack.getDisplayName().getString());
                            }
                        } catch (Exception e2) {
                            LOGGER.error("LOAD: Fallback loading also failed for slot {}", slot);
                        }
                    }
                } else {
                    LOGGER.warn("LOAD: Invalid slot {} or missing Item tag", slot);
                }
            }
            LOGGER.info("LOAD: Successfully loaded {} items into storage", loadedItems);
        } else {
            LOGGER.warn("LOAD: No Items tag found in NBT");
        }
    }
}