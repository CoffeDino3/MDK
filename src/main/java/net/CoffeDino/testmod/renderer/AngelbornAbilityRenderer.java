package net.CoffeDino.testmod.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.CoffeDino.testmod.entity.abilities.AngelbornAbilityEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class AngelbornAbilityRenderer extends EntityRenderer<AngelbornAbilityEntity> {
    private static final ResourceLocation MAGIC_CIRCLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("lunacy", "textures/entity/magic_circle.png");

    public AngelbornAbilityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(AngelbornAbilityEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        renderMagicCircle(entity, poseStack, bufferSource, packedLight);
        if (entity.hasTarget()) {
            renderBeam(entity, poseStack, bufferSource, partialTicks, packedLight);
        }

        poseStack.popPose();
    }

    private void renderMagicCircle(AngelbornAbilityEntity entity, PoseStack poseStack,
                                   MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0, 0.5, 0);
        float scale = 2.0f;
        poseStack.scale(scale, scale, scale);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(-entity.getYRot()));
        float rotation = entity.tickCount * 4.0f;
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotation));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));

        vertexConsumer.addVertex(poseMatrix, -0.5f, -0.5f, 0)
                .setColor(255, 255, 255, 200).setUv(0, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, 0, 0, 1);

        vertexConsumer.addVertex(poseMatrix, -0.5f, 0.5f, 0)
                .setColor(255, 255, 255, 200).setUv(0, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, 0, 0, 1);

        vertexConsumer.addVertex(poseMatrix, 0.5f, 0.5f, 0)
                .setColor(255, 255, 255, 200).setUv(1, 1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, 0, 0, 1);

        vertexConsumer.addVertex(poseMatrix, 0.5f, -0.5f, 0)
                .setColor(255, 255, 255, 200).setUv(1, 0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
                .setNormal(pose, 0, 0, 1);

        poseStack.popPose();
    }


    private void renderBeam(AngelbornAbilityEntity entity, PoseStack poseStack,
                            MultiBufferSource bufferSource, float partialTicks, int packedLight) {
    }

    @Override
    public ResourceLocation getTextureLocation(AngelbornAbilityEntity entity) {
        return MAGIC_CIRCLE_TEXTURE;
    }
    @Override
    public boolean shouldRender(AngelbornAbilityEntity entity, Frustum frustum, double x, double y, double z) {
        return true;
    }

    @Override
    public boolean shouldShowName(AngelbornAbilityEntity entity) {
        return false;
    }
}