package net.CoffeDino.testmod.capability;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SculkStorageProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final ISculkStorage storage = new SculkStorage();
    private final LazyOptional<ISculkStorage> optional = LazyOptional.of(() -> storage);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == TestingCoffeDinoMod.SCULK_STORAGE) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        storage.saveData(tag, provider);
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        storage.loadData(tag, provider);
    }
}