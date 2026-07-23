package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AmphitriteOrbEntity extends Entity {
    private static final Vec3[] FORMATION_OFFSETS = new Vec3[] {
            new Vec3(0.30, 0.70, 1.1),
            new Vec3(0.55, 0.55, 1.2),
            new Vec3(0.15, 0.50, 1.3),
            new Vec3(0.40, 0.35, 1.25),
            new Vec3(0.65, 0.25, 1.35),
            new Vec3(0.10, 0.05, 1.6),
            new Vec3(-0.40, -0.05, 1.8),
            new Vec3(-0.85, -0.20, 2.0),
            new Vec3(-0.50, -0.45, 2.3),
            new Vec3(0.00, -0.60, 2.5)
    };
    private static final Vec3[] HYDRA = new Vec3[] {
            new Vec3(0.30, 0.70, 1.1),
            new Vec3(0.55, 0.55, 1.2),
            new Vec3(0.15, 0.50, 1.3),
            new Vec3(0.40, 0.35, 1.25),
            new Vec3(0.65, 0.25, 1.35),
            new Vec3(0.10, 0.05, 1.6),
            new Vec3(-0.40, -0.05, 1.8),
            new Vec3(-0.85, -0.20, 2.0),
            new Vec3(-0.50, -0.45, 2.3),
            new Vec3(0.00, -0.60, 2.5)
    };
    private static final Vec3[] URSA_MAJOR = new Vec3[] {
            new Vec3(-1.0, 0.6, 1.0),
            new Vec3(-0.4, 0.6, 1.0),
            new Vec3(-0.4, 0.3, 1.5),
            new Vec3(-1.0, 0.3, 1.5),
            new Vec3(-0.2, 0.35, 1.9),
            new Vec3(0.3, 0.15, 2.2),
            new Vec3(0.8, -0.05, 2.5)
    };
    private static final Vec3[] CASSIOPEIA = new Vec3[] {
            new Vec3(-1.2, 0.4, 1.3),
            new Vec3(-0.6, 0.65, 1.5),
            new Vec3(0.0, 0.4, 1.6),
            new Vec3(0.6, 0.65, 1.5),
            new Vec3(1.2, 0.4, 1.3)
    };
    private static final Vec3[] CRUX = new Vec3[] {
            new Vec3(0.0, 0.75, 1.4),
            new Vec3(0.0, 0.15, 1.6),
            new Vec3(-0.5, 0.45, 1.5),
            new Vec3(0.5, 0.45, 1.5)
    };
    private static final Vec3[] ORION_BELT = new Vec3[] {
            new Vec3(-0.7, 0.5, 1.5),
            new Vec3(0.0, 0.45, 1.6),
            new Vec3(0.7, 0.4, 1.7)
    };
    private static final Vec3[] DRACO = new Vec3[] {
            new Vec3(0.90, 0.65, 1.0),
            new Vec3(0.55, 0.70, 1.3),
            new Vec3(0.15, 0.55, 1.5),
            new Vec3(-0.25, 0.60, 1.7),
            new Vec3(-0.60, 0.40, 1.9),
            new Vec3(-0.35, 0.10, 2.1),
            new Vec3(0.05, -0.05, 2.3),
            new Vec3(0.45, -0.20, 2.5)
    };
    private static final Vec3[] SCORPIUS = new Vec3[] {
            new Vec3(0.80, 0.70, 1.0),
            new Vec3(0.55, 0.55, 1.2),
            new Vec3(0.35, 0.35, 1.4),
            new Vec3(0.20, 0.10, 1.6),
            new Vec3(0.10, -0.15, 1.8),
            new Vec3(0.00, -0.40, 2.0),
            new Vec3(-0.20, -0.55, 2.2),
            new Vec3(-0.45, -0.60, 2.35),
            new Vec3(-0.60, -0.40, 2.5)
    };
    private static final Vec3[] LEO = new Vec3[] {
            new Vec3(-0.90, 0.70, 1.2),
            new Vec3(-0.60, 0.55, 1.3),
            new Vec3(-0.75, 0.30, 1.5),
            new Vec3(-0.40, 0.35, 1.6),
            new Vec3(0.20, 0.30, 1.8),
            new Vec3(0.70, 0.10, 2.1)
    };
    private static final Vec3[] GEMINI_TWINS = new Vec3[] {
            new Vec3(-0.35, 0.55, 1.5),
            new Vec3(0.35, 0.55, 1.5)
    };
    private static final java.util.Map<Integer, Vec3[]> FORMATION_SETS = new java.util.HashMap<>();
    static {
        FORMATION_SETS.put(10, HYDRA);
        FORMATION_SETS.put(9, SCORPIUS);
        FORMATION_SETS.put(8, DRACO);
        FORMATION_SETS.put(7, URSA_MAJOR);
        FORMATION_SETS.put(6, LEO);
        FORMATION_SETS.put(5, CASSIOPEIA);
        FORMATION_SETS.put(4, CRUX);
        FORMATION_SETS.put(3, ORION_BELT);
        FORMATION_SETS.put(2, GEMINI_TWINS);
        FORMATION_SETS.put(1, new Vec3[]{ new Vec3(0.0, 0.6, 1.3) });
    }
    private static final double CHEST_HEIGHT = 1.3;
    private static final double FOLLOW_LERP = 0.25;
    private static final int MAX_FLOAT_LIFETIME = 2400;
    private static final double FORMATION_SEARCH_RADIUS = 6.0;
    private static final int ESCAPE_TICKS = 12;
    private static final double CLEAR_RADIUS = 1.8;
    private static final double ESCAPE_SPEED = 0.55;
    private static final double HOMING_SPEED = 0.9;
    private static final double HIT_RADIUS = 1.1;
    private static final int MAX_SEEK_LIFETIME = 100;
    private static final float DAMAGE_PERCENT_OF_MAX_HEALTH = 0.05f;
    private static final int MIN_HIT_GAP_TICKS = 20;
    private static final double ORBIT_RADIUS = 1.3;
    private static final double ORBIT_SPEED = 0.35;
    private static final java.util.Map<UUID, Long> lastHitGameTime = new java.util.HashMap<>();
    private static final int STATE_FLOATING = 0;
    private static final int STATE_SEEKING = 1;
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID =
            SynchedEntityData.defineId(AmphitriteOrbEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Optional<UUID>> DATA_TARGET_UUID =
            SynchedEntityData.defineId(AmphitriteOrbEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_SLOT =
            SynchedEntityData.defineId(AmphitriteOrbEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(AmphitriteOrbEntity.class, EntityDataSerializers.INT);
    private static final DustParticleOptions ORB_DUST = new DustParticleOptions(new Vector3f(0.25f, 0.55f, 0.95f), 1.2f);
    private static final DustParticleOptions THREAD_DUST = new DustParticleOptions(new Vector3f(0.55f, 0.8f, 1.0f), 0.6f);
    private int age;
    private int seekAge;
    private Vec3 escapeDirection = Vec3.ZERO;
    public AmphitriteOrbEntity(EntityType<? extends AmphitriteOrbEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
    }
    public AmphitriteOrbEntity(Level level, LivingEntity owner, int slot) {
        this(ModEntities.AMPHITRITE_ORB.get(), level);
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
        this.entityData.set(DATA_SLOT, slot);
        int remaining = countRemainingFloating(owner);
        this.setPos(formationPosition(owner, slot, remaining));
    }
    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_TARGET_UUID, Optional.empty());
        builder.define(DATA_SLOT, 0);
        builder.define(DATA_STATE, STATE_FLOATING);
    }
    @Override
    public void tick() {
        super.tick();
        LivingEntity owner = getOwner();
        if (this.level().isClientSide) {
            spawnOrbParticles();
            if (getState() == STATE_FLOATING) {
                spawnThreadToPrevious(owner);
            } else {
                spawnSeekingTrail();
            }
            return;
        }
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        if (getState() == STATE_FLOATING) {
            tickFloating(owner);
        } else {
            tickSeeking(owner);
        }
    }
    private int launchDelay = -1;

    private void tickFloating(LivingEntity owner) {
        if (launchDelay >= 0) {
            launchDelay--;
            if (launchDelay <= 0) {
                launchAt(getTarget());
                return;
            }
        }
        age++;
        if (age >= MAX_FLOAT_LIFETIME) {
            this.discard();
            return;
        }
        int remaining = countRemainingFloating(owner);
        Vec3 target = formationPosition(owner, getSlot(), remaining);
        Vec3 newPos = this.position().lerp(target, FOLLOW_LERP);
        this.setPos(newPos.x, newPos.y, newPos.z);
        this.hurtMarked = true;
    }

    private int countRemainingFloating(LivingEntity owner) {
        return this.level().getEntitiesOfClass(AmphitriteOrbEntity.class,
                this.getBoundingBox().inflate(FORMATION_SEARCH_RADIUS),
                orb -> owner.getUUID().equals(orb.getOwnerUUID()) && orb.isFloating()).size();
    }

    private static Vec3 formationPosition(LivingEntity owner, int slot, int remainingCount) {
        Vec3[] shape = FORMATION_SETS.getOrDefault(Math.max(1, remainingCount), HYDRA);
        Vec3 local = shape[Math.min(slot, shape.length - 1)];

        Vec3 base = owner.position().add(0, CHEST_HEIGHT, 0);
        double yawRad = Math.toRadians(owner.getYRot());
        double sinYaw = Mth.sin((float) yawRad);
        double cosYaw = Mth.cos((float) yawRad);
        Vec3 right = new Vec3(cosYaw, 0, sinYaw);
        Vec3 back = new Vec3(sinYaw, 0, -cosYaw);

        Vec3 offset = right.scale(local.x).add(0, local.y, 0).add(back.scale(local.z));
        return base.add(offset);
    }

    public void launchAt(LivingEntity target) {
        this.entityData.set(DATA_STATE, STATE_SEEKING);
        this.entityData.set(DATA_TARGET_UUID, Optional.ofNullable(target != null ? target.getUUID() : null));
        this.seekAge = 0;
        this.noPhysics = false;

        LivingEntity owner = getOwner();
        Vec3 away = owner != null ? this.position().subtract(owner.position()) : Vec3.ZERO;
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(this.random.nextDouble() - 0.5, 0, this.random.nextDouble() - 0.5);
        }
        away = away.normalize();

        double jitterYaw = (this.random.nextDouble() - 0.5) * Math.PI * 0.6;
        away = rotateY(away, jitterYaw).add(0, 0.4 + this.random.nextDouble() * 0.3, 0).normalize();
        this.escapeDirection = away;
    }
    public void scheduleLaunch(LivingEntity target, int delayTicks) {
        this.entityData.set(DATA_TARGET_UUID, Optional.ofNullable(target != null ? target.getUUID() : null));
        this.launchDelay = delayTicks;
    }

    private void tickSeeking(LivingEntity owner) {
        seekAge++;
        LivingEntity target = getTarget();

        Vec3 velocity;
        if (seekAge < ESCAPE_TICKS || isNearOwner(owner)) {
            velocity = escapeDirection.scale(ESCAPE_SPEED).add(randomJitter(0.05));
        } else if (target != null && target.isAlive()) {
            Vec3 toTarget = target.getEyePosition().subtract(this.position());
            double dist = toTarget.length();

            if (dist < HIT_RADIUS) {
                long now = this.level().getGameTime();
                Long last = lastHitGameTime.get(target.getUUID());
                if (last == null || now - last >= MIN_HIT_GAP_TICKS) {
                    hit(target);
                    return;
                }
                Vec3 orbit = orbitVelocity(target);
                this.setDeltaMovement(orbit);
                this.move(MoverType.SELF, orbit);
                this.hurtMarked = true;
                return;
            }
            Vec3 desired = toTarget.normalize().scale(HOMING_SPEED);
            Vec3 current = this.getDeltaMovement();
            double jitterStrength = dist > 3.0 ? 0.06 : 0.015;
            velocity = current.scale(0.65).add(desired.scale(0.45)).add(randomJitter(jitterStrength));
            if (velocity.lengthSqr() > 1.0E-6) {
                velocity = velocity.normalize().scale(HOMING_SPEED);
            }
        } else {
            velocity = this.getDeltaMovement().lengthSqr() > 0.001
                    ? this.getDeltaMovement()
                    : escapeDirection.scale(HOMING_SPEED);
        }
        this.setDeltaMovement(velocity);
        this.move(MoverType.SELF, velocity);
        this.hurtMarked = true;
        if (this.horizontalCollision || this.verticalCollision) {
            splashAndDiscard();
            return;
        }
        if (seekAge > MAX_SEEK_LIFETIME) {
            splashAndDiscard();
        }
    }

    private boolean isNearOwner(LivingEntity owner) {
        return owner != null && this.distanceToSqr(owner) < CLEAR_RADIUS * CLEAR_RADIUS;
    }

    private void hit(LivingEntity target) {
        if (this.level() instanceof ServerLevel serverLevel) {
            LivingEntity owner = getOwner();
            float damage = target.getMaxHealth() * DAMAGE_PERCENT_OF_MAX_HEALTH;
            var source = owner != null
                    ? this.damageSources().indirectMagic(this, owner)
                    : this.damageSources().magic();
            target.hurt(source, damage);
            splash(serverLevel, this.position());
        }
        lastHitGameTime.put(target.getUUID(), this.level().getGameTime());
        this.discard();
    }
    private Vec3 orbitVelocity(LivingEntity target) {
        Vec3 toOrb = this.position().subtract(target.getEyePosition());
        if (toOrb.lengthSqr() < 1.0E-4) toOrb = new Vec3(1, 0, 0);
        Vec3 tangent = new Vec3(-toOrb.z, 0, toOrb.x).normalize();
        Vec3 radial = toOrb.normalize();
        double radiusError = toOrb.length() - ORBIT_RADIUS;
        double bob = Math.sin((this.tickCount + getSlot() * 7) * 0.3) * 0.02;
        return tangent.scale(ORBIT_SPEED).add(radial.scale(-radiusError * 0.3)).add(0, bob, 0);
    }


    private void splashAndDiscard() {
        if (this.level() instanceof ServerLevel serverLevel) {
            splash(serverLevel, this.position());
        }
        this.discard();
    }

    private void splash(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y, pos.z, 20, 0.3, 0.3, 0.3, 0.05);
        level.sendParticles(ParticleTypes.BUBBLE_POP, pos.x, pos.y, pos.z, 15, 0.4, 0.4, 0.4, 0.1);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_SPLASH,
                SoundSource.PLAYERS, 0.8f, 1.2f);
    }

    private Vec3 rotateY(Vec3 vec, double angleRad) {
        double cos = Math.cos(angleRad);
        double sin = Math.sin(angleRad);
        return new Vec3(vec.x * cos - vec.z * sin, vec.y, vec.x * sin + vec.z * cos);
    }

    private Vec3 randomJitter(double strength) {
        return new Vec3(
                (this.random.nextDouble() - 0.5) * strength,
                (this.random.nextDouble() - 0.5) * strength * 0.5,
                (this.random.nextDouble() - 0.5) * strength);
    }

    private void spawnOrbParticles() {
        double radius = 0.16;
        int count = 3;
        float phaseOffset = (this.getId() % 100) * 0.15f;
        float t = (this.tickCount + phaseOffset) * 0.15f;
        for (int i = 0; i < count; i++) {
            double theta = t + (2 * Math.PI / count) * i;
            double phi = Math.PI * 0.5 + Math.sin(t * 0.7 + i) * 0.6;
            double px = this.getX() + radius * Math.sin(phi) * Math.cos(theta);
            double py = this.getY() + radius * Math.cos(phi);
            double pz = this.getZ() + radius * Math.sin(phi) * Math.sin(theta);
            this.level().addParticle(ORB_DUST, px, py, pz, 0, 0, 0);
        }
        float coreChance = getSlot() == 7 ? 0.35f : 0.15f;
        if (this.random.nextFloat() < coreChance) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    private void spawnThreadToPrevious(LivingEntity owner) {
        int slot = getSlot();
        if (slot == 0 || owner == null) return;
        AmphitriteOrbEntity prev = findOrbBySlot(owner, slot - 1);
        if (prev == null) return;
        Vec3 from = this.position();
        Vec3 to = prev.position();
        int segments = 6;
        for (int i = 1; i < segments; i++) {
            double t = i / (double) segments;
            Vec3 p = from.lerp(to, t);
            this.level().addParticle(THREAD_DUST, p.x, p.y, p.z, 0, 0, 0);
        }
    }

    private void spawnSeekingTrail() {
        this.level().addParticle(ParticleTypes.BUBBLE, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        if (this.random.nextFloat() < 0.4f) {
            this.level().addParticle(ORB_DUST, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
        }
    }

    private AmphitriteOrbEntity findOrbBySlot(LivingEntity owner, int slot) {
        List<AmphitriteOrbEntity> found = this.level().getEntitiesOfClass(AmphitriteOrbEntity.class,
                this.getBoundingBox().inflate(FORMATION_SEARCH_RADIUS),
                orb -> owner.getUUID().equals(orb.getOwnerUUID()) && orb.getSlot() == slot);
        return found.isEmpty() ? null : found.get(0);
    }

    public UUID getOwnerUUID() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    private LivingEntity getOwner() {
        UUID id = getOwnerUUID();
        if (id == null) return null;
        return this.level().getPlayerByUUID(id);
    }

    private LivingEntity getTarget() {
        UUID id = this.entityData.get(DATA_TARGET_UUID).orElse(null);
        if (id == null || !(this.level() instanceof ServerLevel serverLevel)) return null;
        Entity e = serverLevel.getEntity(id);
        return e instanceof LivingEntity living ? living : null;
    }

    public int getSlot() {
        return this.entityData.get(DATA_SLOT);
    }

    private int getState() {
        return this.entityData.get(DATA_STATE);
    }

    public boolean isFloating() {
        return getState() == STATE_FLOATING;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        this.seekAge = tag.getInt("SeekAge");
        this.entityData.set(DATA_SLOT, tag.getInt("Slot"));
        this.entityData.set(DATA_STATE, tag.getInt("State"));
        if (tag.hasUUID("Owner")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID("Owner")));
        }
        if (tag.hasUUID("Target")) {
            this.entityData.set(DATA_TARGET_UUID, Optional.of(tag.getUUID("Target")));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.age);
        tag.putInt("SeekAge", this.seekAge);
        tag.putInt("Slot", getSlot());
        tag.putInt("State", getState());
        this.entityData.get(DATA_OWNER_UUID).ifPresent(id -> tag.putUUID("Owner", id));
        this.entityData.get(DATA_TARGET_UUID).ifPresent(id -> tag.putUUID("Target", id));
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}