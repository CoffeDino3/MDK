package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.entity.abilities.AngelbornAbilityEntity;
import com.CoffeDino.lunacy.entity.abilities.GatekeeperPortalEntity;
import com.CoffeDino.lunacy.entity.abilities.GatekeeperProjectileEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Lunacy.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<AngelbornAbilityEntity>> ANGELBORN_ABILITY =
            ENTITIES.register("angelborn_ability",
                    () -> EntityType.Builder.<AngelbornAbilityEntity>of(AngelbornAbilityEntity::new, MobCategory.MISC)
                            .sized(1.0f, 0.1f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("angelborn_ability"));
    public static final DeferredHolder<EntityType<?>, EntityType<LamentBulletEntity>> LAMENT_BULLET =
            ENTITIES.register("lament_bullet",
                    () -> EntityType.Builder.<LamentBulletEntity>of(LamentBulletEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("lament_bullet"));
    public static final DeferredHolder<EntityType<?>, EntityType<FireSpearEntity>> FIRE_SPEAR = ENTITIES.register("fire_spear",
            () -> EntityType.Builder.<FireSpearEntity>of(FireSpearEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("fire_spear"));
    public static final DeferredHolder<EntityType<?>, EntityType<GatekeeperPortalEntity>> GATEKEEPER_PORTAL =
            ENTITIES.register("gatekeeper_portal",
                    () -> EntityType.Builder.<GatekeeperPortalEntity>of(GatekeeperPortalEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("gatekeeper_portal"));

    public static final DeferredHolder<EntityType<?>, EntityType<GatekeeperProjectileEntity>> GATEKEEPER_PROJECTILE =
            ENTITIES.register("gatekeeper_projectile",
                    () -> EntityType.Builder.<GatekeeperProjectileEntity>of(GatekeeperProjectileEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("gatekeeper_projectile"));
    public static final DeferredHolder<EntityType<?>, EntityType<ShiArrowEntity>> SHI_ARROW =
            ENTITIES.register("shi_arrow",
                    () -> EntityType.Builder.<ShiArrowEntity>of(ShiArrowEntity::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("shi_arrow"));
    public static final DeferredHolder<EntityType<?>, EntityType<FloatingRapierEntity>> FLOATING_RAPIER =
            ENTITIES.register("floating_rapier",
                    () -> EntityType.Builder.<FloatingRapierEntity>of(FloatingRapierEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .build("floating_rapier"));
    public static final DeferredHolder<EntityType<?>, EntityType<ThrownRapierEntity>> THROWN_RAPIER =
            ENTITIES.register("thrown_rapier", () -> EntityType.Builder
                    .<ThrownRapierEntity>of(ThrownRapierEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build("thrown_rapier"));
    public static final DeferredHolder<EntityType<?>, EntityType<BorontAvatarEntity>> BORONT_AVATAR =
            ENTITIES.register("boront_avatar",
                    () -> EntityType.Builder.<BorontAvatarEntity>of(BorontAvatarEntity::new, MobCategory.MISC)
                            .sized(3.0f, 18.0f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("boront_avatar"));


}
