package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.BoreasStormEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

@EventBusSubscriber(modid = "lunacy", value = Dist.CLIENT)
public class BoreasRingRenderer {

    private static final ResourceLocation RING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("lunacy", "textures/entity/wind_ring.png");
    private static final float RING_SIZE = 3.4f;
    private static final float HEIGHT_ABOVE_HEAD = 1.6f;
    private static final float ROTATION_SPEED_DEG_PER_TICK = 1.2f;

    private static final int GUST_PARTICLES_PER_DRIP = 3;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 camPos = camera.getPosition();
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);

        boolean drewAny = false;

        for (Entity renderedEntity : level.entitiesForRendering()) {
            if (!(renderedEntity instanceof BoreasStormEntity storm)) continue;

            List<LivingEntity> targets = storm.getAffectedMobs();
            for (LivingEntity target : targets) {
                Vec3 headPos = target.getPosition(partialTick)
                        .add(0, target.getEyeHeight(target.getPose()) + HEIGHT_ABOVE_HEAD, 0);
                float age = storm.tickCount + partialTick;
                renderRing(poseStack, bufferSource, camPos, headPos, age);
                drewAny = true;
                if (level.random.nextFloat() < BoreasStormEntity.GUST_DROP_CHANCE_CLIENT) {
                    spawnGustDrip(level, headPos, target);
                }
            }
        }

        if (drewAny) {
            bufferSource.endBatch(RenderType.eyes(RING_TEXTURE));
        }
    }

    private static void spawnGustDrip(ClientLevel level, Vec3 ringPos, LivingEntity target) {
        Vec3 targetHead = target.position().add(0, target.getEyeHeight(target.getPose()), 0);
        Vec3 fall = targetHead.subtract(ringPos).scale(1.0 / 12.0);
        for (int i = 0; i < GUST_PARTICLES_PER_DRIP; i++) {
            double spawnX = ringPos.x + (level.random.nextDouble() - 0.5) * 1.2;
            double spawnZ = ringPos.z + (level.random.nextDouble() - 0.5) * 1.2;
            double dx = target.getX() - spawnX;
            double dz = target.getZ() - spawnZ;
            level.addParticle(
                    ParticleTypes.CLOUD,
                    spawnX,
                    ringPos.y,
                    spawnZ,
                    dx * 0.08,
                    -0.12,
                    dz * 0.08
            );
        }
    }

    private static void renderRing(PoseStack poseStack, MultiBufferSource bufferSource,
                                   Vec3 camPos, Vec3 worldPos, float age) {
        poseStack.pushPose();
        poseStack.translate(worldPos.x - camPos.x, worldPos.y - camPos.y, worldPos.z - camPos.z);
        poseStack.mulPose(new Quaternionf().rotationY(
                (float) Math.toRadians(age * ROTATION_SPEED_DEG_PER_TICK)));

        poseStack.scale(RING_SIZE, RING_SIZE, RING_SIZE);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.eyes(RING_TEXTURE));
        Matrix4f mat = poseStack.last().pose();
        int light = 15728880;
        buffer.addVertex(mat, -0.5f, 0f, -0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 1f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, 1, 0);

        buffer.addVertex(mat, 0.5f, 0f, -0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(1f, 1f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, 1, 0);

        buffer.addVertex(mat, 0.5f, 0f, 0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(1f, 0f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, 1, 0);

        buffer.addVertex(mat, -0.5f, 0f, 0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 0f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, 1, 0);

        buffer.addVertex(mat, -0.5f, 0f, 0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 0f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, -1, 0);

        buffer.addVertex(mat, 0.5f, 0f, 0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(1f, 0f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, -1, 0);

        buffer.addVertex(mat, 0.5f, 0f, -0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(1f, 1f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, -1, 0);

        buffer.addVertex(mat, -0.5f, 0f, -0.5f)
                .setColor(255, 255, 255, 255)
                .setUv(0f, 1f)
                .setOverlay(0)
                .setLight(light)
                .setNormal(0, -1, 0);


        poseStack.popPose();
    }
}