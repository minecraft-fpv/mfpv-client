package com.gluecode.fpvdrone.entity;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.physics.PhysicsConstants;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.FastMath;
import net.minecraft.util.Mth;

public class DroneBuild {
  public static float self_red;
  public static float self_green;
  public static float self_blue;
  public static float self_cameraAngle; // degs
  public static float self_frameWidth;
  public static float self_frameHeight;
  public static float self_frameLength;
  public static float self_motorWidth;
  public static float self_motorHeight;
  public static int self_batteryCells;
  public static int self_batteryMah;
  public static int self_nBlades;
  public static float self_bladeLength;
  public static float self_bladeWidth;
  public static float self_armWidth;
  public static float self_armThickness;
  public static float self_txaLength;
  public static boolean self_showProCam;
  public static boolean self_isHeroCam;
  public static boolean self_isToothpick;
  
  public static float red;
  public static float green;
  public static float blue;
  public static float cameraAngle; // degs
  public static float frameWidth;
  public static float frameHeight;
  public static float frameLength;
  public static float motorWidth;
  public static float motorHeight;
  public static int batteryCells;
  public static int batteryMah;
  public static int nBlades;
  public static float bladeLength;
  public static float bladeWidth;
  private static float propPitch = 4.6f;
  public static float armWidth;
  public static float armThickness;
  public static float txaLength;
  public static boolean showProCam;
  public static boolean isHeroCam;
  public static boolean isToothpick;
  private static float droneMass;
  private static float motorKv;
  private static float propDiameter;
  private static int blades;
  private static float propWidth;
  private static float antennaLength;
  public static boolean flightMode3d;
  private static float switchlessAngle;
  
  public static DroneBuild getSelf() {
    DroneBuild build = new DroneBuild();
    build.red = getRed();
    build.green = getGreen();
    build.blue = getBlue();
    build.cameraAngle = getCameraAngle();
    build.frameWidth = getFrameWidth();
    build.frameHeight = getFrameHeight();
    build.frameLength = getFrameLength();
    build.motorWidth = getMotorWidth();
    build.motorHeight = getMotorHeight();
    build.batteryCells = getBatteryCells();
    build.batteryMah = getBatteryMah();
    build.nBlades = getBlades();
    build.bladeLength = getPropDiameter() * 0.5f;
    build.bladeWidth = getPropWidth();
    build.armWidth = getArmWidth();
    build.armThickness = getArmThickness();
    build.showProCam = getShowProCam();
    build.isHeroCam = getIsHeroCam();
    build.txaLength = getAntennaLength();
    build.isToothpick = getIsToothpick();
    return build;
  }
  
  public DroneBuild() {
    
  }
  
  public DroneBuild(
    float red,
    float green,
    float blue,
    float cameraAngle, // degs
    float frameWidth,
    float frameHeight,
    float frameLength,
    float motorWidth,
    float motorHeight,
    int batteryCells,
    int batteryMah,
    int nBlades,
    float bladeLength,
    float bladeWidth,
    float armWidth,
    float armThickness,
    float txaLength,
    boolean showProCam,
    boolean isHeroCam,
    boolean isToothpick
  ) {
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.cameraAngle = cameraAngle;
    this.frameWidth = frameWidth;
    this.frameHeight = frameHeight;
    this.frameLength = frameLength;
    this.motorWidth = motorWidth;
    this.motorHeight = motorHeight;
    this.batteryCells = batteryCells;
    this.batteryMah = batteryMah;
    this.nBlades = nBlades;
    this.bladeLength = bladeLength;
    this.bladeWidth = bladeWidth;
    this.armWidth = armWidth;
    this.armThickness = armThickness;
    this.showProCam = showProCam;
    this.isHeroCam = isHeroCam;
    this.txaLength = txaLength;
    this.isToothpick = isToothpick;
  }
  
  public float getBatteryWidth() {
    return getBatteryWidth(batteryMah);
  }
  
