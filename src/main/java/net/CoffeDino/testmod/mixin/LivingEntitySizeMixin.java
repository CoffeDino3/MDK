package net.CoffeDino.testmod.mixin;

import net.CoffeDino.testmod.capability.RaceSizeProvider;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySizeMixin {

    @Inject(
            method = "getDimensions(Lnet/minecraft/world/entity/Pose;)Lnet/minecraft/world/entity/EntityDimensions;",
            at = @At("HEAD"),
            cancellable = true
    )
    private void injectCustomDimensions(Pose pose, CallbackInfoReturnable<EntityDimensions> cir) {
        if ((Object) this instanceof Player player) {
            player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(raceSize -> {
                float height = raceSize.getRaceHeight();
                float width = raceSize.getRaceWidth();
                cir.setReturnValue(EntityDimensions.scalable(width, height));
            });
        }
    }


}