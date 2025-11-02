package net.CoffeDino.testmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.CoffeDino.testmod.abilities.EtherealAbilityHandler;
import net.CoffeDino.testmod.abilities.LoverAbilityHandler;
import net.CoffeDino.testmod.network.*;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TestingCoffeDinoMod.MOD_ID, value = Dist.CLIENT)
public class KeyBindHandler {
    public static final KeyMapping SCULK_STORAGE_KEY = new KeyMapping(
            "key.testingcoffedinomod.sculk_storage",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping WARDER_ABILITY_KEY = new KeyMapping(
            "key.testingcoffedinomod.warder_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping LOVER_ABILITY_KEY = new KeyMapping(
            "key.testingcoffedinomod.lover_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping ENDER_TELEPORT_KEY = new KeyMapping(
            "key.testingcoffedinomod.ender_teleport",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R, // Same key as sculk, but will check race
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping PHANTOM_ABILITY_KEY = new KeyMapping(
            "key.testingcoffedinomod.phantom_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping BELIEVER_ABILITY_KEY = new KeyMapping(
            "key.testingcoffedinomod.believer_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping VAMPIREBORN_ABILITY_KEY = new KeyMapping(
            "key.testingcoffedinomod.vampireborn_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );
    public static final KeyMapping ETHEREAL_ABILITY_KEY = new KeyMapping(
            "key.testingcoffedinomod.ethereal_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.testingcoffedinomod.abilities"
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(SCULK_STORAGE_KEY);
        event.register(WARDER_ABILITY_KEY);
        event.register(LOVER_ABILITY_KEY);
        event.register(ENDER_TELEPORT_KEY);
        event.register(PHANTOM_ABILITY_KEY);
        event.register(BELIEVER_ABILITY_KEY);
        event.register(VAMPIREBORN_ABILITY_KEY);
        event.register(ETHEREAL_ABILITY_KEY);
    }
    private static boolean wasLoverKeyPressed = false;
    private static boolean wasVampirebornKeyPressed = false;
    private static long vampirebornPressTime = 0;
    private static final long TAP_THRESHOLD = 200; // milliseconds

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft minecraft = Minecraft.getInstance();

            if (ETHEREAL_ABILITY_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.ETHEREAL) {
                        boolean jumping = minecraft.options.keyJump.isDown();
                        boolean shifting = minecraft.options.keyShift.isDown();
                        NetworkHandler.sendToServer(new ActivateEtherealAbilityPacket(jumping, shifting));
                    }
                }
            }

            if (ENDER_TELEPORT_KEY.consumeClick()) {
                races.Race race = races.getPlayerRace(minecraft.player);
                if(race==races.Race.ENDER){
                    NetworkHandler.triggerEnderTeleport();
                }
            }
            if (PHANTOM_ABILITY_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.PHANTOM) {
                        NetworkHandler.sendToServer(new ActivatePhantomAbilityPacket());
                    }
                }
            }
            if (BELIEVER_ABILITY_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.BELIEVER) {
                        NetworkHandler.sendToServer(new BelieverAbilityPacket());
                    }
                }
            }

            if (SCULK_STORAGE_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.SCULK) {
                        NetworkHandler.openSculkStorage();
                    }
                }
            }
            if (WARDER_ABILITY_KEY.isDown()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.WARDER) {
                        NetworkHandler.sendToServer(new ActivateWarderAbilityPacket());
                    }
                }
            } else {
                if (minecraft.player != null) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.WARDER) {
                        NetworkHandler.sendToServer(new DeactivateWarderAbilityPacket());
                    }
                }
            }
            if (minecraft.player != null && EtherealAbilityHandler.isAbilityActive(minecraft.player)) {
                boolean jumping = minecraft.options.keyJump.isDown();
                boolean shifting = minecraft.options.keyShift.isDown();
                NetworkHandler.sendToServer(new UpdateEtherealInputPacket(jumping, shifting));
            }
            if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isAlive()) {
                wasVampirebornKeyPressed = false;
                return;
            }

            races.Race race = races.getPlayerRace(minecraft.player);
            if (race != races.Race.VAMPIREBORN) {
                wasVampirebornKeyPressed = false;
                return;
            }

            boolean isVampirebornKeyPressed = VAMPIREBORN_ABILITY_KEY.isDown();

            if (isVampirebornKeyPressed && !wasVampirebornKeyPressed) {
                vampirebornPressTime = System.currentTimeMillis();
                wasVampirebornKeyPressed = true;
            }

            if (!isVampirebornKeyPressed && wasVampirebornKeyPressed) {
                long pressDuration = System.currentTimeMillis() - vampirebornPressTime;

                if (pressDuration < TAP_THRESHOLD) {
                    NetworkHandler.sendToServer(new VampirebornAbilityPacket(false));
                    TestingCoffeDinoMod.LOGGER.debug("Vampireborn single tap detected ({}ms)", pressDuration);
                } else {
                    NetworkHandler.sendToServer(new DeactivateVampirebornAbilityPacket());
                    TestingCoffeDinoMod.LOGGER.debug("Vampireborn hold released ({}ms)", pressDuration);
                }

                wasVampirebornKeyPressed = false;
            }

            if (isVampirebornKeyPressed && wasVampirebornKeyPressed) {
                long pressDuration = System.currentTimeMillis() - vampirebornPressTime;
                if (pressDuration >= TAP_THRESHOLD) {
                    if (pressDuration < TAP_THRESHOLD + 50) {
                        NetworkHandler.sendToServer(new VampirebornAbilityPacket(true));
                        TestingCoffeDinoMod.LOGGER.debug("Vampireborn hold started ({}ms)", pressDuration);
                    }
                }
            }

            wasVampirebornKeyPressed = isVampirebornKeyPressed;
            if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isAlive())
                return;



            if (race == races.Race.LOVER) {
                boolean isLoverKeyPressed = LOVER_ABILITY_KEY.isDown();
                if (isLoverKeyPressed && !wasLoverKeyPressed) {
                    if (!LoverAbilityHandler.isAbilityActive(minecraft.player) &&
                            LoverAbilityHandler.canActivateAbility(minecraft.player)) {
                        NetworkHandler.sendToServer(new ActivateLoverAbilityPacket());
                        TestingCoffeDinoMod.LOGGER.debug("Client: Sent Lover ability activation");
                    }
                }
                wasLoverKeyPressed = isLoverKeyPressed;
            }

        }
    }
}