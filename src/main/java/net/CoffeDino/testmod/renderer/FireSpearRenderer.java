package net.CoffeDino.testmod.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.CoffeDino.testmod.entity.FireSpearEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class FireSpearRenderer extends EntityRenderer<FireSpearEntity> {

    public FireSpearRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(FireSpearEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        float yRot = Mth.lerp(partialTick, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTick, entity.xRotO, entity.getXRot());

        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        poseStack.mulPose(Axis.XP.rotationDegrees(160));
        poseStack.mulPose(Axis.ZP.rotationDegrees(90));

        poseStack.scale(2.0F, 2.0F, 2.0F);

        boolean stuck = entity.isInGround() || entity.isStuckInEntity();
        boolean flying = !stuck;

        if (stuck) {
            applySpearInGroundPose(poseStack);
        } else if (flying) {
            applySpearInGroundPose(poseStack);
        }

        var itemRenderer = Minecraft.getInstance().getItemRenderer();

        itemRenderer.renderStatic(
                entity.getSpearItem(),
                ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                buffer,
                entity.level(),
                entity.getId()
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private void applySpearInGroundPose(PoseStack poseStack) {
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.translate(0.0D, -0.2D, 0.1D);
    }


    private ItemStack getRenderStack(FireSpearEntity entity) {
        ItemStack originalStack = entity.getSpearItem();

        if (entity.isCharged()) {
            ItemStack chargedStack = originalStack.copy();
            var data = chargedStack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
            net.minecraft.nbt.CompoundTag tag = data.isEmpty() ? new net.minecraft.nbt.CompoundTag() : data.copyTag();
            tag.putBoolean("Charged", true);
            chargedStack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(tag));
            return chargedStack;
        }

        return originalStack;
    }


    @Override
    public ResourceLocation getTextureLocation(FireSpearEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}