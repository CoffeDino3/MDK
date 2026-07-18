package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.BorontAvatarEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

public class BorontAvatarRenderer extends EntityRenderer<BorontAvatarEntity> {

    private static final ResourceLocation FALLBACK_SKIN = ResourceLocation.withDefaultNamespace("textures/entity/player/wide/steve.png");
    private static final float GIANT_SCALE = 10.0f;
    private static final int SWING_ANIM_TICKS = 16;
    private static final float SWEEP_ANGLE_RADIANS = 1.3f;
    private static final int BODY_TINT_RGB = 0xB080FF;
    private static final int BODY_ALPHA = 90;
    private static final int GLOW_TINT_RGB = 0xD9B8FF;
    private static final int GLOW_ALPHA = 70;
    private static final float GLOW_SCALE_MULT = 1.06f;
    private static final int ITEM_GLOW_TINT_RGB = 0xD9B8FF;
    private static final int ITEM_GLOW_ALPHA = 140;
    private static final float ITEM_GLOW_SCALE = 1.15f;
    private static final int FULL_BRIGHT_LIGHT = 0xF000F0;

    private final PlayerModel<LivingEntity> model;

    public BorontAvatarRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        this.model.young = false;
    }

    private void applyIdlePose(BorontAvatarEntity entity, float partialTick) {
        float age = entity.tickCount + partialTick;
        float sway = Mth.sin(age * 0.05f);

        model.rightArm.zRot = 0.1f + sway * 0.03f;
        model.leftArm.zRot = -0.1f - sway * 0.03f;
        model.body.yRot = sway * 0.02f;
    }

    @Override
    public void render(BorontAvatarEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotationDegrees(180f - entity.getYRot()));
        poseStack.translate(0.0D, 6.501D, 1.5D);
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(GIANT_SCALE, GIANT_SCALE, GIANT_SCALE);

        model.leftLeg.visible = false;
        model.rightLeg.visible = false;
        model.hat.visible = false;

        applyIdlePose(entity, partialTick);
        applySwingPose(entity, partialTick);

        model.rightSleeve.copyFrom(model.rightArm);
        model.leftSleeve.copyFrom(model.leftArm);
        model.jacket.copyFrom(model.body);
        ResourceLocation texture = getTextureLocation(entity);

        poseStack.pushPose();
        poseStack.scale(GLOW_SCALE_MULT, GLOW_SCALE_MULT, GLOW_SCALE_MULT);
        var glowConsumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(texture));
        int glowArgb = (GLOW_ALPHA << 24) | (GLOW_TINT_RGB & 0xFFFFFF);
        model.renderToBuffer(poseStack, new TintingVertexConsumer(glowConsumer, glowArgb), packedLight, OverlayTexture.NO_OVERLAY, glowArgb);
        poseStack.popPose();

        var bodyConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));
        int bodyArgb = (BODY_ALPHA << 24) | (BODY_TINT_RGB & 0xFFFFFF);
        model.renderToBuffer(poseStack, new TintingVertexConsumer(bodyConsumer, bodyArgb), packedLight, OverlayTexture.NO_OVERLAY, bodyArgb);

        renderHeldItems(entity, partialTick, poseStack, buffer, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    private float swingProgress(BorontAvatarEntity entity, float partialTick) {
        int ticksLeft = entity.getSwingTicksLeft();
        if (ticksLeft <= 0) return 0f;
        float progress = (SWING_ANIM_TICKS - ticksLeft + (1 - partialTick)) / SWING_ANIM_TICKS;
        return Mth.clamp(progress, 0f, 1f);
    }

    private boolean isSwinging(BorontAvatarEntity entity) {
        return entity.getSwingTicksLeft() > 0;
    }

    private float lateralSweep(BorontAvatarEntity entity, float partialTick) {
        if (!isSwinging(entity)) return 0f;
        float progress = swingProgress(entity, partialTick);
        return Mth.lerp(progress, -SWEEP_ANGLE_RADIANS, SWEEP_ANGLE_RADIANS);
    }

    private void applySwingPose(BorontAvatarEntity entity, float partialTick) {
        model.head.xRot = entity.getXRot() * ((float) Math.PI / 180f);

        if (!isSwinging(entity)) {
            model.rightArm.xRot = 0f;
            model.rightArm.yRot = 0f;
            model.leftArm.xRot = 0f;
            model.leftArm.yRot = 0f;
            return;
        }

        model.rightArm.xRot = -1.4f;
        model.rightArm.yRot = lateralSweep(entity, partialTick);
        model.rightArm.zRot = 0f;
        model.leftArm.xRot = -0.3f;
    }

    private static final float SWING_BLADE_DOWN_TILT = 1.5f;

    private void renderHeldItems(BorontAvatarEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        Player owner = getOwnerPlayer(entity);
        if (owner == null) return;

        boolean swinging = isSwinging(entity);
        float swingDelta = swinging ? -1.4f : 0f;

        model.rightArm.xRot = -80.3f + swingDelta;
        model.rightArm.yRot = lateralSweep(entity, partialTick);
        model.rightArm.zRot = 0f;

        model.leftArm.xRot = swinging ? -0.3f : 0f;
        model.leftArm.yRot = 0f;
        model.leftArm.zRot = 0f;

        renderHeldItem(owner.getMainHandItem(), model.rightArm, true, swinging, poseStack, buffer, packedLight, entity);
        renderHeldItem(owner.getOffhandItem(), model.leftArm, false, swinging, poseStack, buffer, packedLight, entity);
    }

    private void renderHeldItem(ItemStack stack, ModelPart arm, boolean rightHand, boolean swinging, PoseStack poseStack, MultiBufferSource buffer, int packedLight, BorontAvatarEntity entity) {
        if (stack.isEmpty()) return;

        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemDisplayContext ctx = rightHand ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;

        poseStack.pushPose();
        arm.translateAndRotate(poseStack);
        poseStack.translate((rightHand ? -2 : 0.1) * 0.0625D, -0.02D, -0.6625D);

        if (swinging) {
            poseStack.mulPose(Axis.XP.rotation(SWING_BLADE_DOWN_TILT));
        }

        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.scale(ITEM_GLOW_SCALE, ITEM_GLOW_SCALE, ITEM_GLOW_SCALE);

        int glowArgb = (ITEM_GLOW_ALPHA << 24) | (ITEM_GLOW_TINT_RGB & 0xFFFFFF);
        MultiBufferSource glowBuffer = new TintingBufferSource(buffer, glowArgb);
        itemRenderer.renderStatic(stack, ctx, FULL_BRIGHT_LIGHT, OverlayTexture.NO_OVERLAY, poseStack, glowBuffer, entity.level(), entity.getId());
        poseStack.popPose();
    }

    private Player getOwnerPlayer(BorontAvatarEntity entity) {
        if (Minecraft.getInstance().level == null) return null;
        UUID ownerUUID = entity.getOwnerUUID();
        if (ownerUUID == null) return null;
        return Minecraft.getInstance().level.getPlayerByUUID(ownerUUID);
    }

    @Override
    public ResourceLocation getTextureLocation(BorontAvatarEntity entity) {
        Player owner = getOwnerPlayer(entity);
        if (owner instanceof AbstractClientPlayer clientPlayer) {
            PlayerSkin skin = clientPlayer.getSkin();
            if (skin != null) return skin.texture();
        }
        return FALLBACK_SKIN;
    }

    private static final class TintingVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final int tintR, tintG, tintB, tintA;

        TintingVertexConsumer(VertexConsumer delegate, int argb) {
            this.delegate = delegate;
            this.tintA = (argb >> 24) & 0xFF;
            this.tintR = (argb >> 16) & 0xFF;
            this.tintG = (argb >> 8) & 0xFF;
            this.tintB = argb & 0xFF;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            return delegate.addVertex(x, y, z);
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            return delegate.setColor(tintR, tintG, tintB, tintA);
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            return delegate.setUv(u, v);
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            return delegate.setUv1(u, v);
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            return delegate.setUv2(u, v);
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            return delegate.setNormal(x, y, z);
        }
    }

    private static final class TintingBufferSource implements MultiBufferSource {
        private final MultiBufferSource delegate;
        private final int tintArgb;

        TintingBufferSource(MultiBufferSource delegate, int tintArgb) {
            this.delegate = delegate;
            this.tintArgb = tintArgb;
        }

        @Override
        public VertexConsumer getBuffer(RenderType renderType) {
            return new TintingVertexConsumer(delegate.getBuffer(renderType), tintArgb);
        }
    }
}