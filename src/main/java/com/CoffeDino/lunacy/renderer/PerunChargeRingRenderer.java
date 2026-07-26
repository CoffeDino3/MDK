package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.PerunSkyBeamEntity;
import com.CoffeDino.lunacy.item.Custom.PerunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

@EventBusSubscriber(modid = "lunacy", value = Dist.CLIENT)
public class PerunChargeRingRenderer {

    private static final ResourceLocation RING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("lunacy", "textures/entity/thunder_ring.png");
    private static final int RING_COUNT = PerunItem.RING_COUNT;
    private static final int TICKS_PER_RING = PerunItem.TICKS_PER_RING;
    private static final float BASE_SIZE = 2.6f;
    private static final float SIZE_STEP = 1.0f;
    private static final float BASE_HEIGHT_ABOVE_HEAD = 2.3f;
    private static final float HEIGHT_STEP = 0.8f;
    private static final float ROTATION_SPEED_DEG_PER_TICK = 6.0f;
    private static final int RING_POP_IN_TICKS = 4;

    public static float ringHeightAboveHead(int ringIndex) {
        return BASE_HEIGHT_ABOVE_HEAD + HEIGHT_STEP * ringIndex;
    }
    public static float topRingHeightAboveHead() {
        return ringHeightAboveHead(RING_COUNT - 1);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        boolean drewAny = false;
        drewAny |= renderChargingRings(level, poseStack, bufferSource, camPos, partialTick);
        drewAny |= renderLingeringRings(level, poseStack, bufferSource, camPos, partialTick);
        if (drewAny) {
            bufferSource.endBatch(RenderType.eyes(RING_TEXTURE));
        }
    }
    private static boolean renderChargingRings(ClientLevel level, PoseStack poseStack,
                                               MultiBufferSource.BufferSource bufferSource,
                                               Vec3 camPos, float partialTick) {
        boolean drewAny = false;
        for (Entity renderedEntity : level.entitiesForRendering()) {
            if (!(renderedEntity instanceof LivingEntity entity)) continue;
            if (!entity.isUsingItem()) continue;
            if (!(entity.getUseItem().getItem() instanceof PerunItem)) continue;
            int chargedTicks = entity.getTicksUsingItem();
            int ringCount = Math.min(RING_COUNT, 1 + chargedTicks / TICKS_PER_RING);
            Vec3 headPos = entity.getPosition(partialTick)
                    .add(0, entity.getEyeHeight(entity.getPose()), 0);
            for (int i = 0; i < ringCount; i++) {
                int ringAge = chargedTicks - (i * TICKS_PER_RING);
                float growIn = Mth.clamp(ringAge / (float) RING_POP_IN_TICKS, 0f, 1f);
                if (growIn <= 0f) continue;

                float ringSize = (BASE_SIZE + SIZE_STEP * i) * growIn;
                Vec3 ringPos = headPos.add(0, ringHeightAboveHead(i), 0);

                float age = entity.tickCount + partialTick;
                renderRing(poseStack, bufferSource, camPos, ringPos, ringSize, age, i, 255);
                drewAny = true;
            }
        }

        return drewAny;
    }

    private static boolean renderLingeringRings(ClientLevel level, PoseStack poseStack,
                                                MultiBufferSource.BufferSource bufferSource,
                                                Vec3 camPos, float partialTick) {
        boolean drewAny = false;
        float topRingHeight = topRingHeightAboveHead();

        for (Entity renderedEntity : level.entitiesForRendering()) {
            if (!(renderedEntity instanceof PerunSkyBeamEntity skyBeam)) continue;

            float visibility = skyBeam.getVisibility(partialTick);
            if (visibility <= 0f) continue;
            Vec3 beamBase = skyBeam.getPosition(partialTick);
            Vec3 headPos = beamBase.subtract(0, topRingHeight, 0);

            int alpha = (int) Mth.clamp(visibility * 255f, 0f, 255f);
            float age = skyBeam.tickCount + partialTick;
            for (int i = 0; i < RING_COUNT; i++) {
                Vec3 ringPos = headPos.add(0, ringHeightAboveHead(i), 0);
                float ringSize = BASE_SIZE + SIZE_STEP * i;
                renderRing(poseStack, bufferSource, camPos, ringPos, ringSize, age, i, alpha);
                drewAny = true;
            }
        }

        return drewAny;
    }

    private static void renderRing(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Vec3 camPos, Vec3 worldPos, float size, float age, int ringIndex, int alpha) {
        poseStack.pushPose();
        poseStack.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
        float spinDir = (ringIndex % 2 == 0) ? 1f : -1f;
        poseStack.mulPose(new Quaternionf().rotationY(
                (float) Math.toRadians(age * ROTATION_SPEED_DEG_PER_TICK * spinDir)));
        poseStack.scale(size, size, size);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.eyes(RING_TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        int light = 15728880;
        buffer.addVertex(mat, -0.5f, 0f, -0.5f).setColor(255, 255, 255, alpha).setUv(0f, 1f)
                .setOverlay(0).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(mat, 0.5f, 0f, -0.5f).setColor(255, 255, 255, alpha).setUv(1f, 1f)
                .setOverlay(0).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(mat, 0.5f, 0f, 0.5f).setColor(255, 255, 255, alpha).setUv(1f, 0f)
                .setOverlay(0).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(mat, -0.5f, 0f, 0.5f).setColor(255, 255, 255, alpha).setUv(0f, 0f)
                .setOverlay(0).setLight(light).setNormal(0, 1, 0);
        buffer.addVertex(mat, -0.5f, 0f, 0.5f).setColor(255, 255, 255, alpha).setUv(0f, 0f)
                .setOverlay(0).setLight(light).setNormal(0, -1, 0);
        buffer.addVertex(mat, 0.5f, 0f, 0.5f).setColor(255, 255, 255, alpha).setUv(1f, 0f)
                .setOverlay(0).setLight(light).setNormal(0, -1, 0);
        buffer.addVertex(mat, 0.5f, 0f, -0.5f).setColor(255, 255, 255, alpha).setUv(1f, 1f)
                .setOverlay(0).setLight(light).setNormal(0, -1, 0);
        buffer.addVertex(mat, -0.5f, 0f, -0.5f).setColor(255, 255, 255, alpha).setUv(0f, 1f)
                .setOverlay(0).setLight(light).setNormal(0, -1, 0);

        poseStack.popPose();
    }
}