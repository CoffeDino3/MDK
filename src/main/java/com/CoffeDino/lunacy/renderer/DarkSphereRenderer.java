package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.DarkSphereEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class DarkSphereRenderer extends EntityRenderer<DarkSphereEntity> {

    public DarkSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(DarkSphereEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/pixel.png");
    }
}