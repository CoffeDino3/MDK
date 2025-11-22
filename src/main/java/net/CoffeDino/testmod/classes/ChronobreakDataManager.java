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

public class ChronobreakDataManager extends SavedData {
    private static final String DATA_NAME = TestingCoffeDinoMod.MOD_ID + "_chronobreak";
    private final Map<UUID, PlayerChronobreakData> playerData = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        CompoundTag playersTag = new CompoundTag();
        for (Map.Entry<UUID, PlayerChronobreakData> entry : playerData.entrySet()) {
            CompoundTag playerTag = new CompoundTag();
            entry.getValue().save(playerTag);
            playersTag.put(entry.getKey().toString(), playerTag);
        }
        tag.put("playerChronobreakData", playersTag);
        return tag;
    }

    public static ChronobreakDataManager load(CompoundTag tag, HolderLookup.Provider registries) {
        ChronobreakDataManager data = new ChronobreakDataManager();
        CompoundTag playersTag = tag.getCompound("playerChronobreakData");
        for (String uuidString : playersTag.getAllKeys()) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                CompoundTag playerTag = playersTag.getCompound(uuidString);
                PlayerChronobreakData playerData = PlayerChronobreakData.load(playerTag);
                data.playerData.put(playerId, playerData);
            } catch (IllegalArgumentException e) {
                TestingCoffeDinoMod.LOGGER.error("Invalid UUID in chronobreak data: {}", uuidString);
            }
        }
        return data;
    }

    public PlayerChronobreakData getOrCreatePlayerData(UUID playerId) {
        return playerData.computeIfAbsent(playerId, k -> {
            setDirty();
            return new PlayerChronobreakData();
        });
    }

    public static ChronobreakDataManager get(ServerPlayer player) {
        DimensionDataStorage storage = player.serverLevel().getDataStorage();
        return storage.computeIfAbsent(
                new SavedData.Factory<>(ChronobreakDataManager::new, ChronobreakDataManager::load, null),
                DATA_NAME
        );
    }

    public static class PlayerChronobreakData {
        private static final String TAG_LAST_SAVE_TIME = "last_save_time";
        private static final String TAG_CHRONO_POS = "chrono_pos";
        private static final String TAG_CHRONO_HEALTH = "chrono_health";
        private static final String TAG_ON_COOLDOWN = "on_cooldown";
        private static final String TAG_COOLDOWN_END = "cooldown_end";

        private long lastSaveTime = 0;
        private CompoundTag chronoPos = null;
        private float chronoHealth = 0;
        private boolean onCooldown = false;
        private long cooldownEnd = 0;

        public void save(CompoundTag tag) {
            tag.putLong(TAG_LAST_SAVE_TIME, lastSaveTime);
            tag.putBoolean(TAG_ON_COOLDOWN, onCooldown);
            tag.putLong(TAG_COOLDOWN_END, cooldownEnd);
            tag.putFloat(TAG_CHRONO_HEALTH, chronoHealth);

            if (chronoPos != null) {
                tag.put(TAG_CHRONO_POS, chronoPos);
            }
        }

        public static PlayerChronobreakData load(CompoundTag tag) {
            PlayerChronobreakData data = new PlayerChronobreakData();
            data.lastSaveTime = tag.getLong(TAG_LAST_SAVE_TIME);
            data.onCooldown = tag.getBoolean(TAG_ON_COOLDOWN);
            data.cooldownEnd = tag.getLong(TAG_COOLDOWN_END);
            data.chronoHealth = tag.getFloat(TAG_CHRONO_HEALTH);

            if (tag.contains(TAG_CHRONO_POS)) {
                data.chronoPos = tag.getCompound(TAG_CHRONO_POS);
            }

            return data;
        }
        public long getLastSaveTime() { return lastSaveTime; }
        public void setLastSaveTime(long time) { this.lastSaveTime = time; }

        public CompoundTag getChronoPos() { return chronoPos; }
        public void setChronoPos(CompoundTag pos) { this.chronoPos = pos; }

        public float getChronoHealth() { return chronoHealth; }
        public void setChronoHealth(float health) { this.chronoHealth = health; }

        public boolean isOnCooldown() { return onCooldown; }
        public void setOnCooldown(boolean cooldown) { this.onCooldown = cooldown; }

        public long getCooldownEnd() { return cooldownEnd; }
        public void setCooldownEnd(long end) { this.cooldownEnd = end; }
    }
}