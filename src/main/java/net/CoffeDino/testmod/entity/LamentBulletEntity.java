package net.CoffeDino.testmod.entity;

import net.CoffeDino.testmod.effects.ModEffects;
import net.CoffeDino.testmod.item.ModItems;
import net.CoffeDino.testmod.particle.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;

public class LamentBulletEntity extends ThrowableItemProjectile {
    private final boolean isAccurate;
    private Vec3 initialPosition;

    public LamentBulletEntity(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.isAccurate = false;
        this.initialPosition = this.position();
    }

    public LamentBulletEntity(Level level, LivingEntity shooter, boolean isAccurate) {
        super(ModEntities.LAMENT_BULLET.get(), shooter, level);
        this.isAccurate = isAccurate;
        this.initialPosition = this.position();
        this.setNoGravity(true);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.initialPosition != null && this.position().distanceTo(this.initialPosition) > 200.0) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide) {
            spawnTrailParticles();
        }
    }

    private void spawnTrailParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            Vec3 pos = this.position();
            serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    2,
                    0.05, 0.05, 0.05,
                    0.01
            );
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            Vec3 hitPos = result.getLocation();
            serverLevel.sendParticles(ParticleTypes.WHITE_ASH,
                    hitPos.x, hitPos.y, hitPos.z,
                    10,
                    0.2, 0.2, 0.2,
                    0.1
            );
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                    hitPos.x, hitPos.y, hitPos.z,
                    5,
                    0.1, 0.1, 0.1,
                    0.05
            );
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        if (!this.level().isClientSide && result.getEntity() instanceof LivingEntity target) {
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);
            MobEffectInstance existing = target.getEffect(ModEffects.MOURNING_FUNERAL.getHolder().get());
            int amplifier = existing != null ? existing.getAmplifier() + 1 : 0;
            if (amplifier > 10) amplifier = 10;

            target.addEffect(new MobEffectInstance(ModEffects.MOURNING_FUNERAL.getHolder().get(), 200, amplifier, false, true, true));

            if (this.level() instanceof ServerLevel serverLevel) {
                Vec3 pos = result.getLocation();
                serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y, pos.z, 10, 0.2, 0.2, 0.2, 0.1);
                serverLevel.sendParticles(ModParticles.MOURNING_BUTTERFLY_PARTICLES.get(), pos.x, pos.y, pos.z, 10, 0.2, 0.2, 0.2, 0.05);
            }

            this.discard();
        }
    }

    @Override
    public boolean isNoGravity() {
        return true;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.LAMENT_BULLET.get();
    }
}
