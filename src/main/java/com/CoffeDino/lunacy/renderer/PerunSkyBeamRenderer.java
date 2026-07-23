package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.PerunSkyBeamEntity;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class PerunSkyBeamRenderer extends EntityRenderer<PerunSkyBeamEntity> {

    private static final float BEAM_WIDTH = 0.35f;
    private static final RenderType BEAM_RENDER_TYPE = RenderType.create(
            "lunacy_sky_beam",
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

    public PerunSkyBeamRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(PerunSkyBeamEntity entity) {
        return ResourceLocation.withDefaultNamespace("missingno");
    }

    @Override
    public void render(PerunSkyBeamEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float visibility = entity.getVisibility(partialTick);
        if (visibility > 0f) {
            poseStack.pushPose();

            int alpha = (int) (visibility * 255f);
            float width = BEAM_WIDTH * (0.6f + 0.4f * visibility);
            float height = PerunSkyBeamEntity.BEAM_HEIGHT;

            VertexConsumer buffer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
            Matrix4f mat = poseStack.last().pose();
            addBeamQuad(buffer, mat, -width, width, height, alpha);
            poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(90));
            Matrix4f mat2 = poseStack.last().pose();
            addBeamQuad(buffer, mat2, -width, width, height, alpha);

            poseStack.popPose();
        }

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private void addBeamQuad(VertexConsumer buffer, Matrix4f mat,
                             float left, float right, float height, int alpha) {
        buffer.addVertex(mat, left, 0f, 0f).setColor(255, 255, 255, alpha);
        buffer.addVertex(mat, right, 0f, 0f).setColor(255, 255, 255, alpha);
        buffer.addVertex(mat, right, height, 0f).setColor(255, 255, 255, alpha);
        buffer.addVertex(mat, left, height, 0f).setColor(255, 255, 255, alpha);
    }
}