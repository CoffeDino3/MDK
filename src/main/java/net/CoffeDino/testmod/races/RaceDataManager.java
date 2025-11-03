package net.CoffeDino.testmod.races;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RaceDataManager extends SavedData {
    private static final String DATA_NAME = TestingCoffeDinoMod.MOD_ID + "_races";
    private final Map<UUID, String> playerRaces = new HashMap<>();
    private static final boolean DEBUG = false;

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag racesTag = new CompoundTag();
        for (Map.Entry<UUID, String> entry : playerRaces.entrySet()) {
            racesTag.putString(entry.getKey().toString(), entry.getValue());
        }
        tag.put("playerRaces", racesTag);
        if (DEBUG) {
            TestingCoffeDinoMod.LOGGER.debug("RaceDataManager saved - {} player races", playerRaces.size());
        }
        return tag;
    }

    public static RaceDataManager load(CompoundTag tag, HolderLookup.Provider registries) {
        RaceDataManager data = new RaceDataManager();
        CompoundTag racesTag = tag.getCompound("playerRaces");
        for (String uuidString : racesTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                String raceId = racesTag.getString(uuidString);
                data.playerRaces.put(playerId, raceId);
            } catch (IllegalArgumentException e) {
                TestingCoffeDinoMod.LOGGER.error("Invalid UUID in race data: {}", uuidString);
            }
        }
        if (DEBUG) {
            TestingCoffeDinoMod.LOGGER.debug("RaceDataManager loaded - {} player races", data.playerRaces.size());
        }
        return data;
    }

    public void setPlayerRace(UUID playerId, String raceId) {
        if (raceId == null || raceId.isEmpty()) {
            playerRaces.remove(playerId);
            if (DEBUG) {
                TestingCoffeDinoMod.LOGGER.debug("Removed race for player: {}", playerId);
            }
        } else {
            playerRaces.put(playerId, raceId);
            if (DEBUG) {
                TestingCoffeDinoMod.LOGGER.debug("Set race for player {}: {}", playerId, raceId);
            }
        }
        setDirty();
    }

    public String getPlayerRace(UUID playerId) {
        String race = playerRaces.get(playerId);
        return race;
    }

    public boolean hasRace(UUID playerId) {
        boolean hasRace = playerRaces.containsKey(playerId);
        return hasRace;
    }

    public static RaceDataManager get(ServerPlayer player) {
        if (player.serverLevel() == null) {
            TestingCoffeDinoMod.LOGGER.warn("Server level is null for player: {}", player.getName().getString());
            return new RaceDataManager();
        }

        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        RaceDataManager manager = storage.computeIfAbsent(
                new SavedData.Factory<>(
                        RaceDataManager::new,
                        RaceDataManager::load,
                        null
                ),
                DATA_NAME
        );

        return manager;
    }
}