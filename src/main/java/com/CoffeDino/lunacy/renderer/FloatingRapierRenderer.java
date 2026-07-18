package com.CoffeDino.lunacy.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.CoffeDino.lunacy.entity.FloatingRapierEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

public class FloatingRapierRenderer extends EntityRenderer<FloatingRapierEntity> {

    public FloatingRapierRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FloatingRapierEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, 0.25D, 0.2D);
        float yRot = entity.getYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        poseStack.translate(0.0D, 0.35D, 0.0D);
        poseStack.scale(1.0F, 1.5F, 1.0F);
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        itemRenderer.renderStatic(
                entity.getDisplayItem(),
                ItemDisplayContext.FIXED,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FloatingRapierEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}