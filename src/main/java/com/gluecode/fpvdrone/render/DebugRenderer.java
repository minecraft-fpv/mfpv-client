package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class DebugRenderer {
  
//  @SubscribeEvent
//  public static void renderPhysicsDebug(RenderWorldLastEvent event) {
//    if (!InputHandler.showDebug()) return;
//
//    LapRender.loadCustomShader();
//
//    MatrixStack stack = event.getMatrixStack();
//
//    Tessellator tessellator = Tessellator.getInstance();
//    BufferBuilder buffer = tessellator.getBuilder();
//    BuildModeRenderer.applyLineMode();
//
//    ActiveRenderInfo info = Minecraft.getInstance().gameRenderer.getMainCamera();
//
//    Vector3d eyePosMC = info.getPosition();
//    Vector3f eyePos = new Vector3f(
//      (float) eyePosMC.x,
//      (float) eyePosMC.y,
//      (float) eyePosMC.z
//    );
//
//    stack.pushPose();
//    stack.translate(-eyePos.x, -eyePos.y, -eyePos.z); // go to center
//
//    for (int motorNumber = 0; motorNumber < 4; motorNumber++) {
//      stack.pushPose();
//
//      Vector3f droneWorldCenter = new Vector3f(
//        (float) InputHandler.position.x,
//        (float) InputHandler.position.y,
//        (float) InputHandler.position.z
//      );
//      Vector3f motorObjectPosition = InputHandler.getMotorObjectPosition(
//        motorNumber);
//      Vector3f droneUp = InputHandler.droneUp;
//
//      Vector3f motorWorldPosition = droneWorldCenter.add(motorObjectPosition);
//
//
//      stack.translate(
//        motorWorldPosition.x,
//        motorWorldPosition.y,
//        motorWorldPosition.z
//      );
//
//      Matrix4f matrix = stack.last().pose();
//
//      buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
//      buffer.vertex(matrix, 0, 0, 0)
//        .color(
//          motorNumber == 0 ? 1f : 0,
//          motorNumber == 1 ? 1f : 0,
//          motorNumber == 2 ? 1f : 0,
//          1f
//        )
//        .endVertex();
//      buffer.vertex(
//        matrix,
//        droneUp.x * 0.1f,
//        droneUp.y * 0.1f,
//        droneUp.z * 0.1f
//      )
//        .color(
//          motorNumber == 0 ? 1f : 0,
//          motorNumber == 1 ? 1f : 0,
//          motorNumber == 2 ? 1f : 0,
//          1f
//        )
//        .endVertex();
//      tessellator.end();
//
//      stack.popPose();
//    }
//
//    stack.popPose();
//
//    LapRender.unloadCustomShader();
//
//    BuildModeRenderer.cleanLineMode();
//  }
//
//  public static Vector3f getMotorObjectPosition(int motorNumber) {
//    // Angle direction is opposite of how it is in DroneModel.
//    float armAngle = motorNumber * 1f / 4f * FastMath.PI * 2f;
//    armAngle = armAngle + FastMath.HALF_PI + FastMath.QUARTER_PI;
//    float bladeLength = getPropDiameter() * 0.5f * 0.0254f;
//    float frameWidth = getFrameWidth() * 0.001f;
//    float armLength = getArmLength(bladeLength, frameWidth);
//    float motorPosition = armLength - armWidth / 2f * 0.001f;
//    Quaternion rot = (new Quaternion());
//    rot.lookAt(droneLook, droneUp);
//    Vector3f rotated = rot.mult(new Vector3f(motorPosition, 0, 0));
//    rot.fromAngleAxis(armAngle, droneUp);
//    return rot.mult(rotated);
//  }
}
