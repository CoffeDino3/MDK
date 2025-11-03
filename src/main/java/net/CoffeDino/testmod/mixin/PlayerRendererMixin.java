package net.CoffeDino.testmod.mixin;

import net.CoffeDino.testmod.capability.RaceSizeProvider;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin extends LivingEntityRenderer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    public PlayerRendererMixin(EntityRendererProvider.Context context, PlayerModel<AbstractClientPlayer> model, float shadowRadius) {
        super(context, model, shadowRadius);
    }

    @Inject(method = "scale", at = @At("HEAD"), cancellable = true)
    private void scalePlayerModel(AbstractClientPlayer player, PoseStack poseStack, float partialTick, CallbackInfo ci) {
        player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(raceSize -> {
            float height = raceSize.getRaceHeight();
            float width = raceSize.getRaceWidth();

            if (height != 1.8f || width != 0.6f) {
                float heightScale = height / 1.8f;
                float widthScale = width / 0.6f;

                poseStack.scale(widthScale, heightScale, widthScale);

                //this.shadowRadius *= Math.max(widthScale, heightScale);
                ci.cancel();
            }
        });
    }
}