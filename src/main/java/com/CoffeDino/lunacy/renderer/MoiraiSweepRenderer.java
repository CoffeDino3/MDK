package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.MoiraiSweepEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class MoiraiSweepRenderer extends EntityRenderer<MoiraiSweepEntity> {

    public MoiraiSweepRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MoiraiSweepEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(MoiraiSweepEntity entity) {
        return null;
    }
}