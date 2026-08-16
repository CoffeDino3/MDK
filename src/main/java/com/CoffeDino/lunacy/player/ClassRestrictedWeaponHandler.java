package com.CoffeDino.lunacy.player;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.ClassDataManager;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.classes.PlayerClasses.PlayerClass;
import com.CoffeDino.lunacy.classes.SpellbladeElement;
import com.CoffeDino.lunacy.item.Custom.*;
import com.CoffeDino.lunacy.leveling.PlayerLevels;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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

    public static final int REQUIRED_LEVEL = 30;
    private record Requirement(PlayerClass requiredClass, int requiredLevel, SpellbladeElement requiredElement) {}

    private static final Map<Class<? extends Item>, Requirement> REQUIREMENTS = new LinkedHashMap<>();
    static {
        REQUIREMENTS.put(FireSpearItem.class,
                new Requirement(PlayerClass.SPEARMAN, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(AmethystRapierItem.class,
                new Requirement(PlayerClass.FENCER, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(ShiBowItem.class,
                new Requirement(PlayerClass.ARCHER, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(SoulScytheItem.class,
                new Requirement(PlayerClass.REAPER, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(ViridyumGreatswordItem.class,
                new Requirement(PlayerClass.HEAVY_KNIGHT, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(LamentGunItem.class,
                new Requirement(PlayerClass.GUNSMITH, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(ObsidiaItem.class,
                new Requirement(PlayerClass.ASSASSIN, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(BorontItem.class,
                new Requirement(PlayerClass.VIKING, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(RocaItem.class,
                new Requirement(PlayerClass.SWORDSMAN, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(GruckItem.class,
                new Requirement(PlayerClass.GUARDIAN, REQUIRED_LEVEL, null));
        REQUIREMENTS.put(CharybdisItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.VOID));
        REQUIREMENTS.put(HeliosItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.LIGHT));
        REQUIREMENTS.put(JoroItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.EARTH));
        REQUIREMENTS.put(ErinyesItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.BLOOD));
        REQUIREMENTS.put(MoiraiItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.ETHER));
        REQUIREMENTS.put(BoreasItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.WIND));
        REQUIREMENTS.put(PhaetonItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.FIRE));
        REQUIREMENTS.put(PerunItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.LIGHTNING));
        REQUIREMENTS.put(AmphitriteItem.class,
                new Requirement(PlayerClass.SPELLBLADE, REQUIRED_LEVEL, SpellbladeElement.WATER));
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
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        PlayerClass currentClass = PlayerClasses.getPlayerClass(player);
        int currentLevel = PlayerLevels.getLevel(serverPlayer);
        SpellbladeElement currentElement = getPlayerElement(serverPlayer);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            Requirement requirement = REQUIREMENTS.get(stack.getItem().getClass());
            if (requirement == null || meetsRequirement(currentClass, currentLevel, currentElement, requirement)) continue;

            player.drop(stack, false);
            player.getInventory().setItem(i, ItemStack.EMPTY);
            reject(player, requirement, currentClass, currentLevel, currentElement);
        }
        ItemStack offHand = player.getOffhandItem();
        if (!offHand.isEmpty()) {
            Requirement requirement = REQUIREMENTS.get(offHand.getItem().getClass());
            if (requirement != null && !meetsRequirement(currentClass, currentLevel, currentElement, requirement)) {
                player.getInventory().removeItem(offHand);
                player.drop(offHand, false);
                reject(player, requirement, currentClass, currentLevel, currentElement);
            }
        }
    }

    private static SpellbladeElement getPlayerElement(ServerPlayer player) {
        ClassDataManager dataManager = ClassDataManager.get(player);
        String elementId = dataManager.getPlayerElement(player.getUUID());
        return SpellbladeElement.fromId(elementId);
    }

    private static boolean meetsRequirement(PlayerClass currentClass, int currentLevel, SpellbladeElement currentElement, Requirement requirement) {
        if (currentClass != requirement.requiredClass()) return false;
        if (currentLevel < requirement.requiredLevel()) return false;
        if (requirement.requiredElement() != null && requirement.requiredElement() != currentElement) return false;
        return true;
    }
    private static void reject(Player player, Requirement requirement, PlayerClass currentClass, int currentLevel, SpellbladeElement currentElement) {
        String message;
        if (currentClass != requirement.requiredClass()) {
            message = "You are not a " + requirement.requiredClass().getDisplayName() + ", you dont know how to wield this weapon.";
        } else if (currentLevel < requirement.requiredLevel()) {
            message = "You are too weak to wield this weapon.";
        } else if (requirement.requiredElement() != null && requirement.requiredElement() != currentElement) {
            message = "You do not have mastery over this element.";
        } else {
            message = "You are unable to wield this weapon.";
        }

        player.displayClientMessage(Component.literal(message)
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.ITEM_BREAK, 1.0F, 1.0F);
    }
}