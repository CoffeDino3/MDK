package com.CoffeDino.lunacy.network;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Lunacy.MODID);

        // PLAY_TO_SERVER packets
        registrar.playToServer(
                RaceSelectionPacket.TYPE,
                RaceSelectionPacket.STREAM_CODEC,
                RaceSelectionPacket::handle
        );
        registrar.playToServer(
                OpenSculkStoragePacket.TYPE,
                OpenSculkStoragePacket.STREAM_CODEC,
                OpenSculkStoragePacket::handle
        );
        registrar.playToServer(
                ActivateWarderAbilityPacket.TYPE,
                ActivateWarderAbilityPacket.STREAM_CODEC,
                ActivateWarderAbilityPacket::handle
        );
        registrar.playToServer(
                DeactivateWarderAbilityPacket.TYPE,
                DeactivateWarderAbilityPacket.STREAM_CODEC,
                DeactivateWarderAbilityPacket::handle
        );
        registrar.playToServer(
                AdjustWarderRingSizePacket.TYPE,
                AdjustWarderRingSizePacket.STREAM_CODEC,
                AdjustWarderRingSizePacket::handle
        );
        registrar.playToServer(
                ActivateLoverAbilityPacket.TYPE,
                ActivateLoverAbilityPacket.STREAM_CODEC,
                ActivateLoverAbilityPacket::handle
        );
        registrar.playToServer(
                DeactivateLoverAbilityPacket.TYPE,
                DeactivateLoverAbilityPacket.STREAM_CODEC,
                DeactivateLoverAbilityPacket::handle
        );
        registrar.playToServer(
                EnderTeleportPacket.TYPE,
                EnderTeleportPacket.STREAM_CODEC,
                EnderTeleportPacket::handle
        );
        registrar.playToServer(
                ActivatePhantomAbilityPacket.TYPE,
                ActivatePhantomAbilityPacket.STREAM_CODEC,
                ActivatePhantomAbilityPacket::handle
        );
        registrar.playToServer(
                TryPhantomMidAirJumpPacket.TYPE,
                TryPhantomMidAirJumpPacket.STREAM_CODEC,
                TryPhantomMidAirJumpPacket::handle
        );
        registrar.playToServer(
                BelieverAbilityPacket.TYPE,
                BelieverAbilityPacket.STREAM_CODEC,
                BelieverAbilityPacket::handle
        );
        registrar.playToServer(
                VampirebornAbilityPacket.TYPE,
                VampirebornAbilityPacket.STREAM_CODEC,
                VampirebornAbilityPacket::handle
        );
        registrar.playToServer(
                DeactivateVampirebornAbilityPacket.TYPE,
                DeactivateVampirebornAbilityPacket.STREAM_CODEC,
                DeactivateVampirebornAbilityPacket::handle
        );
        registrar.playToServer(
                ActivateEtherealAbilityPacket.TYPE,
                ActivateEtherealAbilityPacket.STREAM_CODEC,
                ActivateEtherealAbilityPacket::handle
        );
        registrar.playToServer(
                UpdateEtherealInputPacket.TYPE,
                UpdateEtherealInputPacket.STREAM_CODEC,
                UpdateEtherealInputPacket::handle
        );
        registrar.playToServer(
                ActivateAngelbornAbilityPacket.TYPE,
                ActivateAngelbornAbilityPacket.STREAM_CODEC,
                ActivateAngelbornAbilityPacket::handle
        );
        registrar.playToServer(
                ActivateCelestialAbilityPacket.TYPE,
                ActivateCelestialAbilityPacket.STREAM_CODEC,
                ActivateCelestialAbilityPacket::handle
        );
        registrar.playToServer(
                CelestialPushPacket.TYPE,
                CelestialPushPacket.STREAM_CODEC,
                CelestialPushPacket::handle
        );
        registrar.playToServer(
                CelestialPullPacket.TYPE,
                CelestialPullPacket.STREAM_CODEC,
                CelestialPullPacket::handle
        );
        registrar.playToServer(
                DeactivateCelestialAbilityPacket.TYPE,
                DeactivateCelestialAbilityPacket.STREAM_CODEC,
                DeactivateCelestialAbilityPacket::handle
        );
        registrar.playToServer(
                ClassSelectionPacket.TYPE,
                ClassSelectionPacket.STREAM_CODEC,
                ClassSelectionPacket::handle
        );
        registrar.playToServer(
                OpenClassSelectionPacket.TYPE,
                OpenClassSelectionPacket.STREAM_CODEC,
                OpenClassSelectionPacket::handle
        );
        registrar.playToServer(
                ActivateGatekeeperAbilityPacket.TYPE,
                ActivateGatekeeperAbilityPacket.STREAM_CODEC,
                ActivateGatekeeperAbilityPacket::handle
        );
        registrar.playToServer(
                DeactivateGatekeeperAbilityPacket.TYPE,
                DeactivateGatekeeperAbilityPacket.STREAM_CODEC,
                DeactivateGatekeeperAbilityPacket::handle
        );
        registrar.playToServer(
                DeactivateEtherealAbilityPacket.TYPE,
                DeactivateEtherealAbilityPacket.STREAM_CODEC,
                DeactivateEtherealAbilityPacket::handle
        );
        registrar.playToServer(
                ElementSelectionPacket.TYPE,
                ElementSelectionPacket.STREAM_CODEC,
                ElementSelectionPacket::handle
        );
        registrar.playToServer(
                RequestElementSelectionPacket.TYPE,
                RequestElementSelectionPacket.STREAM_CODEC,
                RequestElementSelectionPacket::handle
        );
        registrar.playToServer(
                ScrollSculkStoragePacket.TYPE,
                ScrollSculkStoragePacket.STREAM_CODEC,
                ScrollSculkStoragePacket::handle
        );

        // PLAY_TO_CLIENT packets
        registrar.playToClient(
                SyncPhaetonStatePacket.TYPE,
                SyncPhaetonStatePacket.STREAM_CODEC,
                SyncPhaetonStatePacket::handle
        );
        registrar.playToClient(
                SyncRacePacket.TYPE,
                SyncRacePacket.STREAM_CODEC,
                SyncRacePacket::handle
        );
        registrar.playToClient(
                RaceSizeSyncPacket.TYPE,
                RaceSizeSyncPacket.STREAM_CODEC,
                RaceSizeSyncPacket::handle
        );
        registrar.playToClient(
                SyncClassPacket.TYPE,
                SyncClassPacket.STREAM_CODEC,
                SyncClassPacket::handle
        );
        registrar.playToClient(
                SyncLevelXpPacket.TYPE,
                SyncLevelXpPacket.STREAM_CODEC,
                SyncLevelXpPacket::handle
        );
        registrar.playToClient(
                SyncChronobreakCooldownPacket.TYPE,
                SyncChronobreakCooldownPacket.STREAM_CODEC,
                SyncChronobreakCooldownPacket::handle
        );
        registrar.playToClient(
                OpenElementSelectionPacket.TYPE,
                OpenElementSelectionPacket.STREAM_CODEC,
                OpenElementSelectionPacket::handle
        );
    }

    public static void sendToServer(Object message) {
        PacketDistributor.sendToServer((net.minecraft.network.protocol.common.custom.CustomPacketPayload) message);
    }

    public static void sendToPlayer(net.minecraft.network.protocol.common.custom.CustomPacketPayload message, ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, message);
    }

    public static void syncRaceToClient(ServerPlayer player, races.Race race) {
        String raceId = race != null ? race.getId() : "";
        PacketDistributor.sendToPlayer(player, new SyncRacePacket(raceId));
    }

    public static void syncSizeToClient(ServerPlayer player, float height, float width) {
        PacketDistributor.sendToPlayer(player, new RaceSizeSyncPacket(height, width));
    }

    public static void openSculkStorage() {
        PacketDistributor.sendToServer(new OpenSculkStoragePacket());
    }

    public static void triggerEnderTeleport() {
        PacketDistributor.sendToServer(new EnderTeleportPacket());
    }

    public static void triggerBelieverAbility(boolean isShiftDown) {
        PacketDistributor.sendToServer(new BelieverAbilityPacket(isShiftDown));
    }

    public static void adjustWarderRingSize(boolean scrollUp) {
        PacketDistributor.sendToServer(new AdjustWarderRingSizePacket(scrollUp));
    }

    public static void tryPhantomMidAirJump() {
        PacketDistributor.sendToServer(new TryPhantomMidAirJumpPacket());
    }

    public static void syncClassToClient(ServerPlayer player, PlayerClasses.PlayerClass playerClass) {
        String classId = playerClass != null ? playerClass.getId() : "";
        PacketDistributor.sendToPlayer(player, new SyncClassPacket(classId));
    }

    public static void syncChronobreakCooldown(ServerPlayer player, long cooldownEnd) {
        PacketDistributor.sendToPlayer(player, new SyncChronobreakCooldownPacket(cooldownEnd));
    }

    public static void openClassSelection() {
        PacketDistributor.sendToServer(new OpenClassSelectionPacket());
    }
    public static void openElementSelection() {
        PacketDistributor.sendToServer(new OpenElementSelectionPacket());
    }
    public static void openElementSelectionForPlayer(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new OpenElementSelectionPacket());
    }
    public static void syncPhaetonStateToClient(ServerPlayer player, long riseEnd, long launchEnd, boolean diving) {
        PacketDistributor.sendToPlayer(player, new SyncPhaetonStatePacket(riseEnd, launchEnd, diving));
    }
    public static void syncLevelXpToClient(ServerPlayer player, int level, int xp, int xpToNextLevel) {
        PacketDistributor.sendToPlayer(player, new SyncLevelXpPacket(level, xp, xpToNextLevel));
    }
    public static void scrollSculkStorage(int scrollRows) {
        PacketDistributor.sendToServer(new ScrollSculkStoragePacket(scrollRows));
    }
}
