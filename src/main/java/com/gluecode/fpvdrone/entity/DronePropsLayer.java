package com.gluecode.fpvdrone.entity;

import com.gluecode.fpvdrone.Main;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class DronePropsLayer<DroneEntity extends LivingEntity> extends LayerRenderer<AbstractClientPlayerEntity, DroneModel<AbstractClientPlayerEntity>> {
  private final DroneModel<AbstractClientPlayerEntity> droneModel;
  
  public DronePropsLayer(
    UUID uuid,
    IEntityRenderer<AbstractClientPlayerEntity, DroneModel<AbstractClientPlayerEntity>> entityRendererIn,
    DroneBuild build
  ) {
    super(entityRendererIn);
    droneModel = new DroneModel<>(
      uuid,
      build,
      true
    ); // make a new drone model with different params
    droneModel.renderer = (DroneRenderer) entityRendererIn;
  }
  
  @Override
  public void render(
    MatrixStack matrixStackIn,
    IRenderTypeBuffer bufferIn,
    int packedLightIn,
    AbstractClientPlayerEntity entitylivingbaseIn,
    float limbSwing,
    float limbSwingAmount,
    float partialTicks,
    float ageInTicks,
    float netHeadYaw,
    float headPitch
  ) {
    if (!entitylivingbaseIn.isInvisible()) {
      this.getParentModel().copyPropertiesTo(this.droneModel);
      this.droneModel.prepareMobModel(
        entitylivingbaseIn,
        limbSwing,
        limbSwingAmount,
        partialTicks
      );
      this.droneModel.setupAnim(
        entitylivingbaseIn,
        limbSwing,
        limbSwingAmount,
        ageInTicks,
        netHeadYaw,
        headPitch
      );
      //            IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucentCull(this.getEntityTexture(entitylivingbaseIn)));
      
      IVertexBuilder ivertexbuilder;
//      if (this.blur) {
//        // What we set here doesn't actually matter because PropModelRenderer will override it.
//        ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucentCull(
//          new ResourceLocation(
//            Main.MOD_ID,
//            "textures/entity/props3.png"
//          )));
//      } else {
        ivertexbuilder = bufferIn.getBuffer(RenderType.entityTranslucentCull(
          new ResourceLocation(
            Main.MOD_ID,
            "textures/entity/drone.png"
          )));
//      }
      
      this.droneModel.renderToBuffer(
        matrixStackIn,
        ivertexbuilder,
        packedLightIn,
        LivingRenderer.getOverlayCoords(entitylivingbaseIn, 0.0F),
        1.0F,
        1.0F,
        1.0F,
        1.0F
      );
    }
  }
}
