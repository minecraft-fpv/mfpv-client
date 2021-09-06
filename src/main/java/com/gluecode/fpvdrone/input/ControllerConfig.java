package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.util.SettingsLoader;
import org.json.simple.JSONArray;

public class ControllerConfig {
  /* AXIS CHANNEL MAPPINGS (0 - 7) (CH 1 - 8) */
  private static int throttleChannel = 0;
  private static int rollChannel = 1;
  private static int pitchChannel = 2;
  private static int yawChannel = 3;
  private static int angleChannel = 4;
  
  /* BUTTON CHANNEL MAPPINGS (0 - 23) (CH 9 - 32) */
  private static int armChannel = 0;
  private static int activateAngleChannel = 1;
  private static int rightClickChannel = 2;
  
  private static float yawRate = 1.15f;
  private static float yawSuper = 0.67f;
  private static float yawExpo = 0.05f;
  private static float pitchRate = 1.15f;
  private static float pitchSuper = 0.67f;
  private static float pitchExpo = 0.05f;
  private static float rollRate = 1.15f;
  private static float rollSuper = 0.67f;
  private static float rollExpo = 0.05f;
  
  private static boolean invertThrottle = false;
  private static boolean invertRoll = false;
  private static boolean invertPitch = false;
  private static boolean invertYaw = false;
  private static boolean invertAngle = false;
  private static boolean invertArm = false;
  private static boolean invertActivateAngle = false;
  private static boolean invertRightClick = false;
  
  private static boolean isGamepad;
  
  // Stick axis ranges:
  // Elements come in pairs.
  // For example, if each axis has a range from -1 to 1, then the array will
  // look like:
  // [-1, 1, -1, 1, -1, 1, -1]
  private static float[] range;
  
  public static float[] convertToPlain(JSONArray jsonArray) {
    float[] floatArray = new float[jsonArray.size()];
    for (int i = 0; i < jsonArray.size(); i++) {
      if (jsonArray.get(i) instanceof Float) {
        Float el = (Float) jsonArray.get(i);
        floatArray[i] = el;
      } else if (jsonArray.get(i) instanceof Double) {
        Double el = (Double) jsonArray.get(i);
        floatArray[i] = new Float(el);
      } else if (jsonArray.get(i) instanceof Integer) {
        Integer el = (Integer) jsonArray.get(i);
        floatArray[i] = el;
      }
    }
    return floatArray;
  }
  
  public static JSONArray getRange() {
    JSONArray array = new JSONArray();
    for (int i = 0; i < 4; i++) {
      int j = i * 2;
      if (range == null) {
        array.add(-1);
        array.add(1);
        continue;
      }
      if (j <= range.length - 1) {
        array.add(range[j]);
      }
      if (j + 1 <= range.length - 1) {
        array.add(range[j + 1]);
      }
    }
    return array;
  }
  
  public static void setRange(JSONArray array) {
    range = convertToPlain(array);
  }
  
  public static void resetRange() {
    setRange(getDefaultRange());
  }
  
  public static JSONArray getDefaultRange() {
    JSONArray array = new JSONArray();
    for (int i = 0; i < 8; i++) {
      array.add(-1f);
      array.add(1f);
    }
    return array;
  }
  
  public static int getThrottleChannel() {
    return throttleChannel;
  }
  
  public static int getRollChannel() {
    return rollChannel;
  }
  
  public static int getPitchChannel() {
    return pitchChannel;
  }
  
  public static int getYawChannel() {
    return yawChannel;
  }
  
  public static int getAngleChannel() {
    return angleChannel;
  }
  
  public static int getArmChannel() {
    return armChannel;
  }
  
  public static int getActivateAngleChannel() {
    return activateAngleChannel;
  }
  
  public static int getRightClickChannel() {
    return rightClickChannel;
  }
  
  public static float getRollRate() {
    return rollRate;
  }
  
  public static float getRollSuper() {
    return rollSuper;
  }
  
  public static float getRollExpo() {
    return rollExpo;
  }
  
