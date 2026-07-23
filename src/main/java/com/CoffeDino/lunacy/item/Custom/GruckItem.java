package com.CoffeDino.lunacy.item.Custom;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GruckItem extends ShieldItem {

    private static final double BEAM_RANGE = 30.0;
    private static final float KNOCKBACK_STRENGTH = 1.5F;
    private static final double HELIX_RADIUS = 0.2;
    private static final double HELIX_TURNS = 4.0;

    private static final float MIDAIR_KNOCKBACK_STRENGTH = 1.5F;
    private static final String NBT_NO_FALL_DAMAGE = "GruckNoFallDamage";
    private static final int MAX_MIDAIR_CHARGES = 3;
    private static final long CHARGE_REGEN_TICKS = 60;

    private static final String NBT_CHARGES = "GruckCharges";
    private static final String NBT_LAST_REGEN_TICK = "GruckLastRegenTick";

    public GruckItem(Properties properties) {
        super(properties);
    }
    private static int getAndUpdateCharges(ServerLevel level, Player player) {
        var data = player.getPersistentData();
        long now = level.getGameTime();

        int charges;
        long lastRegen;

        if (!data.contains(NBT_CHARGES)) {
            charges = MAX_MIDAIR_CHARGES;
            lastRegen = now;
        } else {
            charges = data.getInt(NBT_CHARGES);
            lastRegen = data.getLong(NBT_LAST_REGEN_TICK);

            if (charges < MAX_MIDAIR_CHARGES) {
                long elapsed = now - lastRegen;
                long gained = elapsed / CHARGE_REGEN_TICKS;
                if (gained > 0) {
                    charges = (int) Math.min(MAX_MIDAIR_CHARGES, charges + gained);
                    lastRegen += gained * CHARGE_REGEN_TICKS;
                }
            } else {
                lastRegen = now;
            }
        }

        data.putInt(NBT_CHARGES, charges);
        data.putLong(NBT_LAST_REGEN_TICK, lastRegen);
        return charges;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        boolean midair = !player.onGround() && !player.getAbilities().flying && !player.onClimbable();

        if (midair) {
            if (level instanceof ServerLevel serverLevel) {
                int charges = getAndUpdateCharges(serverLevel, player);

                if (charges <= 0) {
                    return InteractionResultHolder.fail(stack);
                }

                player.getPersistentData().putInt(NBT_CHARGES, charges - 1);

                fireBeam(serverLevel, player, 10.0F);

                Vec3 look = player.getViewVector(1.0F);
                Vec3 knockback = look.scale(-MIDAIR_KNOCKBACK_STRENGTH);
                player.setDeltaMovement(knockback);
                player.hurtMarked = true;

                player.getPersistentData().putBoolean(NBT_NO_FALL_DAMAGE, true);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }

    public static void fireBeam(ServerLevel level, LivingEntity blocker, float beamDamage) {
        Vec3 start = getShieldCenter(blocker);
        Vec3 look = blocker.getViewVector(1.0F);
        Vec3 rawEnd = start.add(look.scale(BEAM_RANGE));

        BlockHitResult blockHit = level.clip(new ClipContext(
                start, rawEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, blocker));
        double maxDist = blockHit.getType() != HitResult.Type.MISS
                ? start.distanceTo(blockHit.getLocation())
                : BEAM_RANGE;
        Vec3 end = start.add(look.scale(maxDist));

        AABB searchBox = blocker.getBoundingBox().expandTowards(look.scale(BEAM_RANGE)).inflate(2.0);
        List<Entity> candidates = level.getEntities(blocker, searchBox,
                e -> e instanceof LivingEntity le && le.isAlive() && le != blocker);

        List<Vec3> impactPoints = new ArrayList<>();
        for (Entity candidate : candidates) {
            AABB hitbox = candidate.getBoundingBox().inflate(1.0);
            Optional<Vec3> hit = hitbox.clip(start, end);
            if (hit.isEmpty()) continue;

            LivingEntity target = (LivingEntity) candidate;
            target.hurt(blocker.damageSources().magic(), beamDamage);
            target.knockback(KNOCKBACK_STRENGTH,
                    blocker.getX() - target.getX(),
                    blocker.getZ() - target.getZ());
            impactPoints.add(candidate.position().add(0, candidate.getBbHeight() * 0.5, 0));
        }

        spawnBeamEffect(level, start, end, impactPoints);

        level.playSound(null, blocker.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                SoundSource.PLAYERS, 1.0F, 1.2F);
        level.playSound(null, blocker.blockPosition(), SoundEvents.EVOKER_CAST_SPELL,
                SoundSource.PLAYERS, 0.8F, 1.4F);
    }

    private static Vec3 getShieldCenter(LivingEntity blocker) {
        Vec3 look = blocker.getViewVector(1.0F);
        Vec3 flatLook = new Vec3(look.x, 0, look.z).normalize();
        double shieldY = blocker.getY() + blocker.getBbHeight() * 0.62;
        return new Vec3(blocker.getX(), shieldY, blocker.getZ()).add(flatLook.scale(0.55));
    }

    private static void spawnBeamEffect(ServerLevel level, Vec3 start, Vec3 end, List<Vec3> impactPoints) {
        // Clean color definitions
        DustParticleOptions whiteCore = new DustParticleOptions(new Vector3f(1.0F, 1.0F, 1.0F), 2.0F);
        DustParticleOptions redAura = new DustParticleOptions(new Vector3f(1.0F, 0.0F, 0.0F), 1.5F);
        DustParticleOptions redGlow = new DustParticleOptions(new Vector3f(1.0F, 0.2F, 0.0F), 1.2F);

        Vec3 diff = end.subtract(start);
        double length = diff.length();
        Vec3 forward = diff.normalize();

        Vec3 up = Math.abs(forward.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = forward.cross(up).normalize();
        Vec3 trueUp = right.cross(forward).normalize();

        int steps = (int) Math.max(20, length * 6);
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            Vec3 point = start.add(forward.scale(t * length));
            level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            if (i % 2 == 0) {
                level.sendParticles(whiteCore, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0.0);
            }
        }
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double angle = t * Math.PI * 2 * HELIX_TURNS;
            Vec3 base = start.add(forward.scale(t * length));
            Vec3 offset = right.scale(Math.cos(angle) * HELIX_RADIUS)
                    .add(trueUp.scale(Math.sin(angle) * HELIX_RADIUS));
            Vec3 point = base.add(offset);
            if (i % 2 == 0) {
                level.sendParticles(redAura, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            } else {
                level.sendParticles(redGlow, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        level.sendParticles(ParticleTypes.FLASH, start.x, start.y, start.z, 2, 0.0, 0.0, 0.0, 0.0);
        for (int i = 0; i < 20; i++) {
            double radius = 0.1 + (i / 20.0) * 0.3;
            for (int j = 0; j < 8; j++) {
                double angle = (j / 8.0) * Math.PI * 2;
                Vec3 offset = new Vec3(
                        Math.cos(angle) * radius,
                        Math.sin(angle) * radius * 0.5,
                        Math.sin(angle) * radius * 0.5
                );
                Vec3 pos = start.add(offset);
                level.sendParticles(whiteCore, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
        for (int i = 0; i < 16; i++) {
            double angle = (i / 16.0) * Math.PI * 2;
            Vec3 offset = new Vec3(
                    Math.cos(angle) * 0.35,
                    Math.sin(angle) * 0.2,
                    Math.sin(angle) * 0.35
            );
            Vec3 pos = start.add(offset);
            level.sendParticles(redAura, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        for (Vec3 impact : impactPoints) {
            level.sendParticles(ParticleTypes.FLASH, impact.x, impact.y, impact.z, 2, 0.0, 0.0, 0.0, 0.0);
            for (int i = 0; i < 30; i++) {
                double radius = 0.1 + (i / 30.0) * 0.5;
                for (int j = 0; j < 6; j++) {
                    double angle = (j / 6.0) * Math.PI * 2 + i * 0.5;
                    Vec3 offset = new Vec3(
                            Math.cos(angle) * radius,
                            Math.sin(angle * 1.5) * radius * 0.4,
                            Math.sin(angle) * radius
                    );
                    Vec3 pos = impact.add(offset);
                    level.sendParticles(whiteCore, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
            for (int i = 0; i < 20; i++) {
                double angle = (i / 20.0) * Math.PI * 2;
                Vec3 offset = new Vec3(
                        Math.cos(angle) * 0.4,
                        Math.sin(angle * 0.7) * 0.15,
                        Math.sin(angle) * 0.4
                );
                Vec3 pos = impact.add(offset);
                level.sendParticles(redAura, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                    impact.x, impact.y, impact.z,
                    3, 0.2, 0.2, 0.2, 0.0);
        }
    }
}