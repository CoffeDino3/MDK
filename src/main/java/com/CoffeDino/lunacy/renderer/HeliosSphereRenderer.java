package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.entity.HeliosSphereEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class HeliosSphereRenderer extends EntityRenderer<HeliosSphereEntity> {

    public HeliosSphereRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HeliosSphereEntity entity) {
        return ResourceLocation.withDefaultNamespace("textures/misc/pixel.png");
    }
}