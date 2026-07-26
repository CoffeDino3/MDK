package com.CoffeDino.lunacy.entity;

import com.CoffeDino.lunacy.item.ModItems;
import com.CoffeDino.lunacy.races.races;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FloatingRapierEntity extends Entity {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM =
            SynchedEntityData.defineId(FloatingRapierEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Integer> DATA_ORBIT_INDEX =
            SynchedEntityData.defineId(FloatingRapierEntity.class, EntityDataSerializers.INT);

    private UUID ownerUUID;
    private float orbitAngleOffset;
    private int lifeTicks = 1200;

    private static final Map<Integer, double[][]> BASE_FORMATIONS = new HashMap<>();

    static {
        BASE_FORMATIONS.put(1, new double[][]{
                {-0.025, 2.3, 0.2}
        });

        BASE_FORMATIONS.put(2, new double[][]{
                {-0.725, 1.9, 0.2},
                {0.675, 1.9, 0.2}
        });

        BASE_FORMATIONS.put(3, new double[][]{
                {-0.025, 2.4, 0.2},
                {-0.725, 1.9, 0.2},
                {0.675, 1.9, 0.2}
        });

        BASE_FORMATIONS.put(4, new double[][]{
                {-0.725, 1.9, 0.2},
                {0.675, 1.9, 0.2},
                {-0.825, 1.4, 0.2},
                {0.775, 1.4, 0.2}
        });

        BASE_FORMATIONS.put(5, new double[][]{
                {-0.025, 2.4, 0.2},
                {-0.725, 1.9, 0.2},
                {0.675, 1.9, 0.2},
                {-0.825, 1.4, 0.2},
                {0.775, 1.4, 0.2}
        });

        BASE_FORMATIONS.put(6, new double[][]{
                {-0.725, 1.9, 0.2},
                {0.675, 1.9, 0.2},
                {-0.825, 1.4, 0.2},
                {0.775, 1.4, 0.2},
                {-0.725, 0.8, 0.2},
                {0.675, 0.8, 0.2}
        });

        BASE_FORMATIONS.put(7, new double[][]{
                {-0.025, 2.4, 0.2},
                {-0.725, 1.9, 0.2},
                {0.675, 1.9, 0.2},
                {-0.825, 1.4, 0.2},
                {0.775, 1.4, 0.2},
                {-0.725, 0.8, 0.2},
                {0.675, 0.8, 0.2}
        });
    }

    public FloatingRapierEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(DATA_ITEM, new ItemStack(ModItems.AMETHYST_RAPIER.get()));
        this.entityData.set(DATA_ORBIT_INDEX, 0);
    }

    public FloatingRapierEntity(Level level, Player owner, ItemStack displayStack, int orbitIndex) {
        this(ModEntities.FLOATING_RAPIER.get(), level);
        this.ownerUUID = owner.getUUID();
        this.orbitAngleOffset = orbitIndex * 45f;
        this.entityData.set(DATA_ITEM, displayStack.copy());
        this.entityData.set(DATA_ORBIT_INDEX, orbitIndex);
        this.setPos(owner.position());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ITEM, ItemStack.EMPTY);
        builder.define(DATA_ORBIT_INDEX, 0);
    }

    public int getOrbitIndex() {
        return this.entityData.get(DATA_ORBIT_INDEX);
    }

    public void setOrbitIndex(int index) {
        this.entityData.set(DATA_ORBIT_INDEX, index);
    }

    @Override
    public void tick() {
        super.tick();

        if (ownerUUID == null) {
            return;
        }

        if (!this.level().isClientSide) {
            this.lifeTicks--;
            if (this.lifeTicks <= 0) {
                this.discard();
                return;
            }
        }

        Player owner = getOwner();
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }

        float[] sizeFactors = getRaceSizeFactors(owner);
        float heightScale = sizeFactors[0];
        float widthScale = sizeFactors[1];

        int totalRapiers = getTotalRapierCount(owner);
        int index = this.getOrbitIndex();

        int safeCount = Mth.clamp(totalRapiers, 1, 7);
        double[][] baseFormation = BASE_FORMATIONS.getOrDefault(safeCount, BASE_FORMATIONS.get(1));
        double[] baseOffset = index < baseFormation.length ? baseFormation[index] : baseFormation[0];

        double localX = baseOffset[0] * widthScale;
        double localY = baseOffset[1] * heightScale;
        double localZ = baseOffset[2] * widthScale;

        float bodyYawRadians = owner.yBodyRot * ((float) Math.PI / 180F);
        float cosYaw = Mth.cos(bodyYawRadians);
        float sinYaw = Mth.sin(bodyYawRadians);

        double rotatedX = localX * cosYaw - localZ * sinYaw;
        double rotatedZ = localZ * cosYaw + localX * sinYaw;

        double targetX = owner.getX() + rotatedX;
        double targetY = owner.getY() + localY;
        double targetZ = owner.getZ() + rotatedZ;

        float time = (this.tickCount + (index * 15)) * 0.05F;

        targetX += Mth.sin(time) * 0.06D * widthScale;
        targetY += Mth.cos(time * 1.5F) * 0.08D * heightScale;
        targetZ += Mth.cos(time) * 0.04D * widthScale;

        double smoothX = Mth.lerp(0.25D, this.getX(), targetX);
        double smoothY = Mth.lerp(0.35D, this.getY(), targetY);
        double smoothZ = Mth.lerp(0.25D, this.getZ(), targetZ);

        setPos(smoothX, smoothY, smoothZ);

        float smoothYaw = Mth.rotLerp(0.20F, -this.getYRot(), owner.yBodyRot);
        setYRot(-smoothYaw);
        setXRot(0);
    }

    private float[] getRaceSizeFactors(Player player) {
        float defaultHeight = 1.8f;
        float defaultWidth = 0.6f;

        races.Race race = races.getPlayerRace(player);
        if (race == null) {
            return new float[]{1.0f, 1.0f};
        }

        float raceHeight = race.getHeight();
        float raceWidth = race.getWidth();

        float heightScale = raceHeight / defaultHeight;
        float widthScale = raceWidth / defaultWidth;

        return new float[]{heightScale, widthScale};
    }

    private int getTotalRapierCount(Player player) {
        int count = 0;
        for (var entity : player.level().getEntitiesOfClass(FloatingRapierEntity.class,
                player.getBoundingBox().inflate(10.0))) {
            Player owner = entity.getOwner();
            if (owner != null && owner.getUUID().equals(player.getUUID())) {
                count++;
            }
        }
        return count;
    }

    public Player getOwner() {
        if (ownerUUID == null) return null;
        return level().getPlayerByUUID(ownerUUID);
    }

    public ItemStack getDisplayItem() {
        return entityData.get(DATA_ITEM);
    }
    public static void reflowFormation(Player player) {
        List<FloatingRapierEntity> remaining = player.level().getEntitiesOfClass(
                FloatingRapierEntity.class,
                player.getBoundingBox().inflate(10.0),
                entity -> {
                    Player owner = entity.getOwner();
                    return owner != null && owner.getUUID().equals(player.getUUID());
                });

        remaining.sort(Comparator.comparingInt(FloatingRapierEntity::getOrbitIndex));

        for (int newIndex = 0; newIndex < remaining.size(); newIndex++) {
            remaining.get(newIndex).setOrbitIndex(newIndex);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) ownerUUID = tag.getUUID("Owner");
        orbitAngleOffset = tag.getFloat("OrbitOffset");
        if (tag.contains("LifeTicks")) this.lifeTicks = tag.getInt("LifeTicks");

        if (tag.contains("DisplayItem")) {
            try {
                ItemStack stack = ItemStack.parse(this.registryAccess(), tag.getCompound("DisplayItem")).orElse(new ItemStack(ModItems.AMETHYST_RAPIER.get()));
                this.entityData.set(DATA_ITEM, stack);
            } catch (Exception e) {
                this.entityData.set(DATA_ITEM, new ItemStack(ModItems.AMETHYST_RAPIER.get()));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) tag.putUUID("Owner", ownerUUID);
        tag.putFloat("OrbitOffset", orbitAngleOffset);
        tag.putInt("LifeTicks", this.lifeTicks);

        ItemStack stack = this.entityData.get(DATA_ITEM);
        if (!stack.isEmpty()) {
            tag.put("DisplayItem", stack.save(this.registryAccess()));
        }
    }
}