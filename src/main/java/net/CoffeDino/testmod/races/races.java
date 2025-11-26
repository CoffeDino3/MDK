package net.CoffeDino.testmod.races;

import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.abilities.BelieverAbilityHandler;
import net.CoffeDino.testmod.capability.IRaceSize;
import net.CoffeDino.testmod.capability.RaceSizeProvider;
import net.CoffeDino.testmod.effects.ModEffects;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
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
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "race_health_modifier");
    private static final ResourceLocation HEIGHT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "race_height");
    private static final ResourceLocation WIDTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(Lunacy.MOD_ID, "race_width");

    private static float sculkHealthBonus = 20.0f;
    private static float warderHealthBonus = 10.0f;
    private static float enderHealthBonus = 0.0f;
    private static float phantomHealthBonus = 5.0f;
    private static float loverHealthBonus = -4.0f;
    private static float believerHealthBonus = 5.0f;
    private static float angelbornHealthBonus = 0.0f;
    private static float vampirebornHealthBonus = -5.0f;
    private static float etherealHealthBonus = 10.0f;
    private static float celestialHealthBonus = 5.0f;

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
        CELESTIAL("celestial", "Celestial", 2.1f, 0.6f);

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

        public String getId() {
            return id;
        }

        public String getDisplayName() {
            return displayName;
        }

        public float getHeight() {
            return height;
        }

        public float getWidth() {
            return width;
        }

    }

    public static void resetClientRace() {
        clientRace = null;
        System.out.println("DEBUG: Reset client race for new world");
    }


    public static void setPlayerRace(Player player, Race race) {
        if (player == null) return;

        Race currentRace = getPlayerRace(player);
        if (currentRace == Race.BELIEVER && race != Race.BELIEVER) {
            BelieverAbilityHandler.onRaceChange(player);
        }
        if (!player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            dataManager.setPlayerRace(player.getUUID(), race != null ? race.getId() : "");
            syncRaceToClient(serverPlayer, race);
            System.out.println("DEBUG: Race set on server for " + player.getName().getString() + ": " + (race != null ? race.getDisplayName() : "null"));

            if (race != null) {
                applyRaceEffects(player, race);
                player.sendSystemMessage(Component.literal("Your race is: " + race.getDisplayName()));
            } else {
                clearRaceEffects(player);
            }
        } else {
            clientRace = race;
            System.out.println("DEBUG: Race set on client: " + (race != null ? race.getDisplayName() : "null"));
        }
    }

    public static void setClientRace(Race race) {
        clientRace = race;
        System.out.println("DEBUG: Set client race: " + (race != null ? race.getDisplayName() : "null"));
    }

    public static Race getPlayerRace(Player player) {
        if (player == null) return null;

        if (player.level().isClientSide()) {
            return clientRace;
        } else if (player instanceof ServerPlayer serverPlayer) {
            RaceDataManager dataManager = RaceDataManager.get(serverPlayer);
            String raceId = dataManager.getPlayerRace(player.getUUID());

            if (raceId == null || raceId.isEmpty()) {
                return null;
            }

            for (Race race : Race.values()) {
                if (race.getId().equals(raceId)) {
                    return race;
                }
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
        net.CoffeDino.testmod.network.NetworkHandler.syncRaceToClient(player, race);
    }

    public static void clearPlayerRace(Player player) {
        if (getPlayerRace(player) == Race.BELIEVER) {
            BelieverAbilityHandler.onRaceChange(player);
        }

        setPlayerRace(player, null);
        System.out.println("DEBUG: Cleared race for player: " + player.getName().getString());
    }

    public static void onPlayerJoinWorld(Player player) {
        if (!player.level().isClientSide()) {
            Race race = getPlayerRace(player);
            if (race != null) {
                System.out.println("DEBUG: Applying race effects to " + player.getName().getString() + ": " + race.getDisplayName());
                applyRaceEffects(player, race);
            }
        }
    }


    public static void onPlayerLeaveWorld(Player player) {
        if (!player.level().isClientSide()) {
            clearRaceEffects(player);
        }
    }

    public static float getHealthBonus(Race race) {
        return switch (race) {
            case SCULK -> sculkHealthBonus;
            case WARDER -> warderHealthBonus;
            case ENDER -> enderHealthBonus;
            case PHANTOM -> phantomHealthBonus;
            case LOVER -> loverHealthBonus;
            case BELIEVER -> believerHealthBonus;
            case ANGELBORN -> angelbornHealthBonus;
            case ETHEREAL -> etherealHealthBonus;
            case CELESTIAL -> celestialHealthBonus;
            case VAMPIREBORN -> vampirebornHealthBonus;
        };
    }


    public static void applyRaceEffects(Player player, Race race) {
        clearRaceEffects(player);
        switch (race) {
            case ENDER -> applyEnderTraits(player);
            case SCULK -> applySculkTraits(player);
            case WARDER -> applyWarderTraits(player);
            case PHANTOM -> applyPhantomTraits(player);
            case LOVER -> applyLoverTraits(player);
            case BELIEVER -> applyBelieverTraits(player);
            case CELESTIAL -> applyCelestialTraits(player);
            case ETHEREAL -> applyEtherealTraits(player);
            case ANGELBORN -> applyAngelbornTraits(player);
            case VAMPIREBORN -> applyVampirebornTraits(player);
        }
        applyHealthBonus(player, race);
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
            player.removeEffect(MobEffects.INVISIBILITY);
            player.removeEffect(MobEffects.REGENERATION);
            player.removeEffect(ModEffects.BLOOD_SURGE.getHolder().get());
            player.removeEffect(ModEffects.ETHER.getHolder().get());

        }

        clearHealthModifier(player);
        clearSizeModifiers(player);

        System.out.println("DEBUG: Clearing race effects for " + player.getName().getString());
    }

    private static void applyHealthBonus(Player player, Race race) {
        if (player instanceof ServerPlayer serverPlayer) {
            AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                healthAttribute.removeModifier(HEALTH_MODIFIER_ID);
                float healthBonus = getHealthBonus(race);
                AttributeModifier healthModifier = new AttributeModifier(
                        HEALTH_MODIFIER_ID,
                        healthBonus,
                        AttributeModifier.Operation.ADD_VALUE
                );
                healthAttribute.addTransientModifier(healthModifier);
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
                System.out.println("DEBUG: Applied " + healthBonus + " health bonus to " + player.getName().getString() + ". New max health: " + player.getMaxHealth());
            }
        }
    }

    private static void clearHealthModifier(Player player) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(HEALTH_MODIFIER_ID);
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }

    private static void applySizeModifiers(Player player, Race race) {
        if (player instanceof ServerPlayer serverPlayer) {
            player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(raceSize -> {
                raceSize.setRaceSize(race.getHeight(), race.getWidth());
            });

            System.out.println("DEBUG: Applied size modifiers for " + race.getDisplayName() +
                    " - Height: " + race.getHeight() + ", Width: " + race.getWidth());
            player.refreshDimensions();
            NetworkHandler.syncSizeToClient(serverPlayer, race.getHeight(), race.getWidth());
            serverPlayer.server.execute(() -> {
                player.refreshDimensions();
            });
        }
    }

    private static void clearSizeModifiers(Player player) {
        player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(IRaceSize::resetRaceSize);
        player.refreshDimensions();
        System.out.println("DEBUG: Cleared size modifiers for " + player.getName().getString());
    }

    private static void applyAngelbornTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.REGENERATION,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

    private static void applyEtherealTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    ModEffects.ETHER.getHolder().get(),
                    -1,
                    0,
                    true,
                    false,
                    true
            ));
        }
    }

    private static void applyVampirebornTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    ModEffects.BLOOD_SURGE.getHolder().get(),
                    -1,
                    0,
                    true,
                    false,
                    true
            ));
        }
    }


    private static void applyCelestialTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

    private static void applyLoverTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.HERO_OF_THE_VILLAGE,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

    private static void applyBelieverTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.LUCK,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }


    private static void applyEnderTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.NIGHT_VISION,
                    -1,
                    0,
                    true,
                    false
            ));
        }

    }

    private static void applySculkTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_RESISTANCE,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

    private static void applyWarderTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.DAMAGE_BOOST,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

    private static void applyPhantomTraits(Player player) {
        if (player instanceof ServerPlayer) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

    private static final Map<UUID, Long> lastDamageTime = new HashMap<>();
    private static final long DAMAGE_COOLDOWN = 40;

    public static void handleVampireSunlight(Player player) {
        if (player == null || player.level().isClientSide()) return;

        Race race = getPlayerRace(player);
        if (race != Race.VAMPIREBORN) return;
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

