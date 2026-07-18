package com.CoffeDino.lunacy.capability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public class RaceSizeCapability implements IRaceSize {

    public static final Codec<RaceSizeCapability> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("race_height").forGetter(RaceSizeCapability::getRaceHeight),
            Codec.FLOAT.fieldOf("race_width").forGetter(RaceSizeCapability::getRaceWidth)
    ).apply(instance, RaceSizeCapability::new));

    private float raceHeight = 1.8f;
    private float raceWidth = 0.6f;

    public RaceSizeCapability() {
    }

    public RaceSizeCapability(float raceHeight, float raceWidth) {
        this.raceHeight = raceHeight;
        this.raceWidth = raceWidth;
    }

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

    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        CompoundTag nbt = new CompoundTag();
        nbt.putFloat("race_height", raceHeight);
        nbt.putFloat("race_width", raceWidth);
        return nbt;
    }

    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt) {
        if (nbt.contains("race_height")) {
            raceHeight = nbt.getFloat("race_height");
        }
        if (nbt.contains("race_width")) {
            raceWidth = nbt.getFloat("race_width");
        }
    }
}