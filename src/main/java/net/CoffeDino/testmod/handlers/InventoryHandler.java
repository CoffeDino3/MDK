package net.CoffeDino.testmod.handlers;

import net.CoffeDino.testmod.classes.PlayerClasses;
import net.CoffeDino.testmod.item.Custom.GunItem;
import net.CoffeDino.testmod.item.ModItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class InventoryHandler {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        checkAndRemoveGun(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        checkAndRemoveGun(event.getEntity());
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        checkAndRemoveGun(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(net.minecraftforge.event.TickEvent.PlayerTickEvent event) {
        if (event.phase == net.minecraftforge.event.TickEvent.Phase.END && event.player.tickCount % 20 == 0) {
            checkAndRemoveGun(event.player);
        }
    }
    public static void checkAndRemoveGun(Player player) {
        if (player.level().isClientSide()) return;

        boolean isGunsmith = PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.GUNSMITH;

        // Check all inventory slots for the gun
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            if (stack.getItem() instanceof GunItem) {
                if (!isGunsmith) {
                    player.getInventory().removeItem(stack);
                    player.drop(stack, false);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("The gun slips from your hands as you are not a Gunsmith!"), true);
                }
            }
        }

        if (!isGunsmith) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            if (mainHand.getItem() instanceof GunItem) {
                player.getInventory().removeItem(mainHand);
                player.drop(mainHand, false);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("The gun slips from your hands as you are not a Gunsmith!"), true);
            }

            if (offHand.getItem() instanceof GunItem) {
                player.getInventory().removeItem(offHand);
                player.drop(offHand, false);
                player.displayClientMessage(net.minecraft.network.chat.Component.literal("The gun slips from your hands as you are not a Gunsmith!"), true);
            }
        }
    }
}