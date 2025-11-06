package net.CoffeDino.testmod.classes;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClassDataManager extends SavedData {
    private static final String DATA_NAME = TestingCoffeDinoMod.MOD_ID + "_classes";
    private final Map<UUID, String> playerClasses = new HashMap<>();
    private static final boolean DEBUG = false;

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag classesTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : playerClasses.entrySet()) {
            classesTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("playerClasses", classesTag);
        if (DEBUG) {
            TestingCoffeDinoMod.LOGGER.debug("ClassDataManager saved - {} player classes", playerClasses.size());
        }
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
                TestingCoffeDinoMod.LOGGER.error("Invalid UUID in class data: {}", uuidString);
            }
        }
        if (DEBUG) {
            TestingCoffeDinoMod.LOGGER.debug("ClassDataManager loaded - {} player classes", data.playerClasses.size());
        }
        return data;
    }

    public void setPlayerClass(UUID playerId, String classId) {
        if (classId == null || classId.isEmpty()) {
            playerClasses.remove(playerId);
            if (DEBUG) {
                TestingCoffeDinoMod.LOGGER.debug("Removed class for player: {}", playerId);
            }
        } else {
            playerClasses.put(playerId, classId);
            if (DEBUG) {
                TestingCoffeDinoMod.LOGGER.debug("Set class for player {}: {}", playerId, classId);
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
            TestingCoffeDinoMod.LOGGER.warn("Server level is null for player: {}", player.getName().getString());
            return new ClassDataManager();
        }

        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        ClassDataManager manager = storage.computeIfAbsent(
                new SavedData.Factory<>(
                        ClassDataManager::new,
                        ClassDataManager::load,
                        null
                ),
                DATA_NAME
        );

        return manager;
    }
}