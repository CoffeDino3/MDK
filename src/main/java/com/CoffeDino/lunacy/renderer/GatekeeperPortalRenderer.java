package com.CoffeDino.lunacy.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.entity.abilities.GatekeeperPortalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class GatekeeperPortalRenderer extends EntityRenderer<GatekeeperPortalEntity> {
    private static final ResourceLocation PORTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/entity/portal.png");

    public GatekeeperPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GatekeeperPortalEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getFacingYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getFacingPitch()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.tickCount * 1.5f % 360));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.tickCount * 1.5f % 360));

        float scale = 2.5f;
        poseStack.scale(scale, scale, scale);


        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();

        vertexConsumer.addVertex(matrix, -0.5f, -0.5f, 0).setColor(255, 255, 255, 255).setUv(0, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
        vertexConsumer.addVertex(matrix,  0.5f, -0.5f, 0).setColor(255, 255, 255, 255).setUv(1, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
        vertexConsumer.addVertex(matrix,  0.5f,  0.5f, 0).setColor(255, 255, 255, 255).setUv(1, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);
        vertexConsumer.addVertex(matrix, -0.5f,  0.5f, 0).setColor(255, 255, 255, 255).setUv(0, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0, 0, 1);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }
    @Override
    public ResourceLocation getTextureLocation(GatekeeperPortalEntity entity) {
        return PORTAL_TEXTURE;
    }
}