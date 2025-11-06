package net.CoffeDino.testmod.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class ModCapabilities {
    public static final Capability<ISculkStorage> SCULK_STORAGE =
            CapabilityManager.get(new CapabilityToken<>() {});

}
