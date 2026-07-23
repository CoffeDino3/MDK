package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.AmphitriteOrbEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AmphitriteOrbRenderer extends EntityRenderer<AmphitriteOrbEntity> {

    public AmphitriteOrbRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AmphitriteOrbEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(AmphitriteOrbEntity entity) {
        return null;
    }
}