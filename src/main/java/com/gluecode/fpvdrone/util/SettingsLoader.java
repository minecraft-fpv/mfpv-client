package com.gluecode.fpvdrone.util;

import com.gluecode.fpvdrone.physics.PhysicsConstants;
import com.gluecode.fpvdrone.render.CameraManager;
import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.network.packet.DroneBuildPacket;
import com.gluecode.fpvdrone.network.packet.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

public class SettingsLoader {
  private static File file;
  private static boolean loaded;
  
  public static String defaultControllerName = "";
  
  public static int defaultThrottleChannel = 0;
  public static int defaultRollChannel = 1;
  public static int defaultPitchChannel = 2;
  public static int defaultYawChannel = 3;
  public static int defaultAngleChannel = 4;
  
  public static int defaultArmChannel = 0;
  public static int defaultActivateAngleChannel = 1;
  public static int defaultRightClickChannel = 2;
  
  public static float defaultRollRate = 1.15f;
  public static float defaultRollSuper = 0.67f;
  public static float defaultRollExpo = 0.00f;
  public static float defaultPitchRate = 1.15f;
  public static float defaultPitchSuper = 0.67f;
  public static float defaultPitchExpo = 0.00f;
  public static float defaultYawRate = 1.15f;
  public static float defaultYawSuper = 0.67f;
  public static float defaultYawExpo = 0.00f;
  
  public static boolean defaultInvertThrottle = false;
  public static boolean defaultInvertRoll = false;
  public static boolean defaultInvertPitch = false;
  public static boolean defaultInvertYaw = false;
  
  public static float defaultMassGrams = 600F;
  public static float defaultMotorKv = 2400f * PhysicsConstants.toSIKv;
  public static float defaultMotorWidth = 22f / 1000f;
  public static float defaultMotorHeight = 7f / 1000f;
  public static int defaultBatteryCells = 4;
  public static int defaultBatteryMah = 1300;
  public static float defaultPropDiameter = 5 / PhysicsConstants.inches;
  public static float defaultPropPitch = 4.6f / PhysicsConstants.inches;
  public static int defaultBlades = 3;
  public static float defaultRed = 1;
  public static float defaultGreen = 1;
  public static float defaultBlue = 1;
  public static float defaultFrameWidth = 30f / 1000f;
  public static float defaultFrameHeight = 30f / 1000f;
  public static float defaultFrameLength = 200f / 1000f;
  public static float defaultPropWidth = defaultMotorWidth * 0.5f;
  public static float defaultArmWidth = 15f / 1000f;
  public static float defaultArmThickness = 5f / 1000f;
  public static float defaultAntennaLength = 17f / 1000f;
  public static boolean defaultShowProCam = true;
  public static boolean defaultIsHeroCam = true;
  public static boolean defaultIsToothpick = false;
  
  public static boolean defaultShowCrosshairs = false;
  public static boolean defaultShowBlockOutline = true;
  public static boolean defaultShowStickOverlay = true;
  public static float defaultSwitchlessAngle = 30F;
  public static float defaultFov = 135;
  public static float currentFov = 135;
  public static boolean defaultFlightMode3d = false;
  
  public static boolean defaultUseRealtimePhysics = true;
  public static boolean currentUseRealtimePhysics = true;
  
  public static boolean defaultUseFisheye = true;
  public static boolean currentUseFisheye = false;
  
  public static JSONObject defaultModel = (JSONObject) JSONValue.parse("{}");
  public static String defaultModelName = "5 Inch 4S (Default)";
  public static String defaultWhoop = "Tiny Whoop (Default)";
  
  public static boolean firstTimeSetup = true;
  public static JSONObject models = defaultModel;
  public static String currentModel = defaultModelName;
  
