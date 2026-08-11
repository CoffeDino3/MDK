package com.CoffeDino.lunacy.item.Custom;

import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Supplier;

public class ModBoatItem extends Item {
    private final Supplier<? extends EntityType<? extends Boat>> entityTypeSupplier;
    private final boolean hasChest;

    public ModBoatItem(boolean hasChest, Supplier<? extends EntityType<? extends Boat>> entityTypeSupplier, Properties properties) {
        super(properties);
        this.hasChest = hasChest;
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        HitResult hitResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);

        if (hitResult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemStack);
        }

        Vec3 viewVector = player.getViewVector(1.0F);
        List<Entity> list = level.getEntities(player, player.getBoundingBox().expandTowards(viewVector.scale(5.0D)).inflate(1.0D), EntitySelector.NO_SPECTATORS.and(Entity::isPickable));
        if (!list.isEmpty()) {
            Vec3 eyePosition = player.getEyePosition();
            for (Entity entity : list) {
                AABB boundingBox = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (boundingBox.contains(eyePosition)) {
                    return InteractionResultHolder.pass(itemStack);
                }
            }
        }

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            Boat boat = this.entityTypeSupplier.get().create(level);
            if (boat == null) {
                return InteractionResultHolder.pass(itemStack);
            }

            boat.setPos(hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z);
            boat.setYRot(player.getYRot());

            if (!level.noCollision(boat, boat.getBoundingBox())) {
                return InteractionResultHolder.fail(itemStack);
            } else {
                if (!level.isClientSide) {
                    level.addFreshEntity(boat);
                    level.gameEvent(player, GameEvent.ENTITY_PLACE, hitResult.getLocation());
                    itemStack.consume(1, player);
                }

                player.awardStat(Stats.ITEM_USED.get(this));
                return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
            }
        } else {
            return InteractionResultHolder.pass(itemStack);
        }
    }
}