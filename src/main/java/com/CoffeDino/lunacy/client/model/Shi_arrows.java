package com.CoffeDino.lunacy.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

public class Shi_arrows<T extends Entity> extends EntityModel<T> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("lunacy", "shi_arrows"), "main");
    private final ModelPart group;
    private final ModelPart group2;
    private final ModelPart group3;
    private final ModelPart group4;
    private final ModelPart group5;
    private final ModelPart group6;

    public Shi_arrows(ModelPart root) {
        this.group = root.getChild("group");
        this.group2 = root.getChild("group2");
        this.group3 = root.getChild("group3");
        this.group4 = this.group3.getChild("group4");
        this.group5 = this.group3.getChild("group5");
        this.group6 = root.getChild("group6");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition group = partdefinition.addOrReplaceChild("group", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -0.5F, 1.5F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, 0.0F, 1.0F, 10.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.0F, 23.0F, 7.0F));
        PartDefinition group2 = partdefinition.addOrReplaceChild("group2", CubeListBuilder.create(), PartPose.offset(-11.0F, 23.0F, 8.0F));
        PartDefinition cube_r1 = group2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 1).addBox(0.0F, -0.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
        PartDefinition cube_r2 = group2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 1).addBox(-0.2F, 1.0F, 1.0F, 3.0F, 0.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(-0.2F, 1.0F, 0.0F, 0.2F, 0.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 1).addBox(0.0F, 1.0F, 0.0F, 2.8F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        PartDefinition cube_r3 = group2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 1).addBox(0.2F, 0.0F, 0.0F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        PartDefinition cube_r4 = group2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 1).addBox(0.4F, -0.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));
        PartDefinition group3 = partdefinition.addOrReplaceChild("group3", CubeListBuilder.create(), PartPose.offset(-11.0F, 23.0F, 8.0F));
        PartDefinition group4 = group3.addOrReplaceChild("group4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition cube_r5 = group4.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 2).addBox(0.0F, -0.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
        PartDefinition cube_r6 = group4.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 2).addBox(-0.2F, 1.0F, 0.0F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        PartDefinition cube_r7 = group4.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 2).addBox(0.2F, 0.0F, 0.0F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        PartDefinition cube_r8 = group4.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(0, 2).addBox(0.4F, -0.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));
        PartDefinition group5 = group3.addOrReplaceChild("group5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition cube_r9 = group5.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 2).addBox(0.0F, -0.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.3927F, 0.0F));
        PartDefinition cube_r10 = group5.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 2).addBox(-0.2F, 1.0F, 0.0F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        PartDefinition cube_r11 = group5.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 2).addBox(0.2F, 0.0F, 0.0F, 3.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        PartDefinition cube_r12 = group5.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 2).addBox(0.4F, -0.5F, 0.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, -0.3927F, 0.0F));
        PartDefinition group6 = partdefinition.addOrReplaceChild("group6", CubeListBuilder.create().texOffs(0, 3).addBox(-2.0F, -2.4F, 0.5F, 2.0F, 0.8F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.0F, -3.2F, 0.5F, 1.0F, 0.8F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.0F, -1.6F, 0.5F, 1.0F, 0.8F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.0F, -2.0F, 0.5F, 1.0F, 0.0F, 0.8F, new CubeDeformation(0.0F))
                .texOffs(0, 3).addBox(-1.0F, -2.0F, -0.3F, 1.0F, 0.0F, 0.8F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, 25.0F, 8.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        group.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        group2.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        group3.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
        group6.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}