package net.CoffeDino.testmod.capability;

import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public interface IRaceSize {
    float getRaceHeight();
    float getRaceWidth();
    void setRaceSize(float height, float width);
    void resetRaceSize();
}
