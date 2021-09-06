package com.gluecode.fpvdrone.input;

import org.lwjgl.glfw.GLFWJoystickCallbackI;

public class JoystickCallback implements GLFWJoystickCallbackI {
  public void invoke(int jid, int event) {
    ControllerReader.joystickCallback(jid, event);
  }
}
