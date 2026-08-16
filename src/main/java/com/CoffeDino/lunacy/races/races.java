package com.CoffeDino.lunacy.races;

import com.CoffeDino.lunacy.Config;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.BelieverAbilityHandler;
import com.CoffeDino.lunacy.abilities.GatekeeperAbilityHandler;
import com.CoffeDino.lunacy.capability.ModAttachments;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import com.CoffeDino.lunacy.network.NetworkHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class races {
    private static Race clientRace = null;
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_health_modifier");
    private static final ResourceLocation HEALTH_LEVEL_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_health_level_modifier");
    private static final ResourceLocation ARMOR_LEVEL_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_armor_level_modifier");
    private static final ResourceLocation TOUGHNESS_LEVEL_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_toughness_level_modifier");
    private static final ResourceLocation ATTACK_DAMAGE_LEVEL_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_attack_damage_level_modifier");
    private static final ResourceLocation HEIGHT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_height");
    private static final ResourceLocation WIDTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "race_width");

    public enum Race {
        SCULK("sculk", "Sculk", 1.8f, 0.6f),
        WARDER("warder", "Warder", 2.2f, 0.8f),
        ENDER("ender", "Ender", 1.9f, 0.6f),
        PHANTOM("phantom", "Phantom", 1.6f, 0.5f),
        LOVER("lover", "Lover", 1.8f, 0.5f),
        BELIEVER("believer", "Believer", 1.7f, 0.6f),
        ANGELBORN("angelborn", "Angelborn", 2.0f, 0.6f),
        VAMPIREBORN("vampireborn", "Vampireborn", 2.0f, 0.6f),
        ETHEREAL("ethereal", "Ethereal", 1.5f, 0.5f),
        CELESTIAL("celestial", "Celestial", 2.1f, 0.6f),
        GATEKEEPER("gatekeeper", "Gatekeeper", 2.5f, 0.9f);

        private final String id;
        private final String displayName;
        private final float height;
        private final float width;

        Race(String id, String displayName, float height, float width) {
            this.id = id;
            this.displayName = displayName;
            this.height = height;
            this.width = width;
        }

        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public float getHeight() { return height; }
        public float getWidth() { return width; }
    }

    public static void resetClientRace() {
        clientRace = null;
        Lunacy.LOGGER.debug("DEBUG: Reset client race for new world");
    }

    public static void setPlayerRace(Player player, Race race) {
        if (player == null) return;

        Race currentRace = getPlayerRace(player);
        if (currentRace == Race.BELIEVER && race != Race.BELIEVER) {
            BelieverAbilityHandler.onRaceChange(player);
        }
        if (currentRace == Race.GATEKEEPER && race != Race.GATEKEEPER) {
            GatekeeperAbilityHandler.deactivateAbility(player);
        }
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (currentRace != null && currentRace != race) {
                PlayerLevels.resetLevel(serverPlayer);
            }

            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            dataManager.setPlayerRace(player.getUUID(), race != null ? race.getId() : "");
            syncRaceToClient(serverPlayer, race);
            Lunacy.LOGGER.debug("DEBUG: Race set on server for " + player.getName().getString() + ": " + (race != null ? race.getDisplayName() : "null"));

            if (race != null) {
                applyRaceEffects(player, race);
                player.sendSystemMessage(Component.literal("Your race is: " + race.getDisplayName()));
            } else {
                clearRaceEffects(player);
            }
        } else {
            clientRace = race;
            Lunacy.LOGGER.debug("DEBUG: Race set on client: " + (race != null ? race.getDisplayName() : "null"));
        }
    }

    public static void setClientRace(Race race) {
        clientRace = race;
        Lunacy.LOGGER.debug("DEBUG: Set client race: " + (race != null ? race.getDisplayName() : "null"));
    }

    public static Race getPlayerRace(Player player) {
        if (player == null) return null;

        if (player.level().isClientSide()) {
            return clientRace;
        } else if (player instanceof ServerPlayer serverPlayer) {
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            String raceId = dataManager.getPlayerRace(player.getUUID());

            if (raceId == null || raceId.isEmpty()) return null;

            for (Race race : Race.values()) {
                if (race.getId().equals(raceId)) return race;
            }
        }

        return null;
    }

    public static boolean hasChosenRace(Player player) {
        if (player == null) return false;

        if (player.level().isClientSide()) {
            return clientRace != null;
        } else if (player instanceof ServerPlayer serverPlayer) {
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            return dataManager.hasRace(player.getUUID());
        }

        return false;
    }

    private static void syncRaceToClient(ServerPlayer player, Race race) {
        NetworkHandler.syncRaceToClient(player, race);
    }

    public static void clearPlayerRace(Player player) {
        if (getPlayerRace(player) == Race.BELIEVER) {
            BelieverAbilityHandler.onRaceChange(player);
        }

        setPlayerRace(player, null);
        Lunacy.LOGGER.debug("DEBUG: Cleared race for player: " + player.getName().getString());
    }

    public static void onPlayerJoinWorld(Player player) {
        if (!player.level().isClientSide()) {
            Race race = getPlayerRace(player);
            if (race != null) {
                Lunacy.LOGGER.debug("DEBUG: Applying race effects to " + player.getName().getString() + ": " + race.getDisplayName());
                applyRaceEffects(player, race);
            }
        }
    }

    public static void onPlayerLeaveWorld(Player player) {
        if (!player.level().isClientSide()) {
            clearRaceEffects(player);
        }
    }
    public static void onLevelChanged(ServerPlayer player) {
        Race race = getPlayerRace(player);
        if (race != null) {
            applyRaceEffects(player, race);
        }
    }

    public static float getHealthBonus(Race race) {
        return Config.healthBonus(race);
    }

    public static void applyRaceEffects(Player player, Race race) {
        clearRaceEffects(player);

        int level = (player instanceof ServerPlayer serverPlayer) ? PlayerLevels.getLevel(serverPlayer) : 1;
        int amplifierBonus = RaceLevelScaling.genericAmplifierBonus(level);

        switch (race) {
            case ENDER -> applyEnderTraits(player, amplifierBonus);
            case SCULK -> applySculkTraits(player, amplifierBonus);
            case WARDER -> applyWarderTraits(player, amplifierBonus);
            case PHANTOM -> applyPhantomTraits(player, amplifierBonus);
            case LOVER -> applyLoverTraits(player, amplifierBonus);
            case BELIEVER -> applyBelieverTraits(player, amplifierBonus);
            case CELESTIAL -> applyCelestialTraits(player, amplifierBonus);
            case ETHEREAL -> applyEtherealTraits(player, amplifierBonus);
            case ANGELBORN -> applyAngelbornTraits(player, amplifierBonus);
            case VAMPIREBORN -> applyVampirebornTraits(player, amplifierBonus);
            case GATEKEEPER -> applyGatekeeperTraits(player, amplifierBonus);
        }

        applyLevel25PermaBuff(player, race, level);
        applyHealthAndArmorBonus(player, race, level);
        applySizeModifiers(player, race);
    }

    private static void clearRaceEffects(Player player) {
        if (player instanceof ServerPlayer) {
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.removeEffect(MobEffects.DAMAGE_BOOST);
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.removeEffect(MobEffects.JUMP);
            player.removeEffect(MobEffects.LUCK);
            player.removeEffect(MobEffects.HERO_OF_THE_VILLAGE);
            player.removeEffect(MobEffects.INVISIBILITY);
            player.removeEffect(MobEffects.REGENERATION);
            player.removeEffect(ModEffects.BLOOD_SURGE);
            player.removeEffect(ModEffects.ETHER);
            player.removeEffect(MobEffects.SATURATION);
            player.removeEffect(MobEffects.DIG_SPEED);
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
            player.removeEffect(MobEffects.WATER_BREATHING);
            player.removeEffect(MobEffects.ABSORPTION);
        }
        clearSizeModifiers(player);

        Lunacy.LOGGER.debug("DEBUG: Clearing race effects for " + player.getName().getString());
    }
    private static void applyHealthAndArmorBonus(Player player, Race race, int level) {
        if (!(player instanceof ServerPlayer)) return;

        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            float previousTotalBonus = 0f;
            AttributeModifier existingBase = healthAttribute.getModifier(HEALTH_MODIFIER_ID);
            AttributeModifier existingLevel = healthAttribute.getModifier(HEALTH_LEVEL_MODIFIER_ID);
            if (existingBase != null) previousTotalBonus += (float) existingBase.amount();
            if (existingLevel != null) previousTotalBonus += (float) existingLevel.amount();

            healthAttribute.removeModifier(HEALTH_MODIFIER_ID);
            healthAttribute.removeModifier(HEALTH_LEVEL_MODIFIER_ID);

            float baseBonus = getHealthBonus(race);
            float levelBonus = RaceLevelScaling.levelHealthBonus(race, level);

            healthAttribute.addPermanentModifier(new AttributeModifier(
                    HEALTH_MODIFIER_ID, baseBonus, AttributeModifier.Operation.ADD_VALUE));
            if (levelBonus != 0f) {
                healthAttribute.addPermanentModifier(new AttributeModifier(
                        HEALTH_LEVEL_MODIFIER_ID, levelBonus, AttributeModifier.Operation.ADD_VALUE));
            }

            float newTotalBonus = baseBonus + levelBonus;
            float delta = newTotalBonus - previousTotalBonus;
            if (delta != 0f) {
                float newHealth = Mth.clamp(player.getHealth() + delta, 0f, player.getMaxHealth());
                player.setHealth(newHealth);
            } else if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }

            Lunacy.LOGGER.debug("DEBUG: Applied {} base + {} level health bonus to {}. New max health: {}",
                    baseBonus, levelBonus, player.getName().getString(), player.getMaxHealth());
        }

        AttributeInstance armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null) {
            armorAttribute.removeModifier(ARMOR_LEVEL_MODIFIER_ID);
            float armorBonus = RaceLevelScaling.levelArmorBonus(race, level);
            if (armorBonus != 0f) {
                armorAttribute.addPermanentModifier(new AttributeModifier(
                        ARMOR_LEVEL_MODIFIER_ID, armorBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }

        AttributeInstance toughnessAttribute = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (toughnessAttribute != null) {
            toughnessAttribute.removeModifier(TOUGHNESS_LEVEL_MODIFIER_ID);
            float toughnessBonus = RaceLevelScaling.levelToughnessBonus(race, level);
            if (toughnessBonus != 0f) {
                toughnessAttribute.addPermanentModifier(new AttributeModifier(
                        TOUGHNESS_LEVEL_MODIFIER_ID, toughnessBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
        AttributeInstance attackDamageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackDamageAttribute != null) {
            attackDamageAttribute.removeModifier(ATTACK_DAMAGE_LEVEL_MODIFIER_ID);
            float attackBonus = RaceLevelScaling.levelAttackDamageBonus(race, level);
            if (attackBonus != 0f) {
                attackDamageAttribute.addPermanentModifier(new AttributeModifier(
                        ATTACK_DAMAGE_LEVEL_MODIFIER_ID, attackBonus, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }
    private static void applyLevel25PermaBuff(Player player, Race race, int level) {
        if (!(player instanceof ServerPlayer) || !RaceLevelScaling.hasReachedLevel25(level)) return;

        switch (race) {
            case SCULK -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 1, true, false));
            case WARDER -> player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, -1, 0, true, false));
            case ENDER -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, true, false));
            case PHANTOM -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 1, true, false));
            case LOVER -> player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, -1, 0, true, false));
            case BELIEVER -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 1, true, false));
            case ANGELBORN -> player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, -1, 1, true, false));
            case VAMPIREBORN -> player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, 0, true, false));
            case ETHEREAL -> player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, -1, 0, true, false));
            case CELESTIAL -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, 0, true, false));
            case GATEKEEPER -> player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, 0, true, false));
        }
    }

    private static void applySizeModifiers(Player player, Race race) {
        if (player instanceof ServerPlayer serverPlayer) {
            float height = Config.height(race);
            float width = Config.width(race);

            player.getData(ModAttachments.RACE_SIZE).setRaceSize(height, width);

            Lunacy.LOGGER.debug("DEBUG: Applied size modifiers for " + race.getDisplayName() +
                    " - Height: " + height + ", Width: " + width);
            player.refreshDimensions();
            NetworkHandler.syncSizeToClient(serverPlayer, height, width);
            serverPlayer.server.execute(() -> {
                player.refreshDimensions();
            });
        }
    }

    private static void clearSizeModifiers(Player player) {
        player.getData(ModAttachments.RACE_SIZE).resetRaceSize();
        player.refreshDimensions();
        Lunacy.LOGGER.debug("DEBUG: Cleared size modifiers for " + player.getName().getString());
    }

    private static void applyGatekeeperTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, -1, amplifierBonus, true, false));
        }
    }

    private static void applyAngelbornTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, -1, amplifierBonus, true, false));
        }
    }

    private static void applyEtherealTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(ModEffects.ETHER, -1, amplifierBonus, true, false, true));
        }
    }

    private static void applyVampirebornTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(ModEffects.BLOOD_SURGE, -1, amplifierBonus, true, false, true));
        }
    }

    private static void applyCelestialTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, -1, 0, true, false));
        }
    }

    private static void applyLoverTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.HERO_OF_THE_VILLAGE, -1, amplifierBonus, true, false));
        }
    }

    private static void applyBelieverTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.LUCK, -1, amplifierBonus, true, false));
        }
    }

    private static void applyEnderTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, true, false));
        }
    }

    private static void applySculkTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, -1, amplifierBonus, true, false));
        }
    }

    private static void applyWarderTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, -1, amplifierBonus, true, false));
        }
    }

    private static void applyPhantomTraits(Player player, int amplifierBonus) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, -1, amplifierBonus, true, false));
        }
    }

    private static final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private static final long DAMAGE_COOLDOWN = 40;

    public static void handleVampireSunlight(Player player) {
        if (player == null || player.level().isClientSide()) return;

        Race race = getPlayerRace(player);
        if (race != Race.VAMPIREBORN) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (isExposedToSunlight(player)) {
            if (!isWearingHelmet(player)) {
                UUID playerId = player.getUUID();
                long currentTime = player.level().getGameTime();
                Long lastTime = lastDamageTime.get(playerId);

                if (lastTime == null || currentTime - lastTime >= DAMAGE_COOLDOWN) {
                    player.hurt(player.damageSources().onFire(), 2.0F);
                    player.setRemainingFireTicks(4 * 20);
                    lastDamageTime.put(playerId, currentTime);
                    if (player.level().getGameTime() % 40 == 0) {
                        player.displayClientMessage(
                                Component.literal("The sunlight burns your vampire flesh! Shadows should help..."),
                                true
                        );
                    }
                }
            }
        }
    }

    private static boolean isExposedToSunlight(Player player) {
        if (!player.level().isDay()) return false;
        if (!player.level().canSeeSky(player.blockPosition())) return false;
        if (player.level().isRaining()) return false;
        return player.level().getMaxLocalRawBrightness(player.blockPosition()) >= 12;
    }

    private static boolean isWearingHelmet(Player player) {
        var helmet = player.getInventory().getArmor(3);
        if (helmet.isEmpty()) return false;
        return helmet.getItem() instanceof net.minecraft.world.item.ArmorItem armorItem &&
                armorItem.getType().getSlot() == net.minecraft.world.entity.EquipmentSlot.HEAD;
    }
}