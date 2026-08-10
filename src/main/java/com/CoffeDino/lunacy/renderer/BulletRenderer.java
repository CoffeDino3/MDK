package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.BulletEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class BulletRenderer extends ThrownItemRenderer<BulletEntity> {

    private final ItemRenderer itemRenderer;

    public BulletRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0f, true);
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(BulletEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float yaw = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch));

        itemRenderer.renderStatic(entity.getItem(), ItemDisplayContext.GROUND,
                packedLight, OverlayTexture.NO_OVERLAY,
                poseStack, buffer, entity.level(), entity.getId());

        poseStack.popPose();
    }

    @Override
    public Vec3 getRenderOffset(BulletEntity entity, float partialTicks) {
        return new Vec3(0, -0.1, 0);
    }
}