  public float getBatteryHeight() {
    return getBatteryHeight(batteryCells);
  }
  
  public float getBatteryLength() {
    return getBatteryLength(batteryCells, batteryMah);
  }
  
  public static float getCameraAngle() {
    if (ControllerReader.getCustomAngle()) {
      float i = (ControllerReader.getAngle() + 1f) * 0.5f;
      return Math.round(Mth.lerp(i, 10f / 5f, 80f / 5f)) * 5f;
    } else {
      return getSwitchlessAngle();
    }
  }
  
  public static float getDroneMass() {
    return droneMass;
  }
  
  public static float getMinDroneMass() {
    float motorWeight = 4f * getMotorMass();
    float propWeight = blades * getBladeMass(propDiameter);
    float batteryMass = getBatteryMass(batteryCells, batteryMah);
    float armMass = 4f * getArmMass();
    float plateMass = 2f * getPlateMass();
    float standoffMass = (getNStandoffs(frameLength) *
                          getStandoffMass());
    float stackWeight = getStackWeight();
    float splitCamWeight = getSplitCamWeight();
    
    boolean bottomBattery = getNStandoffs(frameLength) < 8;
    float proCamWeight = 0;
    if (showProCam && !bottomBattery) {
      if (isHeroCam) {
        proCamWeight = 0.126f;
      } else {
        proCamWeight = 0.074f;
      }
    }
    
    return 0.040f +
           motorWeight +
           propWeight +
           batteryMass +
           armMass +
           plateMass +
           standoffMass +
           stackWeight +
           proCamWeight +
           splitCamWeight;
  }
  
  public static float getMaxDroneMass() {
    return 2f * getMinDroneMass();
  }
  
  public static float getMotorKv() {
    return motorKv;
  }
  
  public static float getMotorWidth() {
//    Main.LOGGER.info("getMotorWidth motorWidth: " + motorWidth);
    return motorWidth;
  }
  
  public static float getMotorHeight() {
    return motorHeight;
  }
  
  public static int getBatteryCells() {
    return batteryCells;
  }
  
  public static float getPropDiameter() {
    return propDiameter;
  }
  
  public static float getPropPitch() {
    return propPitch;
  }
  
  public static int getBlades() {
    return blades;
  }
  
  public static float getRed() {
    return red;
  }
  
  public static float getGreen() {
    return green;
  }
  
  public static float getBlue() {
    return blue;
  }
  
  public static float getFrameWidth() {
    return frameWidth;
  }
  
  public static float getFrameHeight() {
    return frameHeight;
  }
  
  public static float getFrameLength() {
    return frameLength;
  }
  
  public static int getBatteryMah() {
    return batteryMah;
  }
  
  public static float getPropWidth() {
    return propWidth; // millimeter
  }
  
  public static float getArmWidth() {
    return armWidth;
  }
  
  public static float getArmThickness() {
    return armThickness;
  }
  
  public static float getAntennaLength() {
    return antennaLength;
  }
  
  public static boolean getShowProCam() {
    return showProCam;
  }
  
  public static boolean getIsHeroCam() {
    return isHeroCam;
  }
  
  public static boolean getIsToothpick() {
    return isToothpick;
  }
  
  public static boolean getFlightMode3d() {
    return flightMode3d;
  }
  
  public static void setDroneMass(float value) {
    droneMass = value;
    float min = getMinDroneMass();
    float max = min * 2;
    if (droneMass < min) {
      droneMass = min;
    }
    if (droneMass > max) {
      droneMass = max;
    }
  }
  
  public static void setMotorKv(float value) {
    motorKv = value;
    if (motorKv < 0) {
      motorKv = 0;
    }
    if (motorKv > 20000 * PhysicsConstants.toSIKv) {
      motorKv = 20000 * PhysicsConstants.toSIKv;
    }
  }
  
