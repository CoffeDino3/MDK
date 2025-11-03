package net.CoffeDino.testmod.renderer;

import net.CoffeDino.testmod.TestingCoffeDinoMod;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class AngelbornRenderer extends EntityRenderer<Entity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TestingCoffeDinoMod.MOD_ID, "textures/entity/angelborn.png");

    public AngelbornRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(Entity entity) {
        return TEXTURE;
    }
}