package com.gluecode.fpvdrone.entity;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.network.DroneState;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.Quaternion;

import java.util.UUID;

public class DroneModel<T extends AbstractClientPlayerEntity> extends PlayerModel<T> {
  private static final float rads = (float) (Math.PI / 180.0);
  private static final float degs = (float) (180.0 / Math.PI);
  public static float scale = 32;
  public static float bladeThickness = 0.002f;
  
  private boolean propsLayer;
//  private boolean showBlur = false;
  private UUID playerUuid;
  
  private final ModelRenderer frame;
  private final ModelRenderer[] motors;
  private final ModelRenderer splitCam;
  private final ModelRenderer splitCamWindow;
  private ModelRenderer proCam;
  private ModelRenderer proCamWindow;
  private final ModelRenderer txaStem;
  private final ModelRenderer txaTip;
  private final ModelRenderer standoffs;
  private final ModelRenderer battery;
  private PropModelRenderer[] blades;
  
  private float lastAge = 0;
//  private float motorPosition = 0;
  public DroneBuild build;
  public DroneRenderer renderer;
  
  public DroneModel(UUID uuid, DroneBuild build, boolean propsLayer) {
    super(0, false);
    
    this.renderType = (resourceLocation) -> {
      if (propsLayer) {
        // For some reason, even when PropModelRenderer is imitating the RenderType.entityCutoutNoCull,
        // Props will render as black.
        // They need to be renderered using EntityTranslucent
        return RenderType.entityTranslucentCull(resourceLocation);
      } else {
        return RenderType.entityCutoutNoCull(resourceLocation);
      }
    };
    
    this.playerUuid = uuid;
    
//    super((resourceLocation) -> {
//      if (propsLayer) {
//        // For some reason, even when PropModelRenderer is imitating the RenderType.entityCutoutNoCull,
//        // Props will render as black.
//        // They need to be renderered using EntityTranslucent
//        return RenderType.entityTranslucentCull(resourceLocation);
//      } else {
//        return RenderType.entityCutoutNoCull(resourceLocation);
//      }
//    });
    this.propsLayer = propsLayer;
//    this.showBlur = blur;
    this.texWidth = 16;
    this.texHeight = 16;
    
    this.build = build;
    float frameWidth = build.frameWidth;
    float frameHeight = build.frameHeight;
    float frameLength = build.frameLength;
    float motorWidth = build.motorWidth;
    float motorHeight = build.motorHeight;
    float batteryHeight = build.getBatteryHeight();
    float batteryWidth = build.getBatteryWidth();
    float batteryLength = build.getBatteryLength();
    int nBlades = build.nBlades;
    float bladeLength = build.bladeLength;
    float bladeWidth = build.bladeWidth;
    float armWidth = build.armWidth;
    float armThickness = build.armThickness;
    float txaLength = build.txaLength;
    boolean showProCam = build.showProCam;
    boolean isHeroCam = build.isHeroCam;
    boolean isToothpick = build.isToothpick;
    
    float armLength = DroneBuild.getArmLength(bladeLength, frameWidth);
    
    float plateThickness = 0.005f;
    
    float standoffWidth = 0.005f;
    float standoffHeight = frameHeight - plateThickness * 2f;
    float nStandoffs = DroneBuild.getNStandoffs(frameLength);
    
    Main.LOGGER.info("nStandoffs: " + nStandoffs);
    
    boolean bottomBattery = nStandoffs < 8;
    
    if (bottomBattery) {
      showProCam = false;
    }
    
    float splitCamWidth = DroneBuild.getSplitCamSize(frameWidth);
    float splitCamDepth = splitCamWidth / 2f;
    float splitCamTeleWidth = splitCamWidth * 0.5f;
    float splitCamTeleDepth = splitCamDepth * 0.5f;
    
    float proCamWidth = 0.038f;
    float proCamDepth = 0.036f;
    float proCamTeleWidth = 0.030f;
    float proCamTeleDepth = 0.001f;
    
    float heroWindowWidth = 0.030f;
    float heroWindowDepth = 0.008f;
    float heroCamWidth = 0.062f;
    float heroCamHeight = 0.0445f;
    float heroCamDepth = 0.032f - heroWindowDepth;
    
    float stackHeight = 0.015f;
    float stackWidth = DroneBuild.getStackSize(frameLength);
    float stackLength = DroneBuild.getStackSize(frameLength);
    
    float txaWidth = 0.005f;
    float txaHeight = 0.005f;
    
    float txaTipLength = 0.017f;
    float txaTipWidth = 0.013f;
    float txaTipHeight = 0.013f;
    
    frame = new ModelRenderer(this);
    frame.setPos(0, 24, 0);
    // top plate
    frame.texOffs(0, 0).addBox(
      -frameWidth / 2f * scale,
      -plateThickness * scale,
      -frameLength / 2f * scale,
      frameWidth * scale,
      plateThickness * scale,
      frameLength * scale,
      0.0F,
      false
    );
    // bottom plate
    frame.texOffs(0, 0).addBox(
      -frameWidth / 2f * scale,
      -frameHeight * scale,
      -frameLength / 2f * scale,
      frameWidth * scale,
      plateThickness * scale,
      frameLength * scale,
      0.0F,
      false
    );
    
    standoffs = new ModelRenderer(this);
    standoffs.setPos(0, 24, 0);
    
    // front stand-offs
    standoffs.texOffs(0, 0).addBox(
      -(frameWidth / 2f) * scale,
      -(standoffHeight + plateThickness) * scale,
      -(frameLength / 2f) * scale,
      standoffWidth * scale,
      standoffHeight * scale,
      standoffWidth * scale,
      0.0F,
      false
    );
    standoffs.texOffs(0, 0).addBox(
      -(-frameWidth / 2f +
        standoffWidth) * scale,
      -(standoffHeight + plateThickness) * scale,
      -(frameLength / 2f) * scale,
      standoffWidth * scale,
      standoffHeight * scale,
      standoffWidth * scale,
      0.0F,
      false
    );
    // back stand-offs
    standoffs.texOffs(0, 0).addBox(
      -(frameWidth / 2f) * scale,
      -(standoffHeight + plateThickness) * scale,
      (frameLength / 2f - standoffWidth) * scale,
      standoffWidth * scale,
      standoffHeight * scale,
      standoffWidth * scale,
      0.0F,
      false
    );
    standoffs.texOffs(0, 0).addBox(
      -(-frameWidth / 2f +
        standoffWidth) * scale,
      -(standoffHeight + plateThickness) * scale,
      (frameLength / 2f - standoffWidth) * scale,
      standoffWidth * scale,
      standoffHeight * scale,
      standoffWidth * scale,
      0.0F,
      false
    );
    if (nStandoffs > 4) {
      // middle stand-offs
      standoffs.texOffs(0, 0).addBox(
        -(frameWidth / 2f) * scale,
        -(standoffHeight + plateThickness) * scale,
        -(frameLength / 4f) * scale,
        standoffWidth * scale,
        standoffHeight * scale,
        standoffWidth * scale,
        0.0F,
        false
      );
      standoffs.texOffs(0, 0).addBox(
        -(-frameWidth / 2f +
          standoffWidth) * scale,
        -(standoffHeight + plateThickness) * scale,
        -(frameLength / 4f) * scale,
        standoffWidth * scale,
        standoffHeight * scale,
        standoffWidth * scale,
        0.0F,
        false
      );
      standoffs.texOffs(0, 0).addBox(
        -(frameWidth / 2f) * scale,
        -(standoffHeight + plateThickness) * scale,
        (frameLength / 4f - standoffWidth) * scale,
        standoffWidth * scale,
        standoffHeight * scale,
        standoffWidth * scale,
        0.0F,
        false
      );
      standoffs.texOffs(0, 0).addBox(
        -(-frameWidth / 2f +
          standoffWidth) * scale,
        -(standoffHeight + plateThickness) * scale,
        (frameLength / 4f - standoffWidth) * scale,
        standoffWidth * scale,
        standoffHeight * scale,
        standoffWidth * scale,
        0.0F,
        false
      );
    }
    
    // stack
    frame.texOffs(0, 0).addBox(
      -stackWidth / 2f * scale,
      -(stackHeight + armThickness) * scale,
      -stackLength / 2f * scale,
      stackWidth * scale,
      stackHeight * scale,
      stackLength * scale,
      0.0F,
      false
    );
    
    // battery
    battery = new ModelRenderer(this);
    battery.setPos(0, 24, 0);
    if (bottomBattery) {
      battery.texOffs(0, 0).addBox(
        -batteryLength / 2f * scale,
        0,
        -batteryWidth / 2f * scale,
        batteryLength * scale,
        batteryHeight * scale,
        batteryWidth * scale,
        0.0F,
        false
      );
    } else {
      battery.texOffs(0, 0).addBox(
        -batteryWidth / 2f * scale,
        -(batteryHeight + frameHeight) * scale,
        -(batteryLength - frameLength / 4f + standoffWidth) * scale,
        batteryWidth * scale,
        batteryHeight * scale,
        batteryLength * scale,
        0.0F,
        false
      );
    }
    
    // tx antenna
    txaStem = new ModelRenderer(this);
    txaStem.setPos(
      0,
      -(frameHeight - plateThickness) * scale,
      -frameLength / 2f * scale
    );
    frame.addChild(txaStem);
    setRotationAngle(txaStem, 0, 0, 0);
    txaStem.texOffs(0, 0).addBox(
      -txaWidth / 2f * scale,
      -txaHeight * scale,
      -txaLength * scale,
      txaWidth * scale,
      txaHeight * scale,
      txaLength * scale,
      0.0F,
      false
    );
    txaTip = new ModelRenderer(this);
    txaTip.setPos(
      0,
      24 - (frameHeight - plateThickness) * scale,
      -frameLength / 2f * scale
    );
    txaTip.texOffs(0, 0).addBox(
      -txaTipWidth / 2f * scale,
      -(txaTipHeight - txaTipHeight / 2f + txaWidth / 2f) * scale,
      -(txaTipLength + txaLength) * scale,
      txaTipWidth * scale,
      txaTipHeight * scale,
      txaTipLength * scale,
      0.0F,
      false
    );
    
    // split cam
    splitCam = new ModelRenderer(this);
    splitCam.setPos(
      0.0F,
      24 - (frameHeight / 2f) * scale,
      (frameLength / 2f - standoffWidth / 2f) * scale
    );
    splitCam.texOffs(0, 0).addBox(
      -splitCamWidth / 2f * scale,
      -(splitCamWidth - splitCamWidth / 2f) * scale,
      -splitCamDepth / 2f * scale,
      splitCamWidth * scale,
      splitCamWidth * scale,
      splitCamDepth * scale,
      0.0F,
      false
    );
    splitCamWindow = new ModelRenderer(this);
    splitCamWindow.setPos(
      0,
      24 - (frameHeight / 2f) * scale,
      (frameLength / 2f - standoffWidth / 2f) * scale
    );
    splitCamWindow.texOffs(0, 0).addBox(
      -splitCamTeleWidth / 2f * scale,
      -(splitCamTeleWidth / 2f) * scale,
      (splitCamDepth / 2f) * scale,
      splitCamTeleWidth * scale,
      splitCamTeleWidth * scale,
      splitCamTeleDepth * scale,
      0.0F,
      false
    );
    
    // pro cam
    if (showProCam) {
      if (!isHeroCam) {
        float cameraAngle = build.cameraAngle * rads;
        float proCamPivotNudge = FastMath.sin(cameraAngle) *
                                 proCamWidth;
        proCam = new ModelRenderer(this);
        proCam.setPos(
          0.0F,
          24 - (frameHeight) * scale,
          (frameLength / 4f - standoffWidth / 2f + proCamPivotNudge) *
          scale
        );
        proCam.texOffs(0, 0).addBox(
          -proCamWidth / 2f * scale,
          -(proCamWidth) * scale,
          0,
          proCamWidth * scale,
          proCamWidth * scale,
          proCamDepth * scale,
          0.0F,
          false
        );
        proCamWindow = new ModelRenderer(this);
        proCamWindow.setPos(
          0,
          24 - (frameHeight) * scale,
          (frameLength / 4f - standoffWidth / 2f + proCamPivotNudge) *
          scale
        );
        proCamWindow.texOffs(0, 0).addBox(
          -proCamTeleWidth / 2f * scale,
          -(proCamWidth + proCamTeleWidth) / 2f * scale,
          (proCamDepth) * scale,
          proCamTeleWidth * scale,
          proCamTeleWidth * scale,
          proCamTeleDepth * scale,
          0.0F,
          false
        );
      } else {
        float cameraAngle = build.cameraAngle * rads;
        float proCamPivotNudge = FastMath.sin(cameraAngle) *
                                 heroCamHeight;
        proCam = new ModelRenderer(this);
        proCam.setPos(
          0.0F,
          24 - (frameHeight) * scale,
          (frameLength / 4f - standoffWidth / 2f + proCamPivotNudge) *
          scale
        );
        proCam.texOffs(0, 0).addBox(
          -heroCamWidth / 2f * scale,
          -(heroCamHeight) * scale,
          0,
          heroCamWidth * scale,
          heroCamHeight * scale,
          heroCamDepth * scale,
          0.0F,
          false
        );
        proCamWindow = new ModelRenderer(this);
        proCamWindow.setPos(
          0,
          24 - (frameHeight) * scale,
          (frameLength / 4f - standoffWidth / 2f + proCamPivotNudge) *
          scale
        );
        proCamWindow.texOffs(0, 0).addBox(
          -heroCamWidth / 2f * scale,
          -(heroCamHeight) * scale,
          (heroCamDepth) * scale,
          heroWindowWidth * scale,
          heroWindowWidth * scale,
          heroWindowDepth * scale,
          0.0F,
          false
        );
      }
    }
    
//    if (this.showProps && this.showBlur) {
//      blades = new PropModelRenderer[1 * 4];
//    } else if (this.showProps && !this.showBlur) {
      blades = new PropModelRenderer[nBlades * 4];
//    }
    
    
    motors = new ModelRenderer[4];
    for (int i = 0; i < 4; i++) {
      ModelRenderer motor = attachArm(
        i,
        armLength,
        armWidth,
        armThickness,
        motorWidth,
        motorHeight,
        nBlades,
        bladeLength,
        bladeWidth,
        bladeThickness
      );
      motors[i] = motor;
    }
  }
  
