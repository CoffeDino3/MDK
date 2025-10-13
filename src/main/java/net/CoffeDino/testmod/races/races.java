package net.CoffeDino.testmod.races;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.capability.IRaceSize;
import net.CoffeDino.testmod.capability.RaceSizeProvider;
import net.CoffeDino.testmod.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class races {
    private static Race clientRace = null;
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "race_health_modifier");
    private static final ResourceLocation HEIGHT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "race_height");
    private static final ResourceLocation WIDTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "race_width");

    private static float sculkHealthBonus = 20.0f;
    private static float warderHealthBonus = 10.0f;
    private static float enderHealthBonus = 0.0f;
    private static float phantomHealthBonus = 5.0f;
    public enum Race {
        SCULK("sculk", "Sculk", 1.8f, 0.6f),
        WARDER("warder", "Warder", 2.2f, 0.8f),
        ENDER("ender", "Ender", 1.9f, 0.6f),
        PHANTOM("phantom", "Phantom", 1.6f, 0.5f);

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

        public String getDisplayName(){
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

    public static float getHealthBonus(Race race){
        return switch (race){
            case SCULK -> sculkHealthBonus;
            case WARDER -> warderHealthBonus;
            case ENDER -> enderHealthBonus;
            case PHANTOM -> phantomHealthBonus;
        };
    }




    public static void applyRaceEffects(Player player, Race race) {
        clearRaceEffects(player);
        switch (race) {
            case ENDER -> applyEnderTraits(player);
            case SCULK -> applySculkTraits(player);
            case WARDER -> applyWarderTraits(player);
            case PHANTOM -> applyPhantomTraits(player);
        }
        applyHealthBonus(player, race);
        applySizeModifiers(player, race);
    }



    private static void clearRaceEffects(Player player) {
        if (player instanceof ServerPlayer){
            player.removeEffect(MobEffects.DAMAGE_RESISTANCE);
            player.removeEffect(MobEffects.DAMAGE_BOOST);
            player.removeEffect(MobEffects.NIGHT_VISION);
            player.removeEffect(MobEffects.JUMP);
        }

        clearHealthModifier(player);
        clearSizeModifiers(player);

        System.out.println("DEBUG: Clearing race effects for " + player.getName().getString());
    }

    private static void applyHealthBonus(Player player, Race race) {
        if (player instanceof ServerPlayer serverPlayer) {
            AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttribute != null) {
                // Remove old health modifier if it exists
                healthAttribute.removeModifier(HEALTH_MODIFIER_ID);

                // Get the current health bonus for this race
                float healthBonus = getHealthBonus(race);

                if (healthBonus > 0) {
                    // Create new health modifier with ResourceLocation ID
                    AttributeModifier healthModifier = new AttributeModifier(
                            HEALTH_MODIFIER_ID,
                            healthBonus,
                            AttributeModifier.Operation.ADD_VALUE
                    );
                    healthAttribute.addTransientModifier(healthModifier);

                    // Heal player to new max health
                    player.setHealth(player.getMaxHealth());

                    System.out.println("DEBUG: Applied " + healthBonus + " health bonus to " + player.getName().getString());
                }
            }
        }
    }

    private static void clearHealthModifier(Player player) {
        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(HEALTH_MODIFIER_ID);

            // Ensure health doesn't go below 1
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }
    }
    // In your races.java file, replace the size methods:

    private static void applySizeModifiers(Player player, Race race) {
        if (player instanceof ServerPlayer serverPlayer) {
            player.getCapability(RaceSizeProvider.RACE_SIZE).ifPresent(raceSize -> {
                raceSize.setRaceSize(race.getHeight(), race.getWidth());
            });

            System.out.println("DEBUG: Applied size modifiers for " + race.getDisplayName() +
                    " - Height: " + race.getHeight() + ", Width: " + race.getWidth());

            // Force refresh on server
            player.refreshDimensions();

            // Sync to client
            NetworkHandler.syncSizeToClient(serverPlayer, race.getHeight(), race.getWidth());

            // Schedule another refresh for next tick to ensure it applies
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



    private static void applyEnderTraits(Player player) {
        if(player instanceof ServerPlayer){
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
        if(player instanceof ServerPlayer){
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
        if(player instanceof ServerPlayer){
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
        if(player instanceof ServerPlayer){
            player.addEffect(new MobEffectInstance(
                    MobEffects.JUMP,
                    -1,
                    0,
                    true,
                    false
            ));
        }
    }

}