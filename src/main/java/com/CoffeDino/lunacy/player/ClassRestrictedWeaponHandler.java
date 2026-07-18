package com.CoffeDino.lunacy.player;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.classes.PlayerClasses.PlayerClass;
import com.CoffeDino.lunacy.item.Custom.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@EventBusSubscriber(modid = Lunacy.MODID)
public class ClassRestrictedWeaponHandler {

    private record Requirement(PlayerClass requiredClass, String rejectMessage) {}

    private static final Map<Class<? extends Item>, Requirement> REQUIREMENTS = new LinkedHashMap<>();
    static {
        REQUIREMENTS.put(FireSpearItem.class,
                new Requirement(PlayerClass.SPEARMAN, "You are not a Spearman! The spear rejects you."));
        REQUIREMENTS.put(AmethystRapierItem.class,
                new Requirement(PlayerClass.FENCER, "You are not a Fencer! The rapier rejects you."));
        REQUIREMENTS.put(ShiBowItem.class,
                new Requirement(PlayerClass.ARCHER, "You are not an Archer! The bow rejects you."));
        REQUIREMENTS.put(SoulScytheItem.class,
                new Requirement(PlayerClass.REAPER, "You are not a Reaper! The scythe rejects you."));
        REQUIREMENTS.put(ViridyumGreatswordItem.class,
                new Requirement(PlayerClass.CHRONOBLADE, "You are not a Chronoblade! The greatsword rejects you."));
        REQUIREMENTS.put(GunItem.class,
                new Requirement(PlayerClass.GUNSMITH, "The gun slips from your hands as you are not a Gunsmith!"));
        REQUIREMENTS.put(ObsidiaItem.class,
                new Requirement(PlayerClass.ASSASSIN, "You are not an Assassin! The dagger rejects you."));
        REQUIREMENTS.put(BorontItem.class,
                new Requirement(PlayerClass.VIKING, "You are not a Viking! The axe rejects you."));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        checkAndRemoveInvalidWeapons(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        checkAndRemoveInvalidWeapons(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        checkAndRemoveInvalidWeapons(event.getEntity());
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player && !event.getLevel().isClientSide()) {
            checkAndRemoveInvalidWeapons(player);
        }
    }
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.tickCount % 20 == 0) {
            checkAndRemoveInvalidWeapons(player);
        }
    }

    public static void checkAndRemoveInvalidWeapons(Player player) {
        if (player.level().isClientSide()) return;
        if (REQUIREMENTS.isEmpty()) return;
        PlayerClass currentClass = PlayerClasses.getPlayerClass(player);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Requirement requirement = REQUIREMENTS.get(stack.getItem().getClass());
            if (requirement == null || currentClass == requirement.requiredClass()) continue;

            player.drop(stack, false);
            player.getInventory().setItem(i, ItemStack.EMPTY);
            reject(player, requirement);
        }
        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty()) {
            Requirement requirement = REQUIREMENTS.get(offHand.getItem().getClass());
            if (requirement != null && currentClass != requirement.requiredClass()) {
                player.getInventory().removeItem(offHand);
                player.drop(offHand, false);
                reject(player, requirement);
            }
        }
    }
    private static void reject(Player player, Requirement requirement) {
        player.displayClientMessage(Component.literal(requirement.rejectMessage())
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
    }
}