  // Returns the motor's ModelRenderer
  public ModelRenderer attachArm(
    int motorNumber,
    float armLength,
    float armWidth,
    float armThickness,
    float motorWidth,
    float motorHeight,
    int nBlades,
    float bladeLength,
    float bladeWidth,
    float bladeThickness
  ) {
    float armAngle = -motorNumber * 1f / 4f * FastMath.PI * 2f;
    
    ModelRenderer arm = new ModelRenderer(this);
    arm.setPos(0, 0, 0);
    frame.addChild(arm);
    setRotationAngle(
      arm,
      0.0F,
      armAngle + FastMath.HALF_PI + FastMath.QUARTER_PI,
      0.0F
    );
    arm.texOffs(0, 0).addBox(
      -armWidth / 2f * scale,
      -armThickness * scale,
      0,
      armWidth * scale,
      armThickness * scale,
      armLength * scale,
      0.0F,
      false
    );
    
    float motorR = 0.75f;
    float motorG = 0.75f;
    float motorB = 0.75f;
    
    ColorModelRenderer motor = new ColorModelRenderer(
      this,
      motorR,
      motorG,
      motorB,
      1f
    );
    motor.setPos(
      0.0F,
      -armThickness * scale,
      (armLength - armWidth / 2f) * scale
    );
    arm.addChild(motor);
    setRotationAngle(motor, 0.0F, 0, 0.0F);
    motor.texOffs(0, 0).addBox(
      -motorWidth / 2f * scale,
      -motorHeight * scale,
      -motorWidth / 2f * scale,
      motorWidth * scale,
      motorHeight * scale,
      motorWidth * scale,
      0.0F,
      false
    );
    
    float maxPitch = FastMath.atan2(
      bladeLength,
      2f * FastMath.PI * motorWidth
    ); // intentional motorWidth and not motorWidth / 2f
    float maxY = bladeWidth / 2f * FastMath.sin(maxPitch);
    float hubHeight = maxY * 2f;
    float hubWidth = bladeWidth;
    
    float accentR = build.red;
    float accentG = build.green;
    float accentB = build.blue;
    
    ColorModelRenderer propHub = new ColorModelRenderer(
      this,
      accentR,
      accentG,
      accentB,
      1f
    );
    propHub.setPos(0, 0, 0);
    motor.addChild(propHub);
    setRotationAngle(propHub, 0.0F, 0, 0.0F);
    propHub.texOffs(0, 0).addBox(
      -hubWidth / 2f * scale,
      -(motorHeight + hubHeight) * scale,
      -hubWidth / 2f * scale,
      hubWidth * scale,
      hubHeight * scale,
      hubWidth * scale,
      0.0F,
      false
    );
    
//    if (this.showProps && this.showBlur) {
//      // Only render once because all blades have been baked into the texture.
//      for (int i = 0; i < 1; i++) {
//        float bladeAngle = i * 1f / nBlades * FastMath.PI * 2f;
//        PropModelRenderer blade = new PropModelRenderer(
//          this,
//          false,
//          build,
//          motorNumber
//        );
//        blades[motorNumber] = blade;
//
//        blade.setPos(0, -bladeThickness * scale, 0);
//        motor.addChild(blade);
//        setRotationAngle(blade, 0, bladeAngle + FastMath.PI / 4f, 0);
//        blade.texOffs(0, 0).addBox(
//          -bladeLength * scale,
//          (-motorHeight - bladeThickness) * scale,
//          -bladeLength * scale,
//          2f * bladeLength * scale,
//          bladeThickness * scale,
//          2f * bladeLength * scale,
//          0.0F,
//          false
//        );
//      }
    if (this.propsLayer) {
      for (int i = 0; i < nBlades; i++) {
        float bladeAngle = i * 1f / nBlades * FastMath.PI * 2f;
        PropModelRenderer blade = new PropModelRenderer(
          this,
          build,
          motorNumber
        );
        blades[motorNumber * nBlades + i] = blade;
        
        blade.setPos(0, -bladeThickness * scale, 0);
        motor.addChild(blade);
        setRotationAngle(blade, 0, bladeAngle + FastMath.PI / 4f, 0);
        blade.texOffs(0, 0).addBox(
          -bladeWidth / 2f * scale,
          (-motorHeight - bladeThickness) * scale,
          0,
          bladeWidth * scale,
          bladeThickness * scale,
          bladeLength * scale,
          0.0F,
          false
        );
      }
    }
    
    return motor;
  }
  