  public static void setMotorWidth(float value) {
    motorWidth = value;
    if (motorWidth < 5f / 1000f) {
      motorWidth = 5f / 1000f;
    }
  }
  
  public static void setMotorHeight(float value) {
    motorHeight = value;
    if (motorHeight < 2f / 1000f) {
      motorHeight = 2f / 1000f;
    }
  }
  
  public static void setBatteryCells(int value) {
    batteryCells = value;
    if (batteryCells < 1) {
      batteryCells = 1;
    }
    if (batteryCells > 12) {
      batteryCells = 12;
    }
  }
  
  public static void setPropDiameter(float value) {
    propDiameter = value;
    if (propDiameter < 1.5748f / PhysicsConstants.inches) {
      propDiameter = 1.5748f / PhysicsConstants.inches;
    }
    if (propDiameter > 13 / PhysicsConstants.inches) {
      propDiameter = 13 / PhysicsConstants.inches;
    }
  }
  
  public static void setPropPitch(float value) {
    propPitch = value;
    if (propPitch < 0) {
      propPitch = 0;
    }
    if (propPitch > 20 / PhysicsConstants.inches) {
      propPitch = 20 / PhysicsConstants.inches;
    }
  }
  
  public static void setBlades(int value) {
    blades = value;
    if (blades < 2) {
      blades = 2;
    }
    if (blades > 16) {
      blades = 16;
    }
  }
  
  public static void setRed(float value) {
    red = value;
    if (red < 0) {
      red = 0;
    }
    if (red > 1) {
      red = 1;
    }
  }
  
  public static void setGreen(float value) {
    green = value;
    if (green < 0) {
      green = 0;
    }
    if (green > 1) {
      green = 1;
    }
  }
  
  public static void setBlue(float value) {
    blue = value;
    if (blue < 0) {
      blue = 0;
    }
    if (blue > 1) {
      blue = 1;
    }
  }
  
  public static void setFrameWidth(float value) {
    frameWidth = value;
    if (frameWidth < 25f / 1000f) {
      // smallest possible stack
      frameWidth = 25f / 1000f;
    }
    if (frameWidth > 5000 / 1000f) {
      // the size of a truck
      frameWidth = 5000 / 1000f;
    }
  }
  
  public static void setFrameHeight(float value) {
    frameHeight = value;
    if (frameHeight < 25f / 1000f) {
      frameHeight = 25f / 1000f;
    }
    if (frameHeight > 5000f / 1000f) {
      frameHeight = 5000 / 1000f;
    }
  }
  
  public static void setFrameLength(float value) {
    frameLength = value;
    if (frameLength < 25f / 1000f) {
      frameLength = 25f / 1000f;
    }
    if (frameLength > 5000 / 1000f) {
      frameLength = 5000 / 1000f;
    }
  }
  
  public static void setBatteryMah(int value) {
    batteryMah = value;
    if (batteryMah < 300) {
      batteryMah = 300;
    }
  }
  
  public static void setPropWidth(float value) {
    propWidth = value;
    if (propWidth < 6f / 1000f) {
      propWidth = 6f / 1000f;
    }
    if (propWidth > motorWidth) {
      propWidth = motorWidth;
    }
  }
  
  public static void setArmWidth(float value) {
    armWidth = value;
    if (armWidth < 5f / 1000f) {
      armWidth = 5f / 1000f;
    }
    if (armWidth > 500f / 1000f) {
      armWidth = 500f / 1000f;
    }
  }
  
  public static void setArmThickness(float value) {
    armThickness = value;
    if (armThickness < 5f / 1000f) {
      armThickness = 5f / 1000f;
    }
    if (armThickness > 500f / 1000f) {
      armThickness = 500f / 1000f;
    }
  }
  
  public static void setAntennaLength(float value) {
    antennaLength = value;
    if (antennaLength < 5f / 1000f) {
      antennaLength = 5f / 1000f;
    }
    if (antennaLength > 200f / 1000f) {
      antennaLength = 200f / 1000f;
    }
  }
  
