package com.CoffeDino.lunacy.handlers;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.classes.PlayerClasses;
import com.CoffeDino.lunacy.item.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

@EventBusSubscriber(modid = Lunacy.MODID)
public class VillagerDeathHandler {

    @SubscribeEvent
    public static void onVillagerDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            Entity source = event.getSource().getEntity();
            if (source instanceof Player player) {
                if (PlayerClasses.getPlayerClass(player) == PlayerClasses.PlayerClass.GUNSMITH) {
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