  public static void load() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft == null) return;
    
    String gameDir = minecraft.gameDirectory.getAbsolutePath();
    
    Main.LOGGER.debug(gameDir);
    File configFolder = new File(gameDir, "config");
    
    Main.LOGGER.debug("config: " + configFolder.getAbsolutePath());
    Main.LOGGER.debug("loaded: " + loaded);
    if (!loaded) {
      Properties properties = new Properties();
      file = new File(configFolder, "fpvdrone/settings.properties");
      Main.LOGGER.debug("config: " + file.getAbsolutePath());
      file.getParentFile().mkdirs();
      try {
        if (file.createNewFile()) {
          Main.LOGGER.info(
            "Successfully created controller properties");
        }
        if (file.exists()) {
          properties.load(new FileInputStream(file));
          
          String modelsString = properties.getProperty(
            "models",
            defaultModel.toJSONString()
          );
          try {
            models = (JSONObject) JSONValue.parse(modelsString);
          } catch (Exception e) {
            Main.LOGGER.error(e);
          }
          currentModel = properties.getProperty(
            "currentModel",
            defaultModelName
          );
          
          firstTimeSetup = Boolean.parseBoolean(properties.getProperty(
            "firstTimeSetup",
            "" + true
          ));
          
          try {
            loadModel(currentModel);
            save(); // save to complete migration which modified models without saving.
          } catch (NumberFormatException e) {
            Main.LOGGER.debug(e);
            e.printStackTrace();
          }
        }
        
        loaded = true;
      } catch (IOException e) {
        Main.LOGGER.debug(e);
        e.printStackTrace();
      }
    }
  }
  
  public static void save() {
    Main.LOGGER.debug("loaded: " + loaded);
    if (!loaded) return;
    Properties properties = new Properties();
    
    JSONObject model = new JSONObject();
    // Misc:
    model.put("isGamepad", ControllerConfig.getIsGamepad());
    model.put(
      "controllerName",
      ControllerReader.getControllerName(ControllerReader.getControllerId())
    );
    model.put("showCrosshairs", CameraManager.getShowCrosshairs());
    model.put("showBlockOutline", CameraManager.getShowBlockOutline());
    model.put("showStickOverlay", CameraManager.getShowStickOverlay());
    model.put("switchlessAngle", DroneBuild.getSwitchlessAngle());
    model.put("fov", currentFov);
    model.put("flightMode3d", DroneBuild.getFlightMode3d());
    model.put("useFisheye", currentUseFisheye);
    model.put("useRealtimePhysics", currentUseRealtimePhysics);
    
    // Channel Mapping:
    model.put("range", ControllerConfig.getRange());
    model.put("throttleChannel", ControllerConfig.getThrottleChannel());
    model.put("rollChannel", ControllerConfig.getRollChannel());
    model.put("pitchChannel", ControllerConfig.getPitchChannel());
    model.put("yawChannel", ControllerConfig.getYawChannel());
    model.put("angleChannel", ControllerConfig.getAngleChannel());
    model.put("armChannel", ControllerConfig.getArmChannel());
    model.put(
      "activateAngleChannel",
      ControllerConfig.getActivateAngleChannel()
    );
    model.put("rightClickChannel", ControllerConfig.getRightClickChannel());
    model.put("invertThrottle", ControllerConfig.getInvertThrottle());
    model.put("invertRoll", ControllerConfig.getInvertRoll());
    model.put("invertPitch", ControllerConfig.getInvertPitch());
    model.put("invertYaw", ControllerConfig.getInvertYaw());
    
    // Rates:
    model.put("rollRate", ControllerConfig.getRollRate());
    model.put("rollSuper", ControllerConfig.getRollSuper());
    model.put("rollExpo", ControllerConfig.getRollExpo());
    model.put("pitchRate", ControllerConfig.getPitchRate());
    model.put("pitchSuper", ControllerConfig.getPitchSuper());
    model.put("pitchExpo", ControllerConfig.getPitchExpo());
    model.put("yawRate", ControllerConfig.getYawRate());
    model.put("yawSuper", ControllerConfig.getYawSuper());
    model.put("yawExpo", ControllerConfig.getYawExpo());
    
    // Default model's build cannot be editted.
    if (!isDefaultPreset(currentModel)) {
      // Build:
      model.put("mass", DroneBuild.getDroneMass());
      model.put("motorKv", DroneBuild.getMotorKv());
      model.put("motorWidth", DroneBuild.getMotorWidth());
      model.put("motorHeight", DroneBuild.getMotorHeight());
      model.put("batteryCells", DroneBuild.getBatteryCells());
      model.put("propDiameter", DroneBuild.getPropDiameter());
      model.put("propPitch", DroneBuild.getPropPitch());
      model.put("blades", DroneBuild.getBlades());
      model.put("red", DroneBuild.getRed());
      model.put("green", DroneBuild.getGreen());
      model.put("blue", DroneBuild.getBlue());
      model.put("frameWidth", DroneBuild.getFrameWidth());
      model.put("frameHeight", DroneBuild.getFrameHeight());
      model.put("frameLength", DroneBuild.getFrameLength());
      model.put("batteryMah", DroneBuild.getBatteryMah());
      model.put("propWidth", DroneBuild.getPropWidth());
      model.put("armWidth", DroneBuild.getArmWidth());
      model.put("armThickness", DroneBuild.getArmThickness());
      model.put("antennaLength", DroneBuild.getAntennaLength());
      model.put("showProCam", DroneBuild.getShowProCam());
      model.put("isHeroCam", DroneBuild.getIsHeroCam());
      model.put("isToothpick", DroneBuild.getIsToothpick());
    }
    
    models.put(currentModel, model);
    
    properties.setProperty("models", models.toJSONString());
    properties.setProperty("currentModel", currentModel);
    
    properties.setProperty("firstTimeSetup", "" + firstTimeSetup);
    
    try {
      properties.store(new FileOutputStream(file), "Fpv Settings");
      Main.LOGGER.info("Saved FPV settings.");
      
      ClientPlayerEntity player = Minecraft.getInstance().player;
      if (player != null) {
        UUID uuid = player.getUUID();
        Main.droneRenderers.remove(uuid); // reset self model
        
        // Make all other player rebuild the DroneRenderer:
        // Note that every time the player arms, it also causes all the other
        // players to rebuild the DroneRenderer.
        DroneBuildPacket droneBuildPacket = new DroneBuildPacket(
          uuid, DroneBuild.getSelf()
        );
        PacketHandler.sendToServer(droneBuildPacket);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void resetChannels() {
    ControllerConfig.resetRange();
    ControllerConfig.resetThrottleChannel();
    ControllerConfig.resetRollChannel();
    ControllerConfig.resetPitchChannel();
    ControllerConfig.resetYawChannel();
    ControllerConfig.resetAngleChannel();
    
    ControllerConfig.resetInvertThrottle();
    ControllerConfig.resetInvertRoll();
    ControllerConfig.resetInvertPitch();
    ControllerConfig.resetInvertYaw();
    
    ControllerConfig.resetArmChannel();
    ControllerConfig.resetActivateAngleChannel();
    ControllerConfig.resetRightClickChannel();
    save();
  }
  
  public static void resetRates() {
    ControllerConfig.resetRollRate();
    ControllerConfig.resetRollSuper();
    ControllerConfig.resetRollExpo();
    ControllerConfig.resetPitchRate();
    ControllerConfig.resetPitchSuper();
    ControllerConfig.resetPitchExpo();
    ControllerConfig.resetYawRate();
    ControllerConfig.resetYawSuper();
    ControllerConfig.resetYawExpo();
    save();
  }
  
  public static void resetRadioChannels() {
    ControllerConfig.setIsGamepad(false);
    resetChannels();
  }
  
  public static void resetXboxChannels() {
    ControllerConfig.setIsGamepad(true);
    resetChannels();
  }
  
  public static void resetRadioRates() {
    ControllerConfig.setIsGamepad(false);
    resetRates();
  }
  
  public static void resetXboxRates() {
    ControllerConfig.setIsGamepad(true);
    resetRates();
  }
  
  private static float getFloat(JSONObject o, String key) {
    Object v = o.get(key);
    if (v == null) {
      return 0;
    } else if (v instanceof Float) {
      return (float) v;
    } else if (v instanceof Double) {
      return (float) ((double) v);
    } else if (v instanceof Long) {
      return (float) Math.toIntExact((long) v);
    } else if (v instanceof Integer) {
      return (float) ((int) v);
    }
    return 0;
  }
  
  private static String getString(JSONObject o, String key) {
    Object v = o.get(key);
    if (v == null) {
      return "";
    } else if (v instanceof String) {
      return (String) v;
    }
    return "";
  }
  
  public static void loadModel(String name) {
    currentModel = name;
    JSONObject model = (JSONObject) models.get(name);
    
    // Drone build can only be configured for non-default presets:
    if (name.equals(defaultWhoop)) {
      // Build:
      DroneBuild.setMotorKv(13000 * PhysicsConstants.toSIKv);
      DroneBuild.setMotorWidth(8f / 1000f);
      DroneBuild.setMotorHeight(2.5f / 1000f);
      DroneBuild.setBatteryCells(1);
      DroneBuild.setBatteryMah(450);
      DroneBuild.setPropDiameter(1.5f / PhysicsConstants.inches);
      DroneBuild.setPropPitch(1.5f / PhysicsConstants.inches);
      DroneBuild.setBlades(4);
      DroneBuild.setRed(1);
      DroneBuild.setGreen(1);
      DroneBuild.setBlue(1);
      DroneBuild.setFrameWidth(25 / 1000f);
      DroneBuild.setFrameHeight(25 / 1000f);
      DroneBuild.setFrameLength(25 / 1000f);
      DroneBuild.setPropWidth(8f / 1000f);
      DroneBuild.setArmWidth(5 / 1000f);
      DroneBuild.setArmThickness(5 / 1000f);
      DroneBuild.setAntennaLength(DroneBuild.getDefaultAntennaLength());
      DroneBuild.setShowProCam(false);
      DroneBuild.setIsHeroCam(false);
      DroneBuild.setIsToothpick(true);
      
      // set last:
      DroneBuild.setDroneMass(DroneBuild.getDefaultDroneMass());
    } else if (name.equals(defaultModelName) || model == null) {
      // Build:
      DroneBuild.setMotorKv(DroneBuild.getDefaultMotorKv());
      DroneBuild.setMotorWidth(DroneBuild.getDefaultMotorWidth());
      DroneBuild.setMotorHeight(DroneBuild.getDefaultMotorHeight());
      DroneBuild.setBatteryCells(DroneBuild.getDefaultBatteryCells());
      DroneBuild.setBatteryMah(DroneBuild.getDefaultBatteryMah());
      DroneBuild.setPropDiameter(DroneBuild.getDefaultPropDiameter());
      DroneBuild.setPropPitch(DroneBuild.getDefaultPropPitch());
      DroneBuild.setBlades(DroneBuild.getDefaultBlades());
      DroneBuild.setRed(DroneBuild.getDefaultRed());
      DroneBuild.setGreen(DroneBuild.getDefaultGreen());
      DroneBuild.setBlue(DroneBuild.getDefaultBlue());
      DroneBuild.setFrameWidth(DroneBuild.getDefaultFrameWidth());
      DroneBuild.setFrameHeight(DroneBuild.getDefaultFrameHeight());
      DroneBuild.setFrameLength(DroneBuild.getDefaultFrameLength());
      DroneBuild.setPropWidth(DroneBuild.getDefaultPropWidth());
      DroneBuild.setArmWidth(DroneBuild.getDefaultArmWidth());
      DroneBuild.setArmThickness(DroneBuild.getDefaultArmThickness());
      DroneBuild.setAntennaLength(DroneBuild.getDefaultAntennaLength());
      DroneBuild.setShowProCam(DroneBuild.getDefaultShowProCam());
      DroneBuild.setIsHeroCam(DroneBuild.getDefaultIsHeroCam());
      DroneBuild.setIsToothpick(DroneBuild.getDefaultIsToothpick());
      
      // set last:
      DroneBuild.setDroneMass(DroneBuild.getDefaultDroneMass());
    } else {
      try {
        // Build:
        DroneBuild.setMotorKv(model.containsKey("motorKv") ? getFloat(
          model,
          "motorKv"
        ) : DroneBuild.getDefaultMotorKv());
        DroneBuild.setMotorWidth(model.containsKey("motorWidth") ? getFloat(
          model,
          "motorWidth"
        ) : DroneBuild.getDefaultMotorWidth());
        DroneBuild.setMotorHeight(model.containsKey("motorHeight") ? getFloat(
          model,
          "motorHeight"
        ) : DroneBuild.getDefaultMotorHeight());
        DroneBuild.setBatteryCells(model.containsKey("batteryCells") ? (int) getFloat(
          model,
          "batteryCells"
        ) : DroneBuild.getDefaultBatteryCells());
        DroneBuild.setBatteryMah(model.containsKey("batteryMah") ? (int) getFloat(
          model,
          "batteryMah"
        ) : DroneBuild.getDefaultBatteryMah());
        DroneBuild.setPropDiameter(model.containsKey("propDiameter") ? getFloat(
          model,
          "propDiameter"
        ) : DroneBuild.getDefaultPropDiameter());
        DroneBuild.setPropPitch(model.containsKey("propPitch") ? getFloat(
          model,
          "propPitch"
        ) : DroneBuild.getDefaultPropPitch());
        DroneBuild.setBlades(model.containsKey("blades") ? (int) getFloat(
          model,
          "blades"
        ) : DroneBuild.getDefaultBlades());
        DroneBuild.setRed(model.containsKey("red") ? getFloat(
          model,
          "red"
        ) : DroneBuild.getDefaultRed());
        DroneBuild.setGreen(model.containsKey("green") ? getFloat(
          model,
          "green"
        ) : DroneBuild.getDefaultGreen());
        DroneBuild.setBlue(model.containsKey("blue") ? getFloat(
          model,
          "blue"
        ) : DroneBuild.getDefaultBlue());
        DroneBuild.setFrameWidth(model.containsKey("frameWidth") ? getFloat(
          model,
          "frameWidth"
        ) : DroneBuild.getDefaultFrameWidth());
        DroneBuild.setFrameHeight(model.containsKey("frameHeight") ? getFloat(
          model,
          "frameHeight"
        ) : DroneBuild.getDefaultFrameHeight());
        DroneBuild.setFrameLength(model.containsKey("frameLength") ? getFloat(
          model,
          "frameLength"
        ) : DroneBuild.getDefaultFrameLength());
        DroneBuild.setPropWidth(model.containsKey("propWidth") ? getFloat(
          model,
          "propWidth"
        ) : DroneBuild.getDefaultPropWidth());
        DroneBuild.setArmWidth(model.containsKey("armWidth") ? getFloat(
          model,
          "armWidth"
        ) : DroneBuild.getDefaultArmWidth());
        DroneBuild.setArmThickness(model.containsKey("armThickness") ? getFloat(
          model,
          "armThickness"
        ) : DroneBuild.getDefaultArmThickness());
        DroneBuild.setAntennaLength(model.containsKey("antennaLength") ? getFloat(
          model,
          "antennaLength"
        ) : DroneBuild.getDefaultAntennaLength());
        DroneBuild.setShowProCam(model.containsKey("showProCam") ? (boolean) model
          .get("showProCam") : DroneBuild.getDefaultShowProCam());
        DroneBuild.setIsHeroCam(model.containsKey("isHeroCam") ? (boolean) model
          .get("isHeroCam") : DroneBuild.getDefaultIsHeroCam());
        DroneBuild.setIsToothpick(model.containsKey("isToothpick") ? (boolean) model
          .get("isToothpick") : DroneBuild.getDefaultIsToothpick());
        
        // set last:
        DroneBuild.setDroneMass(model.containsKey("mass") ? getFloat(
          model,
          "mass"
        ) : DroneBuild.getDefaultDroneMass());
      } catch (Exception e) {
        Main.LOGGER.error(e);
        e.printStackTrace();
        models.remove(name);
        loadModel(defaultModelName);
      }
    }
    
    // All builds can have channel mappings and rates configured.
    if (model != null) {
      try {
        ControllerReader.setControllerIdFromName(model.containsKey(
          "controllerName") ? getString(
          model,
          "controllerName"
        ) : defaultControllerName);
        ControllerConfig.setIsGamepad(model.containsKey("isGamepad") ? (boolean) model
          .get("isGamepad") : false);
        CameraManager.setShowCrosshairs(model.containsKey(
          "showCrosshairs") ? (boolean) model.get("showCrosshairs") : CameraManager
          .getDefaultShowCrosshairs());
        CameraManager.setShowBlockOutline(model.containsKey(
          "showBlockOutline") ? (boolean) model.get("showBlockOutline") : CameraManager
          .getDefaultShowBlockOutline());
        CameraManager.setShowStickOverlay(model.containsKey(
          "showStickOverlay") ? (boolean) model.get("showStickOverlay") : CameraManager
          .getDefaultShowStickOverlay());
        DroneBuild.setSwitchlessAngle(model.containsKey(
          "switchlessAngle") ? getFloat(
          model,
          "switchlessAngle"
        ) : DroneBuild.getDefaultSwitchlessAngle());
        currentFov = model.containsKey("fov") ? getFloat(
          model,
          "fov"
        ) : defaultFov;
        DroneBuild.setFlightMode3d(model.containsKey("flightMode3d") ? (boolean) model.get("flightMode3d") : DroneBuild
          .getDefaultFlightMode3d());
        currentUseFisheye = model.containsKey("useFisheye") ? (boolean) model
          .get("useFisheye") : defaultUseFisheye;
//        currentUseRealtimePhysics = model.containsKey(
//          "useRealtimePhysics") ? (boolean) model.get(
//          "useRealtimePhysics") : defaultUseRealtimePhysics;
        
        // Channel Mappings:
        ControllerConfig.setRange(model.containsKey("range") ? (JSONArray) model.get("range") : ControllerConfig.getDefaultRange());
        ControllerConfig.setThrottleChannel(model.containsKey(
          "throttleChannel") ? (int) getFloat(
          model,
          "throttleChannel"
        ) : ControllerConfig.getDefaultThrottleChannel());
        ControllerConfig.setRollChannel(model.containsKey("rollChannel") ? (int) getFloat(
          model,
          "rollChannel"
        ) : ControllerConfig.getDefaultRollChannel());
        ControllerConfig.setPitchChannel(model.containsKey("pitchChannel") ? (int) getFloat(
          model,
          "pitchChannel"
        ) : ControllerConfig.getDefaultPitchChannel());
        ControllerConfig.setYawChannel(model.containsKey("yawChannel") ? (int) getFloat(
          model,
          "yawChannel"
        ) : ControllerConfig.getDefaultYawChannel());
        ControllerConfig.setAngleChannel(model.containsKey("angleChannel") ? (int) getFloat(
          model,
          "angleChannel"
        ) : ControllerConfig.getDefaultAngleChannel());
        
        ControllerConfig.setArmChannel(model.containsKey("armChannel") ? (int) getFloat(
          model,
          "armChannel"
        ) : ControllerConfig.getDefaultArmChannel());
        ControllerConfig.setActivateAngleChannel(model.containsKey(
          "activateAngleChannel") ? (int) getFloat(
          model,
          "activateAngleChannel"
        ) : ControllerConfig.getDefaultActivateAngleChannel());
        ControllerConfig.setRightClickChannel(model.containsKey(
          "rightClickChannel") ? (int) getFloat(
          model,
          "rightClickChannel"
        ) : ControllerConfig.getDefaultRightClickChannel());
        
        ControllerConfig.setInvertThrottle(model.containsKey(
          "invertThrottle") ? (boolean) model.get("invertThrottle") : ControllerConfig
          .getDefaultInvertThrottle());
        ControllerConfig.setInvertRoll(model.containsKey("invertRoll") ? (boolean) model
          .get("invertRoll") : ControllerConfig.getDefaultInvertRoll());
        ControllerConfig.setInvertPitch(model.containsKey("invertPitch") ? (boolean) model
          .get("invertPitch") : ControllerConfig.getDefaultInvertPitch());
        ControllerConfig.setInvertYaw(model.containsKey("invertYaw") ? (boolean) model
          .get("invertYaw") : ControllerConfig.getDefaultInvertYaw());
        
        // Rates:
        ControllerConfig.setRollRate(model.containsKey("rollRate") ? getFloat(
          model,
          "rollRate"
        ) : ControllerConfig.getDefaultRollRate());
        ControllerConfig.setRollSuper(model.containsKey("rollSuper") ? getFloat(
          model,
          "rollSuper"
        ) : ControllerConfig.getDefaultRollSuper());
        ControllerConfig.setRollExpo(model.containsKey("rollExpo") ? getFloat(
          model,
          "rollExpo"
        ) : ControllerConfig.getDefaultRollExpo());
        ControllerConfig.setPitchRate(model.containsKey("pitchRate") ? getFloat(
          model,
          "pitchRate"
        ) : ControllerConfig.getDefaultPitchRate());
        ControllerConfig.setPitchSuper(model.containsKey("pitchSuper") ? getFloat(
          model,
          "pitchSuper"
        ) : ControllerConfig.getDefaultPitchSuper());
        ControllerConfig.setPitchExpo(model.containsKey("pitchExpo") ? getFloat(
          model,
          "pitchExpo"
        ) : ControllerConfig.getDefaultPitchExpo());
        ControllerConfig.setYawRate(model.containsKey("yawRate") ? getFloat(
          model,
          "yawRate"
        ) : ControllerConfig.getDefaultYawRate());
        ControllerConfig.setYawSuper(model.containsKey("yawSuper") ? getFloat(
          model,
          "yawSuper"
        ) : ControllerConfig.getDefaultYawSuper());
        ControllerConfig.setYawExpo(model.containsKey("yawExpo") ? getFloat(
          model,
          "yawExpo"
        ) : ControllerConfig.getDefaultYawExpo());
      } catch (Exception e) {
        Main.LOGGER.error(e);
        e.printStackTrace();
        models.remove(name);
        loadModel(defaultModelName);
      }
    } else {
      ControllerReader.setControllerIdFromName(defaultControllerName);
      ControllerConfig.setIsGamepad(false);
      CameraManager.setShowCrosshairs(CameraManager.getDefaultShowCrosshairs());
      CameraManager.setShowBlockOutline(CameraManager.getDefaultShowBlockOutline());
      CameraManager.setShowStickOverlay(CameraManager.getDefaultShowStickOverlay());
      DroneBuild.setSwitchlessAngle(DroneBuild.getDefaultSwitchlessAngle());
      currentFov = defaultFov;
      DroneBuild.setFlightMode3d(DroneBuild.getDefaultFlightMode3d());
      currentUseFisheye = defaultUseFisheye;
//      currentUseRealtimePhysics = defaultUseRealtimePhysics;
      
      // Channel Mappings:
      ControllerConfig.setThrottleChannel(ControllerConfig.getDefaultThrottleChannel());
      ControllerConfig.setRollChannel(ControllerConfig.getDefaultRollChannel());
      ControllerConfig.setPitchChannel(ControllerConfig.getDefaultPitchChannel());
      ControllerConfig.setYawChannel(ControllerConfig.getDefaultYawChannel());
      ControllerConfig.setAngleChannel(ControllerConfig.getDefaultAngleChannel());
      
      ControllerConfig.setArmChannel(ControllerConfig.getDefaultArmChannel());
      ControllerConfig.setActivateAngleChannel(ControllerConfig.getDefaultActivateAngleChannel());
      ControllerConfig.setRightClickChannel(ControllerConfig.getDefaultRightClickChannel());
      
      ControllerConfig.setInvertThrottle(ControllerConfig.getDefaultInvertThrottle());
      ControllerConfig.setInvertRoll(ControllerConfig.getDefaultInvertRoll());
      ControllerConfig.setInvertPitch(ControllerConfig.getDefaultInvertPitch());
      ControllerConfig.setInvertYaw(ControllerConfig.getDefaultInvertYaw());
      
      // Rates:
      ControllerConfig.setRollRate(ControllerConfig.getDefaultRollRate());
      ControllerConfig.setRollSuper(ControllerConfig.getDefaultRollSuper());
      ControllerConfig.setRollExpo(ControllerConfig.getDefaultRollExpo());
      ControllerConfig.setPitchRate(ControllerConfig.getDefaultPitchRate());
      ControllerConfig.setPitchSuper(ControllerConfig.getDefaultPitchSuper());
      ControllerConfig.setPitchExpo(ControllerConfig.getDefaultPitchExpo());
      ControllerConfig.setYawRate(ControllerConfig.getDefaultYawRate());
      ControllerConfig.setYawSuper(ControllerConfig.getDefaultYawSuper());
      ControllerConfig.setYawExpo(ControllerConfig.getDefaultYawExpo());
    }
  }
  
  public static boolean isDefaultPreset(String name) {
    return name.equals(defaultModelName) || name.equals(defaultWhoop);
  }
}
