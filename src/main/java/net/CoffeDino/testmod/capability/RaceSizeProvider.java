
package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.Lunacy;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RaceSizeProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IRaceSize> RACE_SIZE =
            CapabilityManager.get(new CapabilityToken<>() {});

    private static final ResourceLocation RACE_SIZE_ID =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "race_size");

    private final IRaceSize instance = new RaceSizeCapability();
    private final LazyOptional<IRaceSize> optional = LazyOptional.of(() -> instance);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return RACE_SIZE.orEmpty(cap, optional);
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registryAccess) {
        if (instance instanceof RaceSizeCapability raceSize) {
            return raceSize.serializeNBT(registryAccess);
        }
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registryAccess, CompoundTag nbt) {
        if (instance instanceof RaceSizeCapability raceSize) {
            raceSize.deserializeNBT(registryAccess, nbt);
        }
    }
}