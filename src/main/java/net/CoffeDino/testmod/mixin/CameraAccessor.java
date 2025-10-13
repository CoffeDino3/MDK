package net.CoffeDino.testmod.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Camera.class)
public interface CameraAccessor {
    // Explicitly reference the actual field names so mappings don't get confused
    @Accessor("eyeHeight")
    void setEyeHeight(float eyeHeight);

    @Accessor("eyeHeight")
    float getEyeHeight();

    @Accessor("eyeHeightOld")
    float getEyeHeightOld();

    @Accessor("eyeHeightOld")
    void setEyeHeightOld(float eyeHeightOld);
}
