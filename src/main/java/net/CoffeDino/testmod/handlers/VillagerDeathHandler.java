package net.CoffeDino.testmod.handlers;

import net.CoffeDino.testmod.classes.PlayerClasses;
import net.CoffeDino.testmod.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VillagerDeathHandler {

    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            Entity source = event.getSource().getEntity();

            // Check if killed by a player with Gunsmith class
            if (source instanceof Player player) {
                if (PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.GUNSMITH) {
                    // Drop 1-3 lament bullets
                    if (villager.getRandom().nextFloat() < 0.8f) {
                        int bulletCount = 1 + villager.getRandom().nextInt(3);
                        ItemStack bullets = new ItemStack(ModItems.LAMENT_BULLET.get(), bulletCount);
                        villager.spawnAtLocation(bullets);
                    }
                }
            }
        }
    }
}