package net.CoffeDino.testmod.renderer;

import net.CoffeDino.testmod.entity.LamentBulletEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.world.phys.Vec3;

public class LamentBulletRenderer extends ThrownItemRenderer<LamentBulletEntity> {
    public LamentBulletRenderer(EntityRendererProvider.Context context) {
        super(context, 1.0f, true);
    }

    @Override
    public Vec3 getRenderOffset(LamentBulletEntity entity, float partialTicks) {
        return new Vec3(0, -0.1, 0);
    }
}