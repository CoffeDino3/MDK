package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.entity.MapleBoatEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MapleBoatRenderer extends EntityRenderer<MapleBoatEntity> {
    private static final ResourceLocation MAPLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/entity/maple_boat.png");
    private final BoatModel model;

    public MapleBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.8F;
        this.model = new BoatModel(BoatModel.createBodyModel().bakeRoot());
    }

    @Override
    public void render(MapleBoatEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.375D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(270.0F - entityYaw));
        float timeSinceHit = (float) entity.getHurtTime() - partialTicks;
        float damageTaken = entity.getDamage() - partialTicks;
        if (damageTaken < 0.0F) {
            damageTaken = 0.0F;
        }
        if (timeSinceHit > 0.0F) {
            poseStack.mulPose(Axis.XP.rotationDegrees(Mth.sin(timeSinceHit) * timeSinceHit * damageTaken / 10.0F * (float) entity.getHurtDir()));
        }
        float bubbleAngle = entity.getBubbleAngle(partialTicks);
        if (!Mth.equal(bubbleAngle, 0.0F)) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getBubbleAngle(partialTicks)));
        }
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        this.model.setupAnim(entity, partialTicks, 0.0F, -0.1F, 0.0F, 0.0F);
        VertexConsumer vertexconsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }


    @Override
    public ResourceLocation getTextureLocation(MapleBoatEntity entity) {
        return MAPLE_TEXTURE;
    }
}