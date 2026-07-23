package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.entity.ModEntities;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BloodMistEntity extends Entity {
    private static final int LIFETIME_TICKS = 600;
    private static final double HALF_WIDTH = 7.0;
    private static final double HALF_HEIGHT = 2.5;
    private static final double PARTICLE_OVERFLOW_XZ = 2.0;
    private static final double PARTICLE_OVERFLOW_Y = 1.0;
    private static final int OWNER_DAMAGE_INTERVAL = 20;
    private static final float OWNER_DAMAGE = 2.5f;
    private static final int MOB_DAMAGE_INTERVAL = 20;
    private static final float MOB_DAMAGE_PERCENT_OF_MAX_HEALTH = 0.02f;
    public static final float HEAL_PERCENT_OF_MOB_TICK_DAMAGE = 0.25f;
    public static final float HEAL_PERCENT_OF_MELEE_DAMAGE = 0.3f;
    private static final int GLOW_REFRESH_TICKS = 40;
    private static final String GLOW_TEAM_NAME = "lunacy_blood_glow";
    private static final DustParticleOptions RED_MIST = new DustParticleOptions(new Vector3f(0.55f, 0.03f, 0.05f), 2.4f);
    private UUID ownerUUID;
    private int age;
    private final Map<UUID, String> glowingMobs = new HashMap<>();

    public BloodMistEntity(EntityType<? extends BloodMistEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public BloodMistEntity(Level level, LivingEntity owner) {
        this(ModEntities.BLOOD_MIST.get(), level);
        this.setPos(owner.getX(), owner.getY(), owner.getZ());
        this.ownerUUID = owner.getUUID();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        LivingEntity owner = getOwner();
        if (owner != null) {
            this.setPos(owner.getX(), owner.getY(), owner.getZ());
        }
        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }
        age++;
        if (age >= LIFETIME_TICKS || owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        if (age % OWNER_DAMAGE_INTERVAL == 0) {
            owner.hurt(this.damageSources().magic(), OWNER_DAMAGE);
        }
        AABB damageBox = getDamageBox();
        List<LivingEntity> mobs = this.level().getEntitiesOfClass(LivingEntity.class, damageBox,
                e -> e.isAlive() && !e.getUUID().equals(ownerUUID));

        if (this.level() instanceof ServerLevel serverLevel) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam glowTeam = getOrCreateGlowTeam(scoreboard);
            Set<UUID> currentIds = new HashSet<>();
            for (LivingEntity mob : mobs) {
                currentIds.add(mob.getUUID());
                mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_REFRESH_TICKS, 0, false, false));
                if (!glowingMobs.containsKey(mob.getUUID())) {
                    String scoreboardName = mob.getScoreboardName();
                    scoreboard.addPlayerToTeam(scoreboardName, glowTeam);
                    glowingMobs.put(mob.getUUID(), scoreboardName);
                }
                if (age % MOB_DAMAGE_INTERVAL == 0) {
                    float damage = mob.getMaxHealth() * MOB_DAMAGE_PERCENT_OF_MAX_HEALTH;
                    mob.hurt(this.damageSources().magic(), damage);
                    owner.heal(damage * HEAL_PERCENT_OF_MOB_TICK_DAMAGE);
                }
            }
            glowingMobs.keySet().removeIf(id -> {
                if (!currentIds.contains(id)) {
                    scoreboard.removePlayerFromTeam(glowingMobs.get(id), glowTeam);
                    return true;
                }
                return false;
            });
        }
    }

    private PlayerTeam getOrCreateGlowTeam(Scoreboard scoreboard) {
        PlayerTeam team = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
        if (team == null) {
            team = scoreboard.addPlayerTeam(GLOW_TEAM_NAME);
            team.setColor(ChatFormatting.RED);
        }
        return team;
    }
    private AABB getDamageBox() {
        return new AABB(this.getX() - HALF_WIDTH, this.getY() - HALF_HEIGHT, this.getZ() - HALF_WIDTH,
                this.getX() + HALF_WIDTH, this.getY() + HALF_HEIGHT, this.getZ() + HALF_WIDTH);
    }
    public boolean isPositionInMist(Vec3 pos) {
        return getDamageBox().contains(pos);
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    private LivingEntity getOwner() {
        if (ownerUUID == null || !(this.level() instanceof ServerLevel serverLevel)) return null;
        Entity entity = serverLevel.getEntity(ownerUUID);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void spawnClientParticles() {
        int mistParticles = 35;
        for (int i = 0; i < mistParticles; i++) {
            double x = this.getX() + (this.random.nextDouble() * 2 - 1) * (HALF_WIDTH + PARTICLE_OVERFLOW_XZ);
            double y = this.getY() + (this.random.nextDouble() * 2 - 1) * (HALF_HEIGHT + PARTICLE_OVERFLOW_Y);
            double z = this.getZ() + (this.random.nextDouble() * 2 - 1) * (HALF_WIDTH + PARTICLE_OVERFLOW_XZ);

            this.level().addParticle(RED_MIST, x, y, z, 0, 0.01, 0);
        }
        int slashes = 2 + this.random.nextInt(2);
        for (int i = 0; i < slashes; i++) {
            double x = this.getX() + (this.random.nextDouble() * 2 - 1) * HALF_WIDTH;
            double y = this.getY() + (this.random.nextDouble() * 2 - 1) * HALF_HEIGHT + 1.0;
            double z = this.getZ() + (this.random.nextDouble() * 2 - 1) * HALF_WIDTH;
            this.level().addParticle(ParticleTypes.SWEEP_ATTACK, x, y, z, 0, 0, 0);
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (this.level() instanceof ServerLevel serverLevel && !glowingMobs.isEmpty()) {
            Scoreboard scoreboard = serverLevel.getScoreboard();
            PlayerTeam glowTeam = scoreboard.getPlayerTeam(GLOW_TEAM_NAME);
            if (glowTeam != null) {
                for (String name : glowingMobs.values()) {
                    scoreboard.removePlayerFromTeam(name, glowTeam);
                }
            }
        }
        glowingMobs.clear();
        super.remove(reason);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.age = tag.getInt("Age");
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.age);
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
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