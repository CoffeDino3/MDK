package com.CoffeDino.lunacy.classes;

import com.CoffeDino.lunacy.Lunacy;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClassDataManager extends SavedData {
    private static final String DATA_NAME = Lunacy.MODID + "_classes";
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private final Map<UUID, String> playerElements = new HashMap<>();
    private static final boolean DEBUG = false;

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag classesTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : playerClasses.entrySet()) {
            classesTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("playerClasses", classesTag);
        if (DEBUG) {
            Lunacy.LOGGER.debug("ClassDataManager saved - {} player classes", playerClasses.size());
        }
        CompoundTag elementsTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : playerElements.entrySet()) {
            elementsTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("playerElements", elementsTag);
        return tag;
    }

    public static ClassDataManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ClassDataManager data = new ClassDataManager();
        CompoundTag classesTag = tag.getCompound("playerClasses");
        for (String uuidString : classesTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                String classId = classesTag.getString(uuidString);
                data.playerClasses.put(playerId, classId);
            } catch (IllegalArgumentException e) {
                Lunacy.LOGGER.error("Invalid UUID in class data: {}", uuidString);
            }
        }
        if (DEBUG) {
            Lunacy.LOGGER.debug("ClassDataManager loaded - {} player classes", data.playerClasses.size());
        }
        CompoundTag elementsTag = tag.getCompound("playerElements");
        for (String uuidString : elementsTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                data.playerElements.put(playerId, elementsTag.getString(uuidString));
            } catch (IllegalArgumentException e) {
                Lunacy.LOGGER.error("Invalid UUID in element data: {}", uuidString);
            }
        }
        return data;
    }
    public void setPlayerElement(UUID playerId, String elementId) {
        if (elementId == null || elementId.isEmpty()) {
            playerElements.remove(playerId);
        } else {
            playerElements.put(playerId, elementId);
        }
        setDirty();
    }

    public String getPlayerElement(UUID playerId) {
        return playerElements.get(playerId);
    }

    public void setPlayerClass(UUID playerId, String classId) {
        if (classId == null || classId.isEmpty()) {
            playerClasses.remove(playerId);
            if (DEBUG) {
                Lunacy.LOGGER.debug("Removed class for player: {}", playerId);
            }
        } else {
            playerClasses.put(playerId, classId);
            if (DEBUG) {
                Lunacy.LOGGER.debug("Set class for player {}: {}", playerId, classId);
            }
        }
        setDirty();
    }

    public String getPlayerClass(UUID playerId) {
        return playerClasses.get(playerId);
    }

    public boolean hasClass(UUID playerId) {
        return playerClasses.containsKey(playerId);
    }

    public static ClassDataManager get(ServerPlayer player) {
        if (player.serverLevel() == null) {
            Lunacy.LOGGER.warn("Server level is null for player: {}", player.getName().getString());
            return new ClassDataManager();
        }

        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        return storage.computeIfAbsent(
                new Factory<>(
                        ClassDataManager::new,
                        ClassDataManager::load,
                        null
                ),
                DATA_NAME
        );
    }
}