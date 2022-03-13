package com.gluecode.fpvdrone.input;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

public class MouseManager {
  public static float mouseX = 0;
  public static float mouseY = 0;
  public static float mouseDiffX = 0;
  public static float mouseDiffY = 0;
  
  private static boolean guiOpen = false;
  
  public static void poll() {
    boolean nextGuiOpen = Minecraft.getInstance().screen != null;
    boolean toggleGui = nextGuiOpen != guiOpen;
    guiOpen = nextGuiOpen;
    
    if (!toggleGui) {
      MouseHandler MouseHandler = Minecraft.getInstance().mouseHandler;
      double nextMouseX = MouseHandler.xpos();
      double nextMouseY = MouseHandler.ypos();
      mouseDiffX = (float) nextMouseX - mouseX;
      mouseDiffY = (float) nextMouseY - mouseY;
      mouseX = (float) nextMouseX;
      mouseY = (float) nextMouseY;
    } else {
      MouseHandler MouseHandler = Minecraft.getInstance().mouseHandler;
      double nextMouseX = MouseHandler.xpos();
      double nextMouseY = MouseHandler.ypos();
      mouseDiffX = 0;
      mouseDiffY = 0;
      mouseX = (float) nextMouseX;
      mouseY = (float) nextMouseY;
    }
  }
  
  public static float yposDiff() {
    if (Minecraft.getInstance().screen != null) {
      return 0;
    }
    // camera angle fix is used to make yaw and roll feel equal no matter the camera angle setting.
    float sensitivity = (float) Minecraft.getInstance().options.sensitivity;
    //    float cameraAngleFix = FastMath.cos(cameraAngle * rads);
    return mouseDiffX * sensitivity * 0.007f;
  }
  
  public static float getMousePitchDiff() {
    if (Minecraft.getInstance().screen != null) {
      return 0;
    }
    float sensitivity = (float) Minecraft.getInstance().options.sensitivity;
    return (ControllerConfig.getInvertPitch() ? -1 : 1) * mouseDiffY * sensitivity * .007f;
  }
  
  public static float getMouseRollDiff() {
    if (Minecraft.getInstance().screen != null) {
      return 0;
    }
    float sensitivity = (float) Minecraft.getInstance().options.sensitivity;
    //    float cameraAngleFix = FastMath.sin(cameraAngle * rads);
    return mouseDiffX * sensitivity * 0.007f;
  }
}
