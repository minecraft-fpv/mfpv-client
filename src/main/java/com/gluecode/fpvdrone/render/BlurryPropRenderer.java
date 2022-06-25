package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.entity.DroneModel;
import com.gluecode.fpvdrone.physics.PhysicsState;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

public class BlurryPropRenderer {
  private static final ResourceLocation TEXTURE_2 = new ResourceLocation(
    Main.MOD_ID,
    "textures/entity/props2.png"
  );
  private static final ResourceLocation TEXTURE_3 = new ResourceLocation(
    Main.MOD_ID,
    "textures/entity/props3.png"
  );
  private static final ResourceLocation TEXTURE_4 = new ResourceLocation(
    Main.MOD_ID,
    "textures/entity/props4.png"
  );
  private static final ResourceLocation TEXTURE_5 = new ResourceLocation(
    Main.MOD_ID,
    "textures/entity/props5.png"
  );
  
  public static void render(
    PlayerEntity entity,
    PoseStack matrixStack,
    int packedLightIn,
    int packedOverlayIn,
    DroneBuild build
  ) {
//    if (alpha == 0) return;
    
    // Perform translateRotates according to how it's done in ModelRenderer.render.
    // Each ModelRenderer child performs a translateRotate:
  
    // DroneModel.render
    matrixStack.translate(0, 24f / 16f - 24f / DroneModel.scale, 0);
    matrixStack.scale(16f / DroneModel.scale, 16f / DroneModel.scale, 16f / DroneModel.scale);
    
    // frame
    translateRotate(matrixStack, 0, 24, 0, 0, 0, 0);
    
    float armLength = DroneBuild.getArmLength(
      build.bladeLength,
      build.frameWidth
    );
  
    float[] motorRotateAngleY = new float[4];
    motorRotateAngleY[0] = -PhysicsState.getCore().getMotorPos()[0] + FastMath.PI;
    motorRotateAngleY[1] = -PhysicsState.getCore().getMotorPos()[1]; // For some reason this doesn't need an offest
    motorRotateAngleY[2] = -PhysicsState.getCore().getMotorPos()[2] + FastMath.PI;
    motorRotateAngleY[3] = -PhysicsState.getCore().getMotorPos()[3] + FastMath.PI;
    
    for (int motorNumber = 0; motorNumber < 4; motorNumber++) {
      if (DroneModel.getBlurAlpha(entity.getUUID(), motorNumber) == 0) {
        continue;
      }
      
      matrixStack.pushPose();
      
      float armAngle = -motorNumber * 1f / 4f * FastMath.PI * 2f;
      
      // arm
      translateRotate(
        matrixStack,
        0,
        0,
        0,
        0.0F,
        armAngle + FastMath.HALF_PI + FastMath.QUARTER_PI,
        0.0F
      );
      
      // motor
      translateRotate(
        matrixStack,
        0,
        -build.armThickness * DroneModel.scale,
        (armLength - build.armWidth / 2f) * DroneModel.scale,
        0,
        motorRotateAngleY[motorNumber],
        0
      );
      
      // prop
      translateRotate(
        matrixStack,
        0,
        -DroneModel.bladeThickness * DroneModel.scale,
        0,
        0,
        FastMath.PI / 4f,
        0
      );
      
      renderOne(
        matrixStack,
        packedLightIn,
        packedOverlayIn,
        build
      );
      
      matrixStack.popPose();
    }
  }
  