  public static float getPitchRate() {
    return pitchRate;
  }
  
  public static float getPitchSuper() {
    return pitchSuper;
  }
  
  public static float getPitchExpo() {
    return pitchExpo;
  }
  
  public static float getYawRate() {
    return yawRate;
  }
  
  public static float getYawSuper() {
    return yawSuper;
  }
  
  public static float getYawExpo() {
    return yawExpo;
  }
  
  public static boolean getInvertThrottle() {
    return invertThrottle;
  }
  
  public static boolean getInvertRoll() {
    return invertRoll;
  }
  
  public static boolean getInvertPitch() {
    return invertPitch;
  }
  
  public static boolean getInvertYaw() {
    return invertYaw;
  }
  
  public static boolean getInvertAngle() {
    return invertAngle;
  }
  
  public static boolean getInvertArm() {
    return invertArm;
  }
  
  public static boolean getInvertActivateAngle() {
    return invertActivateAngle;
  }
  
  public static boolean getInvertRightClick() {
    return invertRightClick;
  }
  
  public static void setThrottleChannel(int value) {
    if (value < 0 || value > 7) {
      return;
    }
    throttleChannel = value;
  }
  
  public static void setRollChannel(int value) {
    if (value < 0 || value > ControllerReader.getAxisLength() - 1) {
      return;
    }
    rollChannel = value;
  }
  
  public static void setPitchChannel(int value) {
    if (value < 0 || value > ControllerReader.getAxisLength() - 1) {
      return;
    }
    pitchChannel = value;
  }
  
  public static void setYawChannel(int value) {
    if (value < 0 || value > ControllerReader.getAxisLength() - 1) {
      return;
    }
    yawChannel = value;
  }
  
  public static void setAngleChannel(int value) {
    if (value < 0 || value > ControllerReader.getAxisLength() - 1) {
      return;
    }
    angleChannel = value;
  }
  
  public static void setArmChannel(int value) {
    if (value > ControllerReader.getButtonLength() - 1) {
      return;
    }
    if (value >= 0) {
      armChannel = value;
    }
    if (value < 0 && ControllerReader.getAxisLength() + value >= 0) {
      armChannel = value;
    }
  }
  
  public static void setActivateAngleChannel(int value) {
    if (value > ControllerReader.getButtonLength() - 1) {
      return;
    }
    if (value >= 0) {
      activateAngleChannel = value;
    }
    if (value < 0 && ControllerReader.getAxisLength() + value >= 0) {
      activateAngleChannel = value;
    }
  }
  
  public static void setRightClickChannel(int value) {
    if (value > ControllerReader.getButtonLength() - 1) {
      return;
    }
    if (value >= 0) {
      rightClickChannel = value;
    }
    if (value < 0 && ControllerReader.getAxisLength() + value >= 0) {
      rightClickChannel = value;
    }
  }
  
  public static void setRollRate(float value) {
    if (value < 0 || value > 3) {
      return;
    }
    rollRate = value;
  }
  
  public static void setRollSuper(float value) {
    if (value < 0 || value > 1) {
      return;
    }
    rollSuper = value;
  }
  
  public static void setRollExpo(float value) {
    if (value < 0 || value > 1) {
      return;
    }
    rollExpo = value;
  }
  
  public static void setPitchRate(float value) {
    if (value < 0 || value > 3) {
      return;
    }
    pitchRate = value;
  }
  
  public static void setPitchSuper(float value) {
    if (value < 0 || value > 1) {
      return;
    }
    pitchSuper = value;
  }
  
  public static void setPitchExpo(float value) {
    if (value < 0 || value > 1) {
      return;
    }
    pitchExpo = value;
  }
  
  public static void setYawRate(float value) {
    if (value < 0 || value > 3) {
      return;
    }
    yawRate = value;
  }
  
  public static void setYawSuper(float value) {
    if (value < 0 || value > 1) {
      return;
    }
    yawSuper = value;
  }
  