  public static void setShowProCam(boolean value) {
    showProCam = value;
  }
  
  public static void setIsHeroCam(boolean value) {
    isHeroCam = value;
  }
  
  public static void setIsToothpick(boolean value) {
    isToothpick = value;
  }
  
  public static void setFlightMode3d(boolean value) {
    flightMode3d = value;
  }
  
  public static void resetDroneMass() {
    setDroneMass(getDefaultDroneMass());
  }
  
  public static void resetMotorKv() {
    setMotorKv(getDefaultMotorKv());
  }
  
  public static void resetMotorWidth() {
    setMotorWidth(getDefaultMotorWidth());
  }
  
  public static void resetMotorHeight() {
    setMotorHeight(getDefaultMotorHeight());
  }
  
  public static void resetBatteryCells() {
    setBatteryCells(getDefaultBatteryCells());
  }
  
  public static void resetPropDiameter() {
    setPropDiameter(getDefaultPropDiameter());
  }
  
  public static void resetPropPitch() {
    setPropPitch(getDefaultPropPitch());
  }
  
  public static void resetBlades() {
    setBlades(getDefaultBlades());
  }
  
  public static void resetRed() {
    setRed(getDefaultRed());
  }
  
  public static void resetGreen() {
    setGreen(getDefaultGreen());
  }
  
  public static void resetBlue() {
    setBlue(getDefaultBlue());
  }
  
  public static void resetFrameWidth() {
    setFrameWidth(getDefaultFrameWidth());
  }
  
  public static void resetFrameHeight() {
    setFrameHeight(getDefaultFrameHeight());
  }
  
  public static void resetFrameLength() {
    setFrameLength(getDefaultFrameLength());
  }
  
  public static void resetBatteryMah() {
    setBatteryMah(getDefaultBatteryMah());
  }
  
  public static void resetPropWidth() {
    setPropWidth(getDefaultPropWidth());
  }
  
  public static void resetArmWidth() {
    setArmWidth(getDefaultArmWidth());
  }
  
  public static void resetArmThickness() {
    setArmThickness(getDefaultArmThickness());
  }
  
  public static void resetAntennaLength() {
    setAntennaLength(getDefaultAntennaLength());
  }
  
  public static void resetShowProCam() {
    setShowProCam(getDefaultShowProCam());
  }
  
  public static void resetIsHeroCam() {
    setIsHeroCam(getDefaultIsHeroCam());
  }
  
  public static void resetIsToothpick() {
    setIsToothpick(getDefaultIsToothpick());
  }
  
  public static void resetFlightMode3d() {
    setFlightMode3d(getDefaultFlightMode3d());
  }
  
  public static float getDefaultDroneMass() {
    //    if (batteryCells <= 2) {
    //      return getMinMassGrams();
    //    }
    return getMinDroneMass() * (1f / 2f) + getMaxDroneMass() * (1f / 2f);
  }
  
  public static float getDefaultMotorKv() {
    return SettingsLoader.defaultMotorKv;
  }
  
  public static float getDefaultMotorWidth() {
//    Main.LOGGER.info("getDefaultMotorWidth SettingsLoader.defaultMotorWidth: " + SettingsLoader.defaultMotorWidth);
    return SettingsLoader.defaultMotorWidth;
  }
  
  public static float getDefaultMotorHeight() {
    return SettingsLoader.defaultMotorHeight;
  }
  
  public static int getDefaultBatteryCells() {
    return SettingsLoader.defaultBatteryCells;
  }
  
  public static float getDefaultPropDiameter() {
    return SettingsLoader.defaultPropDiameter;
  }
  
  public static float getDefaultPropPitch() {
    return SettingsLoader.defaultPropPitch;
  }
  
  public static int getDefaultBlades() {
    return SettingsLoader.defaultBlades;
  }
  
  public static float getDefaultRed() {
    return SettingsLoader.defaultRed;
  }
  
