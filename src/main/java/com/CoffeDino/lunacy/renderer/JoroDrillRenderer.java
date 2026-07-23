package com.CoffeDino.lunacy.renderer;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public final class JoroDrillRenderer {

    private static final int STRANDS = 5;
    private static final double DRILL_LENGTH = 1.25;
    private static final double BASE_RADIUS = 0.5;
    private static final double SPINS_PER_SECOND = 2.5;
    private static final double SPIRAL_TURNS = 2.0;
    private static final float PARTICLE_SCALE = 0.7f;

    private JoroDrillRenderer() {}

    public static void spawnVortexTick(LivingEntity entity, int elapsedTicks) {
        Level level = entity.level();
        if (!level.isClientSide) return;

        boolean rightArmRaised = (entity.getUsedItemHand() == InteractionHand.MAIN_HAND)
                == (entity.getMainArm() == HumanoidArm.RIGHT);
        double sideSign = rightArmRaised ? 1 : -1;

        float yawRad = entity.yBodyRot * Mth.DEG_TO_RAD;
        Vec3 flatLook = new Vec3(-Mth.sin(yawRad), 0, Mth.cos(yawRad));
        Vec3 rightVec = flatLook.cross(new Vec3(0, 1, 0)).normalize();
        float swingProgress = 0;
        if (entity instanceof Player player) {
            swingProgress = player.getAttackStrengthScale(0.5f);
        }
        double swingAngle = swingProgress * Math.PI * 2;
        double wobbleX = Math.sin(swingAngle) * 0.15;
        double wobbleZ = Math.cos(swingAngle) * 0.08;
        double wobbleY = Math.sin(swingAngle * 0.7) * 0.05;
        Vec3 shoulder = entity.position()
                .add(0, entity.getBbHeight() * 1.25, 0)
                .add(rightVec.scale(sideSign * 0.29 + wobbleX * sideSign))
                .add(flatLook.scale(-0.1 + wobbleZ))
                .add(0, wobbleY, 0);

        double axisWobble = Math.sin(swingAngle * 0.8) * 0.04;
        Vec3 axis = new Vec3(
                flatLook.x * (0.06 + axisWobble),
                1.0 + Math.sin(swingAngle * 0.5) * 0.03,
                flatLook.z * (0.15 + axisWobble)
        ).normalize();
        Vec3 ortho1 = axis.cross(new Vec3(0, 0, 1));
        if (ortho1.lengthSqr() < 1e-4) ortho1 = axis.cross(new Vec3(1, 0, 0));
        ortho1 = ortho1.normalize();
        Vec3 ortho2 = axis.cross(ortho1).normalize();
        double timeSeconds = elapsedTicks / 20.0;
        double spin = timeSeconds * SPINS_PER_SECOND * Math.PI * 2;
        DustParticleOptions dust = new DustParticleOptions(new Vector3f(0.45f, 0.25f, 0.1f), PARTICLE_SCALE);

        for (int s = 0; s < STRANDS; s++) {
            double strandOffset = (Math.PI * 2 / STRANDS) * s;

            for (double t = 0; t <= 1.0; t += 0.1) {
                double height = t * DRILL_LENGTH;
                double spiralWobble = Math.sin(swingAngle * 1.5 + t * 3) * 0.03;
                double spiralAngle = strandOffset + spin + t * SPIRAL_TURNS * Math.PI * 2 + spiralWobble;
                double radiusAtHeight = (1 - t) * BASE_RADIUS;
                Vec3 offset = ortho1.scale(Math.cos(spiralAngle) * radiusAtHeight)
                        .add(ortho2.scale(Math.sin(spiralAngle) * radiusAtHeight));

                Vec3 pos = shoulder.add(axis.scale(height)).add(offset);

                level.addParticle(dust, pos.x, pos.y, pos.z, 0, 0, 0);
            }
        }
    }
}