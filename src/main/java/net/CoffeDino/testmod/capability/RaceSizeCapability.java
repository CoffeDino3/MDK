package net.CoffeDino.testmod.capability;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class RaceSizeCapability implements IRaceSize {
    private float raceHeight = 1.8f;
    private float raceWidth = 0.6f;

    @Override
    public float getRaceHeight() {
        return raceHeight;
    }

    @Override
    public float getRaceWidth() {
        return raceWidth;
    }

    @Override
    public void setRaceSize(float height, float width) {
        this.raceHeight = height;
        this.raceWidth = width;
    }

    @Override
    public void resetRaceSize() {
        this.raceHeight = 1.8f;
        this.raceWidth = 0.6f;
    }

    // Updated for 1.21 - include HolderLookup.Provider parameter
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("race_height", raceHeight);
        nbt.putFloat("race_width", raceWidth);
        return nbt;
    }

    // Updated for 1.21 - include HolderLookup.Provider parameter
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt) {
        if (nbt.contains("race_height")) {
            raceHeight = nbt.getFloat("race_height");
        }
        if (nbt.contains("race_width")) {
            raceWidth = nbt.getFloat("race_width");
        }
    }
}