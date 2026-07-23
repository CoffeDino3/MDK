package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.item.Custom.PhaetonItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class PhaetonWingsLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation WINGS_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("lunacy", "textures/entity/phaeton_wings.png");

    private static final float WIDTH = 4.5f;
    private static final float HEIGHT = 4.5f;
    private static final float BACK_OFFSET = 0.3f;
    private static final float VERTICAL_OFFSET = -2.5f;

    public PhaetonWingsLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {

        long launchEnd = PhaetonItem.getClientLaunchEnd();
        long riseEnd = PhaetonItem.getClientRiseEnd();
        boolean diving = PhaetonItem.isClientDiving();

        if (launchEnd == 0 && riseEnd == 0 && !diving) return;

        poseStack.pushPose();
        this.getParentModel().body.translateAndRotate(poseStack);
        poseStack.translate(0, VERTICAL_OFFSET, BACK_OFFSET);
        poseStack.mulPose(Axis.YP.rotationDegrees(180));

        VertexConsumer buffer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(WINGS_TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        float halfW = WIDTH / 2f;

        vertex(buffer, mat, -halfW, 0, 0, 0, 0, packedLight);
        vertex(buffer, mat, halfW, 0, 0, 1, 0, packedLight);
        vertex(buffer, mat, halfW, HEIGHT, 0, 1, 1, packedLight);
        vertex(buffer, mat, -halfW, HEIGHT, 0, 0, 1, packedLight);

        poseStack.popPose();
    }

    private static void vertex(VertexConsumer buffer, Matrix4f mat, float x, float y, float z,
                               float u, float v, int light) {
        buffer.addVertex(mat, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, 0, 1);
    }
}