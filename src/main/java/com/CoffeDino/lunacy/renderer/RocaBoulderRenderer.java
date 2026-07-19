package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.RocaBoulderEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class RocaBoulderRenderer extends EntityRenderer<RocaBoulderEntity> {

    public RocaBoulderRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RocaBoulderEntity entity, float yaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        poseStack.pushPose();
        poseStack.translate(-0.5, 0, -0.5);
        var dispatcher = net.minecraft.client.Minecraft.getInstance().getBlockRenderer();
        dispatcher.renderSingleBlock(state, poseStack, buffer, packedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public net.minecraft.resources.ResourceLocation getTextureLocation(RocaBoulderEntity entity) {
        return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;
    }
}