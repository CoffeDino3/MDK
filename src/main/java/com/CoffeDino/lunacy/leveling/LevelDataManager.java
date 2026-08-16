package com.CoffeDino.lunacy.leveling;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelDataManager extends SavedData {
    private static final String DATA_NAME = Lunacy.MODID + "_levels";
    private static final int DEFAULT_LEVEL = 1;
    private static final int DEFAULT_XP = 0;

    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<UUID, Integer> playerXp = new HashMap<>();
    private static final boolean DEBUG = false;

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag levelsTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : playerLevels.entrySet()) {
            levelsTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("playerLevels", levelsTag);

        CompoundTag xpTag = new CompoundTag();
        for (Map.Entry<UUID, Integer> entry : playerXp.entrySet()) {
            xpTag.putInt(entry.getKey().toString(), entry.getValue());
        }
        tag.put("playerXp", xpTag);

        if (DEBUG) {
            Lunacy.LOGGER.debug("LevelDataManager saved - {} players", playerLevels.size());
        }
        return tag;
    }

    public static LevelDataManager load(CompoundTag tag, HolderLookup.Provider registries) {
        LevelDataManager data = new LevelDataManager();

        CompoundTag levelsTag = tag.getCompound("playerLevels");
        for (String uuidString : levelsTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                data.playerLevels.put(playerId, levelsTag.getInt(uuidString));
            } catch (IllegalArgumentException e) {
                Lunacy.LOGGER.error("Invalid UUID in level data: {}", uuidString);
            }
        }

        CompoundTag xpTag = tag.getCompound("playerXp");
        for (String uuidString : xpTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                data.playerXp.put(playerId, xpTag.getInt(uuidString));
            } catch (IllegalArgumentException e) {
                Lunacy.LOGGER.error("Invalid UUID in xp data: {}", uuidString);
            }
        }

        if (DEBUG) {
            Lunacy.LOGGER.debug("LevelDataManager loaded - {} players", data.playerLevels.size());
        }
        return data;
    }

    public int getLevel(UUID playerId) {
        return playerLevels.getOrDefault(playerId, DEFAULT_LEVEL);
    }

    public void setLevel(UUID playerId, int level) {
        playerLevels.put(playerId, level);
        setDirty();
        if (DEBUG) {
            Lunacy.LOGGER.debug("Set level for player {}: {}", playerId, level);
        }
    }

    public int getXp(UUID playerId) {
        return playerXp.getOrDefault(playerId, DEFAULT_XP);
    }

    public void setXp(UUID playerId, int xp) {
        playerXp.put(playerId, xp);
        setDirty();
        if (DEBUG) {
            Lunacy.LOGGER.debug("Set xp for player {}: {}", playerId, xp);
        }
    }

    public void clear(UUID playerId) {
        playerLevels.remove(playerId);
        playerXp.remove(playerId);
        setDirty();
    }

    public static LevelDataManager get(ServerPlayer player) {
        if (player.getServer() == null) {
            Lunacy.LOGGER.warn("Server is null for player: {}", player.getName().getString());
            return new LevelDataManager();
        }

        DimensionDataStorage storage = player.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(
                        LevelDataManager::new,
                        LevelDataManager::load,
                        null
                ),
                DATA_NAME
        );
    }
}