package com.CoffeDino.lunacy.item;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum BulletEnhancement implements StringRepresentable {
    NONE("none", 1.0f, 0, 0, 0f),
    EXPLOSIVE("explosive", 1.0f, 0, 4, 2.0f),
    HEAVY("heavy", 1.5f, 3, 4, 0f),
    TOXIC("toxic", 1.0f, 0, 4, 0f),
    PIERCING("piercing", 2.0f, 7, 6, 0f),
    RICOCHET("ricochet", 1.0f, 0, 4, 0f),
    IGNITE("ignite", 1.0f, 0, 4, 0f),
    ROOTED("rooted", 1.0f, 0, 4, 0f),
    SOAKED("soaked", 1.0f, 0, 3, 0f),
    KNOCKBACK("knockback", 1.0f, 0, 4, 0f),
    STATIC("static", 1.0f, 0, 5, 0f),
    VOLATILE("volatile", 1.0f, 0, 5, 0f);

    public static final Codec<BulletEnhancement> CODEC = StringRepresentable.fromEnum(BulletEnhancement::values);
    public static final StreamCodec<RegistryFriendlyByteBuf, BulletEnhancement> STREAM_CODEC =
            StreamCodec.of((buf, value) -> buf.writeEnum(value), buf -> buf.readEnum(BulletEnhancement.class));

    private final String id;
    private final float damageMultiplier;
    private final int pierceCount;
    private final int anvilCost;
    private final float explosionPower;

    BulletEnhancement(String id, float damageMultiplier, int pierceCount, int anvilCost, float explosionPower) {
        this.id = id;
        this.damageMultiplier = damageMultiplier;
        this.pierceCount = pierceCount;
        this.anvilCost = anvilCost;
        this.explosionPower = explosionPower;
    }

    public float getDamageMultiplier() { return damageMultiplier; }
    public int getPierceCount() { return pierceCount; }
    public int getAnvilCost() { return anvilCost; }
    public float getExplosionPower() { return explosionPower; }
    public Component displayName() { return Component.translatable("bullet_enhancement.lunacy." + id); }

    @Override
    public String getSerializedName() { return id; }
}