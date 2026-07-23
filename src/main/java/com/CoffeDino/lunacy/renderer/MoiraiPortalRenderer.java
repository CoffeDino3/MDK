package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.MoiraiPortalEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class MoiraiPortalRenderer extends EntityRenderer<MoiraiPortalEntity> {

    private static final ResourceLocation PORTAL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("lunacy", "textures/entity/moirai_portal.png");

    private static final float BASE_SIZE = 4.2f;

    private static final float U0 = 71f / 588f;
    private static final float U1 = 476f / 588f;
    private static final float V0 = 16f / 424f;
    private static final float V1 = 403f / 424f;
    private static final float SPIN_DEG_PER_TICK = 2.5f;

    public MoiraiPortalRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(MoiraiPortalEntity entity) {
        return PORTAL_TEXTURE;
    }

    @Override
    public void render(MoiraiPortalEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float visibility = entity.getVisibility(partialTick);
        if (visibility > 0f) {
            float age = entity.tickCount + partialTick;
            poseStack.pushPose();
            poseStack.mulPose(new Quaternionf().rotationY((float) Math.toRadians(180 - entityYaw)));
            poseStack.mulPose(new Quaternionf().rotationZ((float) Math.toRadians(age * SPIN_DEG_PER_TICK)));

            float scale = BASE_SIZE * (0.4f + 0.6f * visibility);
            poseStack.scale(scale, scale, scale);

            VertexConsumer buffer = bufferSource.getBuffer(RenderType.eyes(PORTAL_TEXTURE));
            Matrix4f mat = poseStack.last().pose();
            int alpha = (int) Mth.clamp(visibility * 255f, 0f, 255f);
            int light = 15728880;

            addQuad(buffer, mat, alpha, light);

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    private void addQuad(VertexConsumer buffer, Matrix4f mat, int alpha, int light) {
        buffer.addVertex(mat, -0.5f, -0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U0, V1)
                .setOverlay(0).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(mat, 0.5f, -0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U1, V1)
                .setOverlay(0).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(mat, 0.5f, 0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U1, V0)
                .setOverlay(0).setLight(light).setNormal(0, 0, 1);
        buffer.addVertex(mat, -0.5f, 0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U0, V0)
                .setOverlay(0).setLight(light).setNormal(0, 0, 1);

        buffer.addVertex(mat, -0.5f, 0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U0, V0)
                .setOverlay(0).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(mat, 0.5f, 0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U1, V0)
                .setOverlay(0).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(mat, 0.5f, -0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U1, V1)
                .setOverlay(0).setLight(light).setNormal(0, 0, -1);
        buffer.addVertex(mat, -0.5f, -0.5f, 0f).setColor(255, 255, 255, alpha).setUv(U0, V1)
                .setOverlay(0).setLight(light).setNormal(0, 0, -1);
    }
}