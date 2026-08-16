package com.CoffeDino.lunacy.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.abilities.CelestialAbilityHandler;
import com.CoffeDino.lunacy.abilities.EtherealAbilityHandler;
import com.CoffeDino.lunacy.abilities.LoverAbilityHandler;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.client.gui.ClassSelectionScreen;
import com.CoffeDino.lunacy.client.gui.PlayerDataScreen;
import com.CoffeDino.lunacy.effects.AbilityCooldown;
import com.CoffeDino.lunacy.effects.ModEffects;
import com.CoffeDino.lunacy.network.*;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = Lunacy.MODID, value = Dist.CLIENT)
public class KeyBindHandler {
    public static final KeyMapping RACE_ABILITY_KEY = new KeyMapping(
            "key.lunacy.race_ability",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            "category.lunacy.abilities"
    );
    public static final KeyMapping CLASS_SELECTION_KEY = new KeyMapping(
            "key.lunacy.class_selection",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_C,
            "category.lunacy.general"
    );
    public static final KeyMapping OPEN_PLAYER_DATA_KEY = new KeyMapping(
            "key.lunacy.player_data",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_SEMICOLON,
            "category.lunacy.general"
    );

    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(RACE_ABILITY_KEY);
        event.register(CLASS_SELECTION_KEY);
        event.register(OPEN_PLAYER_DATA_KEY);
    }

    private static boolean wasVampirebornKeyPressed = false;
    private static long vampirebornPressTime = 0;
    private static final long TAP_THRESHOLD = 200;
    private static boolean wasCelestialKeyPressed = false;
    private static boolean celestialAbilityActive = false;
    private static boolean wasGatekeeperKeyPressed = false;
    private static boolean gatekeeperAbilityActive = false;
    private static boolean wasJumpKeyPressedForPhantom = false;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();

        if (CLASS_SELECTION_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
                if (!PlayerClasses.hasChosenClass(minecraft.player)) {
                    minecraft.setScreen(new ClassSelectionScreen());
                } else if (PlayerClasses.getPlayerClass(minecraft.player) == PlayerClasses.PlayerClass.SPELLBLADE) {
                    NetworkHandler.sendToServer(new RequestElementSelectionPacket());
                }
            }
        }
        if (OPEN_PLAYER_DATA_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new PlayerDataScreen());
            }
        }
        if (minecraft.player != null && minecraft.screen == null && minecraft.player.isAlive()) {
            races.Race jumpRace = races.getPlayerRace(minecraft.player);
            boolean isJumpKeyPressed = minecraft.options.keyJump.isDown();
            if (jumpRace == races.Race.PHANTOM && !minecraft.player.onGround()) {
                if (isJumpKeyPressed && !wasJumpKeyPressedForPhantom) {
                    NetworkHandler.tryPhantomMidAirJump();
                }
            }
            wasJumpKeyPressedForPhantom = isJumpKeyPressed;
        } else {
            wasJumpKeyPressedForPhantom = false;
        }

        if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isAlive()) {
            resetTransientKeyState();
            return;
        }

        races.Race race = races.getPlayerRace(minecraft.player);
        if (race == null) {
            resetTransientKeyState();
            return;
        }

        switch (race) {
            case SCULK -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    NetworkHandler.openSculkStorage();
                }
            }
            case WARDER -> handleWarder();
            case ENDER -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    NetworkHandler.triggerEnderTeleport();
                }
            }
            case PHANTOM -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    NetworkHandler.sendToServer(new ActivatePhantomAbilityPacket());
                }
            }
            case LOVER -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    if (!LoverAbilityHandler.isAbilityActive(minecraft.player) &&
                            !AbilityCooldown.isActive(minecraft.player, ModEffects.LOVER_COOLDOWN)) {
                        NetworkHandler.sendToServer(new ActivateLoverAbilityPacket());
                        Lunacy.LOGGER.debug("Client: Sent Lover ability activation");
                    }
                }
            }
            case BELIEVER -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    boolean shifting = minecraft.options.keyShift.isDown();
                    NetworkHandler.sendToServer(new BelieverAbilityPacket(shifting));
                }
            }
            case VAMPIREBORN -> handleVampireborn();
            case ETHEREAL -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    if (EtherealAbilityHandler.isAbilityActive(minecraft.player)) {
                        NetworkHandler.sendToServer(new DeactivateEtherealAbilityPacket());
                    } else {
                        boolean jumping = minecraft.options.keyJump.isDown();
                        boolean shifting = minecraft.options.keyShift.isDown();
                        NetworkHandler.sendToServer(new ActivateEtherealAbilityPacket(jumping, shifting));
                    }
                }
                if (EtherealAbilityHandler.isAbilityActive(minecraft.player)) {
                    boolean jumping = minecraft.options.keyJump.isDown();
                    boolean shifting = minecraft.options.keyShift.isDown();
                    NetworkHandler.sendToServer(new UpdateEtherealInputPacket(jumping, shifting));
                }
            }
            case ANGELBORN -> {
                if (RACE_ABILITY_KEY.consumeClick()) {
                    NetworkHandler.sendToServer(new ActivateAngelbornAbilityPacket());
                }
            }
            case CELESTIAL -> handleCelestial();
            case GATEKEEPER -> handleGatekeeper();
        }
        if (race != races.Race.VAMPIREBORN) wasVampirebornKeyPressed = false;
        if (race != races.Race.CELESTIAL) { wasCelestialKeyPressed = false; celestialAbilityActive = false; }
        if (race != races.Race.GATEKEEPER) { wasGatekeeperKeyPressed = false; gatekeeperAbilityActive = false; }
    }

    private static void resetTransientKeyState() {
        wasVampirebornKeyPressed = false;
        wasCelestialKeyPressed = false;
        celestialAbilityActive = false;
        wasGatekeeperKeyPressed = false;
        gatekeeperAbilityActive = false;
        wasJumpKeyPressedForPhantom = false;
    }

    private static void handleWarder() {
        if (RACE_ABILITY_KEY.isDown()) {
            NetworkHandler.sendToServer(new ActivateWarderAbilityPacket());
        } else {
            NetworkHandler.sendToServer(new DeactivateWarderAbilityPacket());
        }
    }

    private static void handleCelestial() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean isCelestialKeyPressed = RACE_ABILITY_KEY.isDown();
        if (isCelestialKeyPressed && !wasCelestialKeyPressed && !celestialAbilityActive) {
            if (!AbilityCooldown.isActive(minecraft.player, ModEffects.CELESTIAL_COOLDOWN)) {
                NetworkHandler.sendToServer(new ActivateCelestialAbilityPacket());
                celestialAbilityActive = true;
            }
        } else if (!isCelestialKeyPressed && wasCelestialKeyPressed && celestialAbilityActive) {
            NetworkHandler.sendToServer(new DeactivateCelestialAbilityPacket());
            celestialAbilityActive = false;
        }
        wasCelestialKeyPressed = isCelestialKeyPressed;
    }

    private static void handleGatekeeper() {
        Minecraft minecraft = Minecraft.getInstance();
        boolean isGatekeeperKeyPressed = RACE_ABILITY_KEY.isDown();
        if (isGatekeeperKeyPressed && !wasGatekeeperKeyPressed && !gatekeeperAbilityActive) {
            if (minecraft.player.getFoodData().getFoodLevel() > 0) {
                NetworkHandler.sendToServer(new ActivateGatekeeperAbilityPacket());
                gatekeeperAbilityActive = true;
            }
        } else if (!isGatekeeperKeyPressed && wasGatekeeperKeyPressed && gatekeeperAbilityActive) {
            NetworkHandler.sendToServer(new DeactivateGatekeeperAbilityPacket());
            gatekeeperAbilityActive = false;
        }
        wasGatekeeperKeyPressed = isGatekeeperKeyPressed;
    }

    private static void handleVampireborn() {
        boolean isVampirebornKeyPressed = RACE_ABILITY_KEY.isDown();

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
            if (pressDuration >= TAP_THRESHOLD && pressDuration < TAP_THRESHOLD + 50) {
                NetworkHandler.sendToServer(new VampirebornAbilityPacket(true));
                Lunacy.LOGGER.debug("Vampireborn hold started ({}ms)", pressDuration);
            }
        }
    }

    @SubscribeEvent
    public static void onMouseClick(InputEvent.MouseButton.Pre event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isAlive()) return;
        races.Race race = races.getPlayerRace(minecraft.player);
        if (race != races.Race.CELESTIAL || !CelestialAbilityHandler.isAbilityActive(minecraft.player)) {
            return;
        }

        if (event.getButton() == 1 && event.getAction() == InputConstants.PRESS) {
            NetworkHandler.sendToServer(new CelestialPushPacket());
        } else if (event.getButton() == 0 && event.getAction() == InputConstants.PRESS) {
            NetworkHandler.sendToServer(new CelestialPullPacket());
        }
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null || !minecraft.player.isAlive()) return;

        races.Race race = races.getPlayerRace(minecraft.player);
        if (race != races.Race.WARDER) return;
        if (!RACE_ABILITY_KEY.isDown()) return;

        double delta = event.getScrollDeltaY();
        if (delta == 0) return;

        NetworkHandler.adjustWarderRingSize(delta > 0);
        event.setCanceled(true);
    }
}