  public static void renderOne(
    PoseStack matrixStack,
    int packedLightIn,
    int packedOverlayIn,
    DroneBuild build
  ) {
    Matrix4f matrix4f = matrixStack.last().pose();
    Matrix3f matrix3f = matrixStack.last().normal();
    
    float bladeLength = build.bladeLength;
    float motorHeight = build.motorHeight;
    float bladeThickness = DroneModel.bladeThickness;
    float red = build.red;
    float green = build.green;
    float blue = build.blue;
    float alpha = 1;
  
    float posX1 = -bladeLength * DroneModel.scale;
    float posY1 = (-motorHeight - bladeThickness) * DroneModel.scale;
    float posZ1 = -bladeLength * DroneModel.scale;
    float posX2 = posX1 + 2f * bladeLength * DroneModel.scale;
    float posY2 = posY1 + bladeThickness * DroneModel.scale;
    float posZ2 = posZ1 + 2f * bladeLength * DroneModel.scale;
  
    float x = posX1;
    float z = posZ1;
    float f = posX2;
    float f1 = posY2;
    float f2 = posZ2;
    //    float x = -bladeLength * 10f;
    //    float z = -bladeLength * 10f;
    //    float f = bladeLength * 10f;
    //    float f1 = 0;
    //    float f2 = bladeLength * 10f;
    
    float u1 = 1;
    float v1 = 1;
    float u2 = 0;
    float v2 = 1;
    float u3 = 0;
    float v3 = 0;
    float u4 = 1;
    float v4 = 0;
    
        Vector4f p1 = new Vector4f(f / 16f, f1 / 16f, z / 16f, 1.0F);
        Vector4f p2 = new Vector4f(x / 16f, f1 / 16f, z / 16f, 1.0F);
        Vector4f p3 = new Vector4f(x / 16f, f1 / 16f, f2 / 16f, 1.0F);
        Vector4f p4 = new Vector4f(f / 16f, f1 / 16f, f2 / 16f, 1.0F);
//    Vector4f p1 = new Vector4f(f, f1, z, 1.0F);
//    Vector4f p2 = new Vector4f(x, f1, z, 1.0F);
//    Vector4f p3 = new Vector4f(x, f1, f2, 1.0F);
//    Vector4f p4 = new Vector4f(f, f1, f2, 1.0F);
    //    Vector4f p1 = new Vector4f(1, 0, -1f, 1.0F);
    //    Vector4f p2 = new Vector4f(0, 0, -1f, 1.0F);
    //    Vector4f p3 = new Vector4f(0, 1, -1f, 1.0F);
    //    Vector4f p4 = new Vector4f(1, 1, -1f, 1.0F);
    
    //    Vector4f p1 = new Vector4f(f, z, -1f, 1.0F);
    //    Vector4f p2 = new Vector4f(x, z, -1f, 1.0F);
    //    Vector4f p3 = new Vector4f(x, f2, -1f, 1.0F);
    //    Vector4f p4 = new Vector4f(f, f2, -1f, 1.0F);
    
    
    p1.transform(matrix4f);
    p2.transform(matrix4f);
    p3.transform(matrix4f);
    p4.transform(matrix4f);
    
    //    Main.LOGGER.info("p1: " + p1);
    
    // for some reason normals are defined backwards. This is actually going up.
    Vector3f vector3f = Direction.DOWN.step();
    vector3f.transform(matrix3f);
    float n = vector3f.x();
    float n1 = vector3f.y();
    float n2 = vector3f.z();
    
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    
    buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.NEW_ENTITY);
    buffer.vertex(
      p4.x(),
      p4.y(),
      p4.z(),
      red,
      green,
      blue,
      alpha,
      u4,
      v4,
      packedOverlayIn,
      packedLightIn,
      n,
      n1,
      n2
    );
    buffer.vertex(
      p3.x(),
      p3.y(),
      p3.z(),
      red,
      green,
      blue,
      alpha,
      u3,
      v3,
      packedOverlayIn,
      packedLightIn,
      n,
      n1,
      n2
    );
    buffer.vertex(
      p2.x(),
      p2.y(),
      p2.z(),
      red,
      green,
      blue,
      alpha,
      u2,
      v2,
      packedOverlayIn,
      packedLightIn,
      n,
      n1,
      n2
    );
    buffer.vertex(
      p1.x(),
      p1.y(),
      p1.z(),
      red,
      green,
      blue,
      alpha,
      u1,
      v1,
      packedOverlayIn,
      packedLightIn,
      n,
      n1,
      n2
    );
    
    // Other side:
    buffer.vertex(
      p1.x(),
      p1.y(),
      p1.z(),
      red,
      green,
      blue,
      alpha,
      u1,
      v1,
      packedOverlayIn,
      packedLightIn,
      -n,
      -n1,
      -n2
    );
    buffer.vertex(
      p2.x(),
      p2.y(),
      p2.z(),
      red,
      green,
      blue,
      alpha,
      u2,
      v2,
      packedOverlayIn,
      packedLightIn,
      -n,
      -n1,
      -n2
    );
    buffer.vertex(
      p3.x(),
      p3.y(),
      p3.z(),
      red,
      green,
      blue,
      alpha,
      u3,
      v3,
      packedOverlayIn,
      packedLightIn,
      -n,
      -n1,
      -n2
    );
    buffer.vertex(
      p4.x(),
      p4.y(),
      p4.z(),
      red,
      green,
      blue,
      alpha,
      u4,
      v4,
      packedOverlayIn,
      packedLightIn,
      -n,
      -n1,
      -n2
    );
    
    int nBlades = build.nBlades;
    if (nBlades == 2) {
      Minecraft
        .getInstance()
        .getEntityRenderDispatcher().textureManager.bind(TEXTURE_2);
    } else if (nBlades == 3) {
      Minecraft
        .getInstance()
        .getEntityRenderDispatcher().textureManager.bind(TEXTURE_3);
    } else if (nBlades == 4) {
      Minecraft
        .getInstance()
        .getEntityRenderDispatcher().textureManager.bind(TEXTURE_4);
    } else if (nBlades == 5) {
      Minecraft
        .getInstance()
        .getEntityRenderDispatcher().textureManager.bind(TEXTURE_5);
    } else {
      Minecraft
        .getInstance()
        .getEntityRenderDispatcher().textureManager.bind(TEXTURE_3);
    }
    
    boolean originalDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
    
    RenderSystem.enableDepthTest();
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(
      GlStateManager.SourceFactor.SRC_ALPHA,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
      GlStateManager.SourceFactor.ONE,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
    );
    RenderHelper.turnBackOn();
    Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
    
    tessellator.end();
    
    if (!originalDepthTest) {
      RenderSystem.disableDepthTest();
    }
    RenderSystem.disableBlend();
    RenderSystem.defaultBlendFunc();
    RenderHelper.turnOff();
    Minecraft.getInstance().gameRenderer
      .lightTexture()
      .turnOffLightLayer();
  }
  
  public static void translateRotate(
    PoseStack matrixStackIn,
    float rotationPointX,
    float rotationPointY,
    float rotationPointZ,
    float rotateAngleX,
    float rotateAngleY,
    float rotateAngleZ
  ) {
    matrixStackIn.translate(
      (double) (rotationPointX / 16.0F),
      (double) (rotationPointY / 16.0F),
      (double) (rotationPointZ / 16.0F)
    );
    if (rotateAngleZ != 0.0F) {
      matrixStackIn.mulPose(Vector3f.ZP.rotation(rotateAngleZ));
    }
    
    if (rotateAngleY != 0.0F) {
      matrixStackIn.mulPose(Vector3f.YP.rotation(rotateAngleY));
    }
    
    if (rotateAngleX != 0.0F) {
      matrixStackIn.mulPose(Vector3f.XP.rotation(rotateAngleX));
    }
  }
}
