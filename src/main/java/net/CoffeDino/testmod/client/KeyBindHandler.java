package net.CoffeDino.testmod.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.CoffeDino.testmod.Lunacy;
import net.CoffeDino.testmod.abilities.CelestialAbilityHandler;
import net.CoffeDino.testmod.abilities.EtherealAbilityHandler;
import net.CoffeDino.testmod.abilities.LoverAbilityHandler;
import net.CoffeDino.testmod.classes.PlayerClasses;
import net.CoffeDino.testmod.client.gui.ClassSelectionScreen;
import net.CoffeDino.testmod.network.*;
import net.CoffeDino.testmod.races.races;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Lunacy.MOD_ID, value = Dist.CLIENT)
public class KeyBindHandler {
    public static final KeyMapping SCULK_STORAGE_KEY = new KeyMapping(
            "key.lunacy.sculk_storage",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping WARDER_ABILITY_KEY = new KeyMapping(
            "key.lunacy.warder_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping LOVER_ABILITY_KEY = new KeyMapping(
            "key.lunacy.lover_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping ENDER_TELEPORT_KEY = new KeyMapping(
            "key.lunacy.ender_teleport",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R, // Same key as sculk, but will check race
            "category.lunacy.abilities"
    );
    public static final KeyMapping PHANTOM_ABILITY_KEY = new KeyMapping(
            "key.lunacy.phantom_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping BELIEVER_ABILITY_KEY = new KeyMapping(
            "key.lunacy.believer_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping VAMPIREBORN_ABILITY_KEY = new KeyMapping(
            "key.lunacy.vampireborn_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping ETHEREAL_ABILITY_KEY = new KeyMapping(
            "key.lunacy.ethereal_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping ANGELBORN_ABILITY_KEY = new KeyMapping(
            "key.lunacy.angelborn_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping CELESTIAL_ABILITY_KEY = new KeyMapping(
            "key.lunacy.celestial_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping CLASS_SELECTION_KEY = new KeyMapping(
            "key.lunacy.class_selection",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C, // C key for class selection
            "category.lunacy.general"
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
        event.register(ANGELBORN_ABILITY_KEY);
        event.register(CELESTIAL_ABILITY_KEY);
        event.register(CLASS_SELECTION_KEY);
    }

    private static boolean wasLoverKeyPressed = false;
    private static boolean wasVampirebornKeyPressed = false;
    private static long vampirebornPressTime = 0;
    private static final long TAP_THRESHOLD = 200; // milliseconds
    private static boolean wasCelestialKeyPressed = false;
    private static boolean celestialAbilityActive = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft minecraft = Minecraft.getInstance();
            if (CLASS_SELECTION_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    // Only open if player hasn't chosen a class yet
                    if (!PlayerClasses.hasChosenClass(minecraft.player)) {
                        minecraft.setScreen(new ClassSelectionScreen());
                    }
                }
            }

            boolean isCelestialKeyPressed = CELESTIAL_ABILITY_KEY.isDown();

            if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                races.Race race = races.getPlayerRace(minecraft.player);
                if (race == races.Race.CELESTIAL) {
                    // Key pressed - activate if not active and no cooldown
                    if (isCelestialKeyPressed && !wasCelestialKeyPressed && !celestialAbilityActive) {
                        if (CelestialAbilityHandler.canActivateAbility(minecraft.player)) {
                            NetworkHandler.sendToServer(new ActivateCelestialAbilityPacket());
                            celestialAbilityActive = true;
                        }
                    }
                    // Key released - deactivate if active
                    else if (!isCelestialKeyPressed && wasCelestialKeyPressed && celestialAbilityActive) {
                        NetworkHandler.sendToServer(new DeactivateCelestialAbilityPacket());
                        celestialAbilityActive = false;
                    }
                } else {
                    // Not celestial race - reset state
                    celestialAbilityActive = false;
                }
            } else {
                // No valid player - reset state
                celestialAbilityActive = false;
            }

            wasCelestialKeyPressed = isCelestialKeyPressed;

            if (ANGELBORN_ABILITY_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race = races.getPlayerRace(minecraft.player);
                    if (race == races.Race.ANGELBORN) {
                        NetworkHandler.sendToServer(new ActivateAngelbornAbilityPacket());
                    }
                }
            }

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
                if (race == races.Race.ENDER) {
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
            if (LOVER_ABILITY_KEY.consumeClick()) {
                if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                    races.Race race2 = races.getPlayerRace(minecraft.player);
                    if (race2 == races.Race.LOVER) {
                        if (!LoverAbilityHandler.isAbilityActive(minecraft.player) &&
                                LoverAbilityHandler.canActivateAbility(minecraft.player)) {
                            NetworkHandler.sendToServer(new ActivateLoverAbilityPacket());
                            Lunacy.LOGGER.debug("Client: Sent Lover ability activation");
                        }
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
                    Lunacy.LOGGER.debug("Vampireborn single tap detected ({}ms)", pressDuration);
                } else {
                    NetworkHandler.sendToServer(new DeactivateVampirebornAbilityPacket());
                    Lunacy.LOGGER.debug("Vampireborn hold released ({}ms)", pressDuration);
                }

                wasVampirebornKeyPressed = false;
            }

            if (isVampirebornKeyPressed && wasVampirebornKeyPressed) {
                long pressDuration = System.currentTimeMillis() - vampirebornPressTime;
                if (pressDuration >= TAP_THRESHOLD) {
                    if (pressDuration < TAP_THRESHOLD + 50) {
                        NetworkHandler.sendToServer(new VampirebornAbilityPacket(true));
                        Lunacy.LOGGER.debug("Vampireborn hold started ({}ms)", pressDuration);
                    }
                }
            }

            wasVampirebornKeyPressed = isVampirebornKeyPressed;
        }
    }
    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isAlive()) return;

        races.Race race = races.getPlayerRace(minecraft.player);
        if (race != races.Race.CELESTIAL || !CelestialAbilityHandler.isAbilityActive(minecraft.player)) {
            return;
        }

        if (event.getButton() == 1 && event.getAction() == InputConstants.PRESS) { // Right click
            NetworkHandler.sendToServer(new CelestialPushPacket());
            // REMOVED: event.setCanceled(true);
        } else if (event.getButton() == 0 && event.getAction() == InputConstants.PRESS) { // Left click
            NetworkHandler.sendToServer(new CelestialPullPacket());
            // REMOVED: event.setCanceled(true);
        }
    }
}