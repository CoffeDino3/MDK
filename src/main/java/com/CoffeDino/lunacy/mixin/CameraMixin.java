package com.CoffeDino.lunacy.mixin;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.capability.RaceSizeCapability;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private float eyeHeight;

    @Shadow
    private float eyeHeightOld;

    @Shadow
    public abstract Entity getEntity();

    @Unique
    private int debugCounter = 0;

    @Inject(
            method = "setup",
            at = @At("HEAD")
    )
    private void debugCamera(BlockGetter level, Entity entity, boolean detached, boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        if (entity instanceof Player player) {
            RaceSizeCapability raceSize = player.getData(ModAttachments.RACE_SIZE);
            debugCounter++;
            float customHeight = raceSize.getRaceHeight();
            float targetEyeHeight = calculateStableEyeHeight(customHeight, player.getPose());
            this.eyeHeightOld = targetEyeHeight;
            this.eyeHeight = targetEyeHeight;
        }
    }

    @Unique
    private float calculateStableEyeHeight(float height, Pose pose) {
        float baseEyeHeight = height * 0.9f;

        return switch (pose) {
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> baseEyeHeight * 0.4f;
            case CROUCHING -> baseEyeHeight * 0.8f;
            case SLEEPING -> 0.2f;
            default -> baseEyeHeight;
        };
    }
}