  @Override
  public void setupAnim(
    T entityIn,
    float limbSwing,
    float limbSwingAmount,
    float ageInTicks,
    float netHeadYaw,
    float headPitch
  ) {
    float age = ageInTicks / 20f;
    float elapsed = age - lastAge;
    lastAge = age;
  
    float[] motorPos = DroneState.getMotorPos(entityIn.getUUID(), elapsed);
    
    // yRot does not follow right-hand rule.
    motors[0].yRot = -motorPos[0] + FastMath.PI;
    motors[1].yRot = -motorPos[1]; // For some reason this doesn't need an offest
    motors[2].yRot = -motorPos[2] + FastMath.PI;
    motors[3].yRot = -motorPos[3] + FastMath.PI;
    //        motors[0].yRot = 0 + FastMath.PI;
    //        motors[1].yRot = 0;
    //        motors[2].yRot = 0 + FastMath.PI;
    //        motors[3].yRot = 0 + FastMath.PI;
    
    if (proCam != null) {
      proCam.xRot = build.cameraAngle * rads;
      proCamWindow.xRot = build.cameraAngle * rads;
    }
    splitCam.xRot = build.cameraAngle * rads;
    splitCamWindow.xRot = build.cameraAngle * rads;
    txaStem.xRot = -(90 - build.cameraAngle) * rads;
    txaTip.xRot = -(90 - build.cameraAngle) * rads;
    
    //        scale = 32;
    //        splitCam.xRot = FastMath.cos(age);
  }
  
