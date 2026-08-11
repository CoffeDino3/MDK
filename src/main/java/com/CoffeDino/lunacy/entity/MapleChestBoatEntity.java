package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.item.ModItems;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MapleChestBoatEntity extends ChestBoat {

    public MapleChestBoatEntity(EntityType<? extends ChestBoat> entityType, Level level) {
        super(entityType, level);
    }

    public MapleChestBoatEntity(Level level, double x, double y, double z) {
        this(ModEntities.MAPLE_CHEST_BOAT.get(), level);
        this.setPos(x, y, z);
        this.xo = x;
        this.yo = y;
        this.zo = z;
    }

    @Override
    public Item getDropItem() {
        return ModItems.MAPLE_CHEST_BOAT.get();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions dimensions, float partialTick) {
        float zOffset = this.getSinglePassengerXOffset();
        if (this.getPassengers().size() > 1) {
            int index = this.getPassengers().indexOf(entity);
            zOffset = (index == 0) ? 0.2F : -0.6F;
            if (entity instanceof Animal) {
                zOffset += 0.2F;
            }
        }
        return (new Vec3(0.0D, -0.35D, (double) zOffset))
                .yRot(-this.getYRot() * ((float) Math.PI / 180F));
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction) {
        super.positionRider(passenger, moveFunction);
        this.clampRotation(passenger);
    }

    @Override
    protected void clampRotation(Entity entityToUpdate) {
        entityToUpdate.setYBodyRot(this.getYRot());
        float f = Mth.wrapDegrees(entityToUpdate.getYRot() - this.getYRot());
        float f1 = Mth.clamp(f, -105.0F, 105.0F);
        entityToUpdate.yRotO += f1 - f;
        entityToUpdate.setYRot(entityToUpdate.getYRot() + f1 - f);
        entityToUpdate.setYHeadRot(entityToUpdate.getYRot());
    }
}