  public static float getDefaultGreen() {
    return SettingsLoader.defaultGreen;
  }
  
  public static float getDefaultBlue() {
    return SettingsLoader.defaultBlue;
  }
  
  public static float getDefaultFrameWidth() {
    return SettingsLoader.defaultFrameWidth;
  }
  
  public static float getDefaultFrameHeight() {
    return SettingsLoader.defaultFrameHeight;
  }
  
  public static float getDefaultFrameLength() {
    return SettingsLoader.defaultFrameLength;
  }
  
  public static int getDefaultBatteryMah() {
    return SettingsLoader.defaultBatteryMah;
  }
  
  public static float getDefaultPropWidth() {
    return SettingsLoader.defaultPropWidth;
  }
  
  public static float getDefaultArmWidth() {
    return SettingsLoader.defaultArmWidth;
  }
  
  public static float getDefaultArmThickness() {
    return SettingsLoader.defaultArmThickness;
  }
  
  public static float getDefaultAntennaLength() {
    return SettingsLoader.defaultAntennaLength;
  }
  
  public static boolean getDefaultShowProCam() {
    return SettingsLoader.defaultShowProCam;
  }
  
  public static boolean getDefaultIsHeroCam() {
    return SettingsLoader.defaultIsHeroCam;
  }
  
  public static boolean getDefaultIsToothpick() {
    return SettingsLoader.defaultIsToothpick;
  }
  
  public static boolean getDefaultFlightMode3d() {
    return SettingsLoader.defaultFlightMode3d;
  }
  
  /****************************************************************************/
  /* RENDERABLE DERIVED: ******************************************************/
  /****************************************************************************/
  
  public static float getSplitCamSize(float frameWidth) {
    float minSize = 0.019f + 2f * 0.005f;
    if (frameWidth < minSize) {
      return 0.015f;
    } else {
      return 0.020f;
    }
  }
  
  public static float getSplitCamWeight() {
    float frameSize = frameWidth;
    float minSize = 0.019f + 2f * 0.005f;
    if (frameSize < minSize) {
      return 0.0011f;
    } else {
      return 0.009f;
    }
  }
  
  public static float getStackSize(float frameLength) {
    float standoffWidth = 0.005f;
    float minLength = standoffWidth * 4f + 0.030f * 3f;
    if (frameLength < minLength) {
      return 0.024f;
    } else {
      return 0.034f;
    }
  }
  
  public static float getStackWeight() {
    float size = getStackSize(frameLength);
    float volume = size * size * 0.015f;
    return volume * PhysicsConstants.stackDensity;
  }
  
  public static int getNStandoffs(float frameLength) {
    float standoffWidth = 0.005f;
    float minLength = standoffWidth * 4f + 0.020f * 3f;
    float length = frameLength;
    if (length < minLength) {
      return 4;
    } else {
      return 8;
    }
  }
  
  public static float getStandoffMass() {
    float plateThickness = 0.005f;
    float standoffWidth = 0.005f;
    float standoffHeight = frameHeight - plateThickness * 2f;
    float volume = standoffWidth * standoffWidth * standoffHeight;
    return PhysicsConstants.aluminumDensity * volume;
  }
  
  public static float getArmMass() {
    float width = armWidth;
    float height = armThickness;
    float length = getArmLength(
      propDiameter * 0.5f,
      frameWidth
    );
    float volume = width * height * length;
    return PhysicsConstants.carbonFiberDensity * volume;
  }
  
  public static float getPlateMass() {
    float plateThickness = 0.005f;
    float width = frameWidth;
    float height = frameHeight;
    float volume = plateThickness * width * height;
    return PhysicsConstants.carbonFiberDensity * volume;
  }
  
  public static float getArmLength(float bladeLength, float frameWidth) {
    float armAngle = 45f * PhysicsConstants.rads;
    return (bladeLength + frameWidth / 2f + 0.01f) /
           FastMath.sin(FastMath.PI / 2f - armAngle);
  }
  
