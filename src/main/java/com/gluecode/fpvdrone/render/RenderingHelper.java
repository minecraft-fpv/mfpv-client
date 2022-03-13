package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.render.CameraManager;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.FastMath;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;

public class RenderingHelper {
  /*
  Given a point in world pos, this will return the screen space position
  based on the current perspective, projection, and ortho transformations.

  You use this to render stuff during the GUI rendering phase.
  Given a point in the world, this will tell you where that point is appearing on GUI layer.
  * */
  public static com.jme3.math.Vector2f getScreenSpacePos(
    com.jme3.math.Vector3f worldPos,
    Entity renderViewEntity,
    float partialTicks
  ) {
    MainWindow mainWindow = Minecraft.getInstance().getWindow();
    int scaledWidth = mainWindow.getGuiScaledWidth();
    int scaledHeight = mainWindow.getGuiScaledHeight();

    Vec3 eyePosMC = renderViewEntity.getEyePosition(partialTicks);
    com.jme3.math.Vector3f eyePos = new com.jme3.math.Vector3f(
      (float) eyePosMC.x,
      (float) eyePosMC.y,
      (float) eyePosMC.z
    );
    com.jme3.math.Vector3f diff = worldPos.subtract(eyePos);
    Vector3f diffMC = new Vector3f(diff.x, diff.y, diff.z);
    // diff is the position of the gate relative to the eye position.
    // It must be rotated the same amount as the eye rotation
    ActiveRenderInfo activerenderinfo = Minecraft.getInstance().gameRenderer
      .getMainCamera();
    diffMC.transform(Vector3f.YP.rotationDegrees(activerenderinfo.getYRot() +
                                                 180.0F));
    diffMC.transform(Vector3f.XP.rotationDegrees(activerenderinfo.getXRot()));
    diffMC.transform(Vector3f.ZP.rotationDegrees(ControllerReader.getArm() ? CameraManager.cameraRoll : 0));
    // Now diff is a vector pointing from the camera to the gate in view space.
    // View space just means relative to the camera.
    boolean backwards = diffMC.z() > 0;
    Matrix4f projM = Minecraft.getInstance().gameRenderer.getProjectionMatrix(
      activerenderinfo,
      partialTicks,
      true
    );
    Vector4f d4 = new Vector4f(
      diffMC.x(),
      diffMC.y(),
      diffMC.z(),
      1.0f
    );
    d4.transform(projM);
    // Now diff is in clip space.
    d4.perspectiveDivide();
    // now diff is NDC.
    d4.mul(new Vector3f(scaledWidth / 2, scaledHeight / 2, 1));
    com.jme3.math.Vector2f ssPos = new com.jme3.math.Vector2f(
      d4.x(),
      d4.y()
    );
    if (backwards) {
      // multLocal(1000) ensures the gatePos appear off screen;
      ssPos.multLocal(-1).multLocal(1000);
      //      ssPos.multLocal(-1);
    }

    float averageSize = (mainWindow.getGuiScaledWidth() +
                         mainWindow.getGuiScaledHeight()) / 2f;
    float size = 0.025f * averageSize;
    float padding = size;
    float maxX = (scaledWidth / 2) - padding;
    float minX = -(scaledWidth / 2) + padding;
    float maxY = (scaledHeight / 2) - padding;
    float minY = -(scaledHeight / 2) + padding;
    if (ssPos.x > maxX) {
      float length = new com.jme3.math.Vector2f(ssPos.x, 0).length();
      float ratio = FastMath.abs(maxX) / length;
      ssPos.multLocal(ratio);
    }
    if (ssPos.x < minX) {
      float length = new com.jme3.math.Vector2f(ssPos.x, 0).length();
      float ratio = FastMath.abs(minX) / length;
      ssPos.multLocal(ratio);
    }
    if (ssPos.y > maxY) {
      float length = new com.jme3.math.Vector2f(0, ssPos.y).length();
      float ratio = FastMath.abs(maxY) / length;
      ssPos.multLocal(ratio);
    }
    if (ssPos.y < minY) {
      float length = new com.jme3.math.Vector2f(0, ssPos.y).length();
      float ratio = FastMath.abs(minY) / length;
      ssPos.multLocal(ratio);
    }

    //    Main.LOGGER.info("ssPos: " + ssPos);
    //    Main.LOGGER.info("scaledHeight: " + scaledHeight * 0.5f);

    // apply fisheye
    if (SettingsLoader.currentUseFisheye && ControllerReader.getArm()) {
      float aspect = scaledWidth / scaledHeight;
      float fov = (float) (Minecraft.getInstance().options.fov *
                           FastMath.PI / 180f);

      float xImageMax = scaledHeight * 0.5f;
      float yImageMax = scaledHeight * 0.5f;
      com.jme3.math.Vector2f ssPosCentered = ssPos;
      //      ssPosCentered.x *= aspect;
      float imageMax = (new com.jme3.math.Vector2f(
        xImageMax,
        yImageMax
      )).length();
      float image = ssPosCentered.length();

      float tanAngle = image / imageMax * FastMath.tan(fov * 0.5f);
      float scale = FastMath.atan(tanAngle) / tanAngle;

      float stretch = (fov * 0.5f) / FastMath.tan(fov * 0.5f);

      //    vec2 vTexEquidistant = clamp(texCentered / scale + vec2(0.5, 0.5), vec2(0, 0), vec2(1, 1));
      ssPos = ssPosCentered.multLocal(scale /
                                      stretch); // unintuitive: texcoord needs to be scaled in the opposite direction.


      // clamp again:
      if (ssPos.x > maxX) {
        float length = new com.jme3.math.Vector2f(ssPos.x, 0).length();
        float ratio = FastMath.abs(maxX) / length;
        ssPos.multLocal(ratio);
      }
      if (ssPos.x < minX) {
        float length = new com.jme3.math.Vector2f(ssPos.x, 0).length();
        float ratio = FastMath.abs(minX) / length;
        ssPos.multLocal(ratio);
      }
      if (ssPos.y > maxY) {
        float length = new com.jme3.math.Vector2f(0, ssPos.y).length();
        float ratio = FastMath.abs(maxY) / length;
        ssPos.multLocal(ratio);
      }
      if (ssPos.y < minY) {
        float length = new com.jme3.math.Vector2f(0, ssPos.y).length();
        float ratio = FastMath.abs(minY) / length;
        ssPos.multLocal(ratio);
      }
    }

    return ssPos;
  }

  //  public static Vector2f applyWorldToScreenSpace(Matrix4f matrix, Vector3f worldPos) {
  //    MainWindow mainWindow = Minecraft.getInstance().getWindow();
  //    int scaledWidth = mainWindow.getGuiScaledWidth();
  //    int scaledHeight = mainWindow.getGuiScaledHeight();
  //    boolean backwards = diffMC.getZ() > 0;
  //    Vector4f d4 = new Vector4f(worldPos.getX(), worldPos.getY(), worldPos.getZ(), 1.0f);
  //    d4.transform(matrix);
  //    // Now diff is in clip space.
  //    d4.perspectiveDivide();
  //    // now diff is NDC.
  //    d4.mul(new Vector3f(scaledWidth / 2, scaledHeight / 2, 1));
  //    com.jme3.math.Vector2f ssPos = new com.jme3.math.Vector2f(d4.getX(), d4.getY());
  //    if (backwards) {
  //      // multLocal(1000) ensures the gatePos appear off screen;
  //      ssPos.multLocal(-1).multLocal(1000);
  //    }
  //    return ssPos;
  //  }
}