  public static void setYawExpo(float value) {
    if (value < 0 || value > 1) {
      return;
    }
    yawExpo = value;
  }
  
  public static void setInvertThrottle(boolean value) {
    invertThrottle = value;
  }
  
  public static void setInvertRoll(boolean value) {
    invertRoll = value;
  }
  
  public static void setInvertPitch(boolean value) {
    invertPitch = value;
  }
  
  public static void setInvertYaw(boolean value) {
    invertYaw = value;
  }
  
  public static void setInvertAngle(boolean value) {
    invertAngle = value;
  }
  
  public static void setInvertArm(boolean value) {
    invertArm = value;
  }
  
  public static void setInvertActivateAngle(boolean value) {
    invertActivateAngle = value;
  }
  
  public static void setInvertRightClick(boolean value) {
    invertRightClick = value;
  }
  
  public static void resetThrottleChannel() {
    setThrottleChannel(getDefaultThrottleChannel());
    resetInvertThrottle();
  }
  
  public static void resetRollChannel() {
    setRollChannel(getDefaultRollChannel());
    resetInvertRoll();
  }
  
  public static void resetPitchChannel() {
    setPitchChannel(getDefaultPitchChannel());
    resetInvertPitch();
  }
  
  public static void resetYawChannel() {
    setYawChannel(getDefaultYawChannel());
    resetInvertYaw();
  }
  
  public static void resetAngleChannel() {
    setAngleChannel(getDefaultAngleChannel());
    resetInvertAngle();
  }
  
  public static void resetArmChannel() {
    setArmChannel(getDefaultArmChannel());
    resetInvertArm();
  }
  
  public static void resetActivateAngleChannel() {
    setActivateAngleChannel(getDefaultActivateAngleChannel());
    resetInvertActivateAngle();
  }
  
  public static void resetRightClickChannel() {
    setRightClickChannel(getDefaultRightClickChannel());
    resetInvertRightClick();
  }
  
  public static void resetRollRate() {
    setRollRate(getDefaultRollRate());
  }
  
  public static void resetRollSuper() {
    setRollSuper(getDefaultRollSuper());
  }
  
  public static void resetRollExpo() {
    setRollExpo(getDefaultRollExpo());
  }
  
  public static void resetPitchRate() {
    setPitchRate(getDefaultPitchRate());
  }
  
  public static void resetPitchSuper() {
    setPitchSuper(getDefaultPitchSuper());
  }
  
  public static void resetPitchExpo() {
    setPitchExpo(getDefaultPitchExpo());
  }
  
  public static void resetYawRate() {
    setYawRate(getDefaultYawRate());
  }
  
  public static void resetYawSuper() {
    setYawSuper(getDefaultYawSuper());
  }
  
  public static void resetYawExpo() {
    setYawExpo(getDefaultYawExpo());
  }
  
  public static void resetInvertThrottle() {
    setInvertThrottle(getDefaultInvertThrottle());
  }
  
  public static void resetInvertRoll() {
    setInvertRoll(getDefaultInvertRoll());
  }
  
  public static void resetInvertPitch() {
    setInvertPitch(getDefaultInvertPitch());
  }
  
  public static void resetInvertYaw() {
    setInvertYaw(getDefaultInvertYaw());
  }
  
  public static void resetInvertAngle() {
    setInvertAngle(getDefaultInvertAngle());
  }
  
  public static void resetInvertArm() {
    setInvertArm(getDefaultInvertArm());
  }
  
  public static void resetInvertActivateAngle() {
    setInvertActivateAngle(getDefaultInvertActivateAngle());
  }
  
  public static void resetInvertRightClick() {
    setInvertRightClick(getDefaultInvertRightClick());
  }
  
  public static int getDefaultThrottleChannel() {
    if (isGamepad) {
      return 1;
    } else {
      return SettingsLoader.defaultThrottleChannel;
    }
  }
  
  public static int getDefaultRollChannel() {
    if (isGamepad) {
      return 2;
    } else {
      return SettingsLoader.defaultRollChannel;
    }
  }
  
