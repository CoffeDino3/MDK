package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.PerunOrbitalStrikeEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class PerunOrbitalStrikeRenderer extends EntityRenderer<PerunOrbitalStrikeEntity> {
    private static final float BEAM_HEIGHT = 60f;
    private static final float BEAM_WIDTH = 0.18f;
    private static final float MAX_RING_RADIUS = (float) (PerunOrbitalStrikeEntity.STRIKE_RADIUS * 1.8);
    private static final float RING_THICKNESS = 0.35f;
    private static final int RING_SEGMENTS = 32;
    private static final float[] RING_DELAY_FRACTIONS = {0f, 0.15f, 0.35f};

    private static final RenderType STRIKE_RENDER_TYPE = RenderType.create(
            "lunacy_orbital_strike",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    public PerunOrbitalStrikeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PerunOrbitalStrikeEntity entity) {
        return ResourceLocation.withDefaultNamespace("missingno");
    }

    @Override
    public void render(PerunOrbitalStrikeEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {

        float t = entity.getAge() + partialTick;
        VertexConsumer buffer = bufferSource.getBuffer(STRIKE_RENDER_TYPE);
        poseStack.pushPose();
        if (t < PerunOrbitalStrikeEntity.IMPACT_TICK) {
            renderDescendBeam(entity, partialTick, poseStack, buffer);
        } else {
            renderImpactRings(t, poseStack, buffer);
        }
        poseStack.popPose();

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }
    private void renderDescendBeam(PerunOrbitalStrikeEntity entity, float partialTick,
                                   PoseStack poseStack, VertexConsumer buffer) {
        float progress = entity.getDescendProgress(partialTick);
        int alpha = (int) (Mth255(Math.min(1f, progress * 2.5f)));
        Matrix4f mat = poseStack.last().pose();
        addBeamQuad(buffer, mat, -BEAM_WIDTH, BEAM_WIDTH, BEAM_HEIGHT, alpha);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
        Matrix4f mat2 = poseStack.last().pose();
        addBeamQuad(buffer, mat2, -BEAM_WIDTH, BEAM_WIDTH, BEAM_HEIGHT, alpha);
    }

    private void addBeamQuad(VertexConsumer buffer, Matrix4f mat, float left, float right, float height, int alpha) {
        buffer.addVertex(mat, left, 0f, 0f).setColor(255, 255, 255, alpha);
        buffer.addVertex(mat, right, 0f, 0f).setColor(255, 255, 255, alpha);
        buffer.addVertex(mat, right, height, 0f).setColor(255, 255, 255, alpha);
        buffer.addVertex(mat, left, height, 0f).setColor(255, 255, 255, alpha);
    }
    private void renderImpactRings(float t, PoseStack poseStack, VertexConsumer buffer) {
        float lingerT = t - PerunOrbitalStrikeEntity.IMPACT_TICK;
        float lingerProgress = Mth.clampF(lingerT / PerunOrbitalStrikeEntity.LINGER_TICKS, 0f, 1f);

        Matrix4f mat = poseStack.last().pose();
        float flashProgress = Mth.clampF(lingerT / 4f, 0f, 1f);
        int flashAlpha = (int) (Mth255(1f - flashProgress) * 0.8f);
        if (flashAlpha > 2) {
            addDisc(buffer, mat, MAX_RING_RADIUS * 0.6f, 0.05f, 255, 255, 255, flashAlpha);
        }
        for (float delayFraction : RING_DELAY_FRACTIONS) {
            float delayTicks = delayFraction * PerunOrbitalStrikeEntity.LINGER_TICKS;
            float local = lingerT - delayTicks;
            if (local < 0f) continue;
            float localProgress = Mth.clampF(local / (PerunOrbitalStrikeEntity.LINGER_TICKS * (1f - delayFraction)), 0f, 1f);
            float radius = MAX_RING_RADIUS * easeOutCubic(localProgress);
            int alpha = (int) (Mth255(1f - localProgress) * 0.9f);
            if (alpha <= 2) continue;

            float innerRadius = Math.max(0f, radius - RING_THICKNESS);
            addRing(buffer, mat, innerRadius, radius, 0.05f, RING_SEGMENTS, 255, 255, 255, alpha);
        }
    }

    private void addDisc(VertexConsumer buffer, Matrix4f mat, float radius, float y,
                         int r, int g, int b, int alpha) {
        addRing(buffer, mat, 0f, radius, y, RING_SEGMENTS, r, g, b, alpha);
    }

    private void addRing(VertexConsumer buffer, Matrix4f mat, float innerRadius, float outerRadius, float y,
                         int segments, int r, int g, int b, int alpha) {
        for (int i = 0; i < segments; i++) {
            float a1 = (float) (2 * Math.PI * i / segments);
            float a2 = (float) (2 * Math.PI * (i + 1) / segments);
            float ix1 = Mth.cos(a1) * innerRadius, iz1 = Mth.sin(a1) * innerRadius;
            float ox1 = Mth.cos(a1) * outerRadius, oz1 = Mth.sin(a1) * outerRadius;
            float ix2 = Mth.cos(a2) * innerRadius, iz2 = Mth.sin(a2) * innerRadius;
            float ox2 = Mth.cos(a2) * outerRadius, oz2 = Mth.sin(a2) * outerRadius;
            buffer.addVertex(mat, ix1, y, iz1).setColor(r, g, b, alpha);
            buffer.addVertex(mat, ox1, y, oz1).setColor(r, g, b, alpha);
            buffer.addVertex(mat, ox2, y, oz2).setColor(r, g, b, alpha);
            buffer.addVertex(mat, ix2, y, iz2).setColor(r, g, b, alpha);
        }
    }

    private static float Mth255(float f) {
        return Math.max(0f, Math.min(1f, f)) * 255f;
    }

    private static float easeOutCubic(float x) {
        float f = 1f - x;
        return 1f - f * f * f;
    }

    private static final class Mth {
        static float clampF(float value, float min, float max) {
            return net.minecraft.util.Mth.clamp(value, min, max);
        }
        static float cos(float angle) {
            return net.minecraft.util.Mth.cos(angle);
        }
        static float sin(float angle) {
            return net.minecraft.util.Mth.sin(angle);
        }
    }
}