package com.CoffeDino.lunacy.renderer;

import com.CoffeDino.lunacy.Lunacy;
import com.CoffeDino.lunacy.events.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GruckRenderer extends BlockEntityWithoutLevelRenderer {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Lunacy.MODID, "textures/entity/gruck.png");
    private ShieldModel model;

    public GruckRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (this.model == null) {
            this.model = new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.GRUCK));
        }

        poseStack.pushPose();
        poseStack.scale(1.0F, -1.0F, -1.0F);
        VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(
                bufferSource, model.renderType(TEXTURE), false, stack.hasFoil());
        model.renderToBuffer(poseStack, consumer, packedLight, packedOverlay, -1);
        poseStack.popPose();
    }
}