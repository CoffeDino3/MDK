package net.CoffeDino.testmod.entity;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.entity.abilities.AngelbornAbilityEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, TestingCoffeDinoMod.MOD_ID);

    public static final RegistryObject<EntityType<AngelbornAbilityEntity>> ANGELBORN_ABILITY =
            ENTITIES.register("angelborn_ability",
                    () -> EntityType.Builder.<AngelbornAbilityEntity>of(AngelbornAbilityEntity::new, MobCategory.MISC)
                            .sized(1.0f, 0.1f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("angelborn_ability"));
    public static final RegistryObject<EntityType<LamentBulletEntity>> LAMENT_BULLET =
            ENTITIES.register("lament_bullet",
                    () -> EntityType.Builder.<LamentBulletEntity>of(LamentBulletEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(1)
                            .build("lament_bullet"));
    public static final RegistryObject<EntityType<FireSpearEntity>> FIRE_SPEAR = ENTITIES.register("fire_spear",
            () -> EntityType.Builder.<FireSpearEntity>of(FireSpearEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build("fire_spear"));
}