  public static float getBatteryMass(float batteryCells, float batteryMah) {
    // Curve fitting:
    float mass = -19.69f
                 + 9.12f * batteryCells
                 + 0.02f * batteryMah
                 - 0.04f * batteryCells * batteryCells
                 + 0.02f * batteryCells * batteryMah;
    if (mass < 10) {
      mass = 10f;
    }
    return mass / 1000f;
  }
  
  public static float getBatteryWidth(float batteryMah) {
    // There are 2 kinds of cells: thin or wide
    if (batteryMah <= 650) {
      return 0.017f;
    } else {
      return 0.038f;
    }
  }
  
  public static float getBatteryHeight(int batteryCells) {
    float cellHeight = 0.0065f;
    return cellHeight * batteryCells;
  }
  
  public static float getBatteryLength(int batteryCells, float batteryMah) {
    //    float batteryMah = getBatteryMah();
    //    float batteryCells = getBatteryCells();
    //    float energyDensity = 104712.3966f + 395.4718f * batteryMah - 10125.616f * batteryCells; // watt hour / m^3
    //    if (energyDensity < 250000f) {
    //      energyDensity = 250000f;
    //    }
    //    if (energyDensity > 730000f) {
    //      energyDensity = 730000f;
    //    }
    float energyDensity = 275000f;
    float width = getBatteryWidth(batteryMah);
    float height = getBatteryHeight(batteryCells);
    //    Main.LOGGER.info("InputHandler.batteryMah: " + InputHandler.batteryMah);
    //    Main.LOGGER.info("InputHandler.batteryCells: " + InputHandler.batteryCells);
    //    Main.LOGGER.info("batteryCells: " + batteryCells);
    //    Main.LOGGER.info("batteryMah: " + batteryMah);
    //    Main.LOGGER.info("width: " + width);
    //    Main.LOGGER.info("height: " + height);
    float maxEnergy = 4.2f * batteryCells * batteryMah * 0.001f;
    // energyDensity = maxEnergy / (width * height * length)
    // length = maxEnergy / (width * height * energyDensity)
    return maxEnergy / (width * height * energyDensity);
  }
  
  /****************************************************************************/
  /* NON RENDERABLE DERIVED: **************************************************/
  /****************************************************************************/
  
  public static float getBladeMass(float propDiameter) {
    // For approximation,
    // blade width is half of motor width.
    // blade thickness is 1mm
    float width = getPropWidth();
    float length = propDiameter * 0.5f;
    float height = 0.002f;
    float volume = length * width * height;
    return PhysicsConstants.polycarbonateDensity * volume;
  }
  
  public static float getBellMass() {
    // Bell thickness is 1mm
    float r = (motorWidth) * 0.5f;
    
    float vBell = FastMath.PI *
                  (motorHeight) *
                  (r * r - (r - 0.001f) * (r - 0.001f));
    float mBell = PhysicsConstants.steelDensity * vBell;
    
    float vMag = FastMath.PI *
                 (motorHeight) *
                 ((r - 0.001f) * (r - 0.001f) -
                  (r - 0.002f) * (r - 0.002f));
    float mMag = PhysicsConstants.neodymiumDensity * vMag;
    
    return mBell + mMag;
  }
  
  public static float getMotorMass() {
    float R = (motorWidth) * 0.5f;
    float volume = FastMath.PI * R * R * (motorHeight);
    return PhysicsConstants.motorMassCoefficient * volume;
  }
  
  public static float getSwitchlessAngle() {
    return switchlessAngle;
  }
  
  public static void setSwitchlessAngle(float value) {
    switchlessAngle = value;
  }
  
  public static void resetSwitchlessAngle() {
    setSwitchlessAngle(getDefaultSwitchlessAngle());
  }
  
  public static float getDefaultSwitchlessAngle() {
    return SettingsLoader.defaultSwitchlessAngle;
  }
}