  public static int getDefaultPitchChannel() {
    if (isGamepad) {
      return 3;
    } else {
      return SettingsLoader.defaultPitchChannel;
    }
  }
  
  public static int getDefaultYawChannel() {
    if (isGamepad) {
      return 0;
    } else {
      return SettingsLoader.defaultYawChannel;
    }
  }
  
  public static int getDefaultAngleChannel() {
    if (isGamepad) {
      return 4;
    } else {
      return SettingsLoader.defaultAngleChannel;
    }
  }
  
  public static int getDefaultArmChannel() {
    if (isGamepad) {
      return 10 - ControllerReader.getAxisLength();
    } else {
      return SettingsLoader.defaultArmChannel;
    }
  }
  
  public static int getDefaultActivateAngleChannel() {
    if (isGamepad) {
      return 8 - ControllerReader.getAxisLength();
    } else {
      return SettingsLoader.defaultActivateAngleChannel;
    }
  }
  
  public static int getDefaultRightClickChannel() {
    if (isGamepad) {
      return 11 - ControllerReader.getAxisLength();
    } else {
      return SettingsLoader.defaultRightClickChannel;
    }
  }
  
  public static float getDefaultRollRate() {
    if (isGamepad) {
      return 1.1f;
    } else {
      return SettingsLoader.defaultRollRate;
    }
  }
  
  public static float getDefaultRollSuper() {
    if (isGamepad) {
      return 0.3f;
    } else {
      return SettingsLoader.defaultRollSuper;
    }
  }
  
  public static float getDefaultRollExpo() {
    if (isGamepad) {
      return 0.5f;
    } else {
      return SettingsLoader.defaultRollExpo;
    }
  }
  
  public static float getDefaultPitchRate() {
    if (isGamepad) {
      return 1.1f;
    } else {
      return SettingsLoader.defaultPitchRate;
    }
  }
  
  public static float getDefaultPitchSuper() {
    if (isGamepad) {
      return 0.3f;
    } else {
      return SettingsLoader.defaultPitchSuper;
    }
  }
  
  public static float getDefaultPitchExpo() {
    if (isGamepad) {
      return 0.5f;
    } else {
      return SettingsLoader.defaultPitchExpo;
    }
  }
  
  public static float getDefaultYawRate() {
    if (isGamepad) {
      return 1.0f;
    } else {
      return SettingsLoader.defaultYawRate;
    }
  }
  
  public static float getDefaultYawSuper() {
    if (isGamepad) {
      return 0.3f;
    } else {
      return SettingsLoader.defaultYawSuper;
    }
  }
  
  public static float getDefaultYawExpo() {
    if (isGamepad) {
      return 0.5f;
    } else {
      return SettingsLoader.defaultYawExpo;
    }
  }
  
  public static boolean getDefaultInvertThrottle() {
    if (isGamepad) {
      return true;
    } else {
      return SettingsLoader.defaultInvertThrottle;
    }
  }
  
  public static boolean getDefaultInvertRoll() {
    if (isGamepad) {
      return false;
    } else {
      return SettingsLoader.defaultInvertRoll;
    }
  }
  
  public static boolean getDefaultInvertPitch() {
    if (isGamepad) {
      return true;
    } else {
      return SettingsLoader.defaultInvertPitch;
    }
  }
  
  public static boolean getDefaultInvertYaw() {
    if (isGamepad) {
      return false;
    } else {
      return SettingsLoader.defaultInvertYaw;
    }
  }
  
  public static boolean getDefaultInvertAngle() {
    return false;
  }
  
  public static boolean getDefaultInvertArm() {
    return false;
  }
  
  public static boolean getDefaultInvertActivateAngle() {
    return false;
  }
  
  public static boolean getDefaultInvertRightClick() {
    return false;
  }
  
  public static boolean getIsGamepad() {
    return isGamepad;
  }
  
  public static void setIsGamepad(boolean value) {
    isGamepad = value;
  }
}
