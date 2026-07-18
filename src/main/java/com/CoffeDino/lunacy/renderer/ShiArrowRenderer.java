package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.client.model.Shi_arrows;
import com.CoffeDino.lunacy.entity.ShiArrowEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ShiArrowRenderer extends EntityRenderer<ShiArrowEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("lunacy", "textures/entity/shi_arrow.png");
    private final Shi_arrows<ShiArrowEntity> model;

    public ShiArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new Shi_arrows<>(context.bakeLayer(Shi_arrows.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(ShiArrowEntity entity) {
        return TEXTURE;
    }

    @Override
    public void render(ShiArrowEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getViewYRot(partialTicks) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getViewXRot(partialTicks)));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.translate(0.75F, -1.7F, -0.5F);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        int color = 0xFFFFFFFF;
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, color);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}