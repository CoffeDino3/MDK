package com.CoffeDino.lunacy.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class ReaperSoulData {

    public static final Codec<ReaperSoulData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("soulStacks").forGetter(ReaperSoulData::getSoulStacks),
            Codec.LONG.fieldOf("lastGainTime").forGetter(ReaperSoulData::getLastGainTime)
    ).apply(instance, ReaperSoulData::new));

    private int soulStacks;
    private long lastGainTime;

    public ReaperSoulData() {
        this.soulStacks = 0;
        this.lastGainTime = 0;
    }

    public ReaperSoulData(int soulStacks, long lastGainTime) {
        this.soulStacks = soulStacks;
        this.lastGainTime = lastGainTime;
    }

    public int getSoulStacks() {
        return soulStacks;
    }

    public void setSoulStacks(int stacks) {
        this.soulStacks = stacks;
    }

    public void addStack(int maxStacks) {
        if (soulStacks < maxStacks) {
            soulStacks++;
        }
    }

    public long getLastGainTime() {
        return lastGainTime;
    }

    public void setLastGainTime(long time) {
        this.lastGainTime = time;
    }
}