  @Override
  public void renderToBuffer(
    MatrixStack matrixStack,
    IVertexBuilder buffer,
    int packedLight,
    int packedOverlay,
    float red,
    float green,
    float blue,
    float alpha
  ) {
//    boolean renderProps = this.renderer.renderProps;
    
    float r = 36f / 255f;
    float g = 36f / 255f;
    float b = 36f / 255f;
    
    float glassR = 10f / 255f;
    float glassG = 10f / 255f;
    float glassB = 10f / 255f;
    
    float accentR = build.red;
    float accentG = build.green;
    float accentB = build.blue;
    
    float batteryR = 0.75f;
    float batteryG = 0.75f;
    float batteryB = 0.75f;
    
    matrixStack.pushPose();
    
    matrixStack.translate(0, 24f / 16f - 24f / scale, 0);
    matrixStack.scale(16f / scale, 16f / scale, 16f / scale);
    
    if (this.propsLayer) {
      if (blades != null) {
        for (int i = 0; i < blades.length; i++) {
          int motorNumber = blades[i].motorNumber;
            if (blades[i] != null) {
              blades[i].alpha = (1 - getBlurAlpha(this.playerUuid, motorNumber));
            }
        }
      }

      // render with alpha 0 to hide frame.
      frame.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        accentR,
        accentG,
        accentB,
        0
      );
    } else {
      if (blades != null) {
        for (int i = 0; i < blades.length; i++) {
          if (blades[i] != null) {
            blades[i].alpha = 0;
          }
        }
      }
      
      frame.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        r,
        g,
        b,
        1f
      );
      splitCam.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        r,
        g,
        b,
        1f
      );
      splitCamWindow.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        glassR,
        glassG,
        glassB,
        1f
      );
      if (proCam != null) {
        proCam.render(
          matrixStack,
          buffer,
          packedLight,
          packedOverlay,
          r,
          g,
          b,
          1f
        );
        proCamWindow.render(
          matrixStack,
          buffer,
          packedLight,
          packedOverlay,
          glassR,
          glassG,
          glassB,
          1f
        );
      }
      txaTip.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        batteryR,
        batteryG,
        batteryB,
        1f
      );
      standoffs.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        accentR,
        accentG,
        accentB,
        1f
      );
      battery.render(
        matrixStack,
        buffer,
        packedLight,
        packedOverlay,
        batteryR,
        batteryG,
        batteryB,
        1f
      );
    }
    
    matrixStack.popPose();
  }
  
  public void setRotationAngle(
    ModelRenderer modelRenderer,
    float x,
    float y,
    float z
  ) {
    modelRenderer.xRot = x;
    modelRenderer.yRot = y;
    modelRenderer.zRot = z;
  }
  
  public static float getBlurAlpha(UUID uuid, int motorNumber) {
    DroneState last = DroneState.lastMap.getOrDefault(uuid, null);
    if (last == null) {
      return 0;
    }
    
    float[] motorVel = last.motorVel;
    
    if (motorNumber >= motorVel.length) {
      return 0;
    }
  
    float vel = FastMath.abs(motorVel[motorNumber]);
    if (vel < 30) {
      return 0;
    } else {
      return 1;
    }
  }
}
