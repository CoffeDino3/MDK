package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.BloodMistEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;

public class BloodMistRenderer extends EntityRenderer<BloodMistEntity> {

    public BloodMistRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(BloodMistEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(BloodMistEntity entity) {
        return null;
    }
}