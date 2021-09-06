package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.audio.DroneSound;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.network.Network;
import com.gluecode.fpvdrone.physics.IPhysicsCore;
import com.gluecode.fpvdrone.physics.PhysicsState;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.json.simple.JSONArray;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class ControllerReader {
  private static final float rads = (float) (Math.PI / 180);
  private static final float degs = (float) (180 / Math.PI);
  
  protected static int controllerId;
  public static String controllerName;
  protected static FloatBuffer axisBuffer;
  protected static ByteBuffer buttonBuffer;
  private static float[] axis;
  private static byte[] button;
  
  public static long angleChangeTime = System.currentTimeMillis();
  private static float angleAtNotify = 0;
  
  public static boolean rawArm = false;
  private static long lastArm = System.currentTimeMillis();
  
  // ranges is an array twice as big as axis.
  // [min0, max0, min1, max1, min2, max2, min3, max3, min4, max4]
  private static float[] range;
  private static float[] rangeSnapshot;
  private static long lastRangeChangeTime;
  
  private static IntConsumer onChannelChanged;
  private static Consumer<float[]> onRangeFound;
  private static float[] axisSnapshot;
  private static byte[] buttonSnapshot;
  private static final float minDetectionDiff = 0.3f;
  
  protected static float throttle = 0;
  protected static float yaw = 0;
  protected static float pitch = 0;
  protected static float roll = 0;
  protected static float ang = 0;
  protected static boolean arm = false;
  protected static boolean armWaitOneTick = false;
  protected static boolean customAngle = false;
  protected static boolean rightClick = false;
  
  // Save non-armed FOV when arming so that it can be restored when disarming.
  private static float disarmFov = 70;
  
  public static void init() {
    axis = new float[8];
    button = new byte[24];
    
    GLFW.glfwSetJoystickCallback(new JoystickCallback());
  }
  
  public static void joystickCallback(int jid, int event) {
    if (event == GLFW.GLFW_CONNECTED) {
      boolean alreadyConnected = controllerId != -1;
      if (alreadyConnected) {
        alreadyConnected = GLFW.glfwJoystickPresent(controllerId);
      }
      if (!alreadyConnected) {
        // If no controller is set, then automatically set it.
        controllerId = jid;
        controllerName = GLFW.glfwGetJoystickName(jid);
        Main.LOGGER.debug(controllerName + " connected.");
        
        axisBuffer = GLFW.glfwGetJoystickAxes(controllerId);
        buttonBuffer = GLFW.glfwGetJoystickButtons(controllerId);
        if (axis == null || axisBuffer.remaining() != axis.length) {
          axis = new float[axisBuffer.remaining()];
        }
        if (button == null ||
            buttonBuffer.remaining() != button.length) {
          button = new byte[buttonBuffer.remaining()];
        }
      }
    } else if (event == GLFW.GLFW_DISCONNECTED) {
      Main.LOGGER.debug(controllerName + " disconnected.");
      controllerId = -1;
      controllerName = null;
      axisBuffer = null;
      buttonBuffer = null;
    }
  }
  
  private static void connectController() {
    if (controllerId == -1) return;
    if (GLFW.glfwJoystickPresent(controllerId)) {
      controllerName = GLFW.glfwGetJoystickName(controllerId);
      Main.LOGGER.debug(controllerName + " connected.");
      
      axisBuffer = GLFW.glfwGetJoystickAxes(controllerId);
      buttonBuffer = GLFW.glfwGetJoystickButtons(controllerId);
      if (axis == null || axisBuffer.remaining() != axis.length) {
        axis = new float[axisBuffer.remaining()];
      }
      if (button == null || buttonBuffer.remaining() != button.length) {
        button = new byte[buttonBuffer.remaining()];
      }
    }
  }
  
  public static int getAxisLength() {
    if (axis == null) {
      axis = new float[8];
    }
    return axis.length;
  }
  
  public static int getButtonLength() {
    if (button == null) {
      button = new byte[24];
    }
    return button.length;
  }
  
  public static void setControllerId(int value) {
    controllerId = value;
    if (controllerId < 0) {
      controllerId = 0;
    }
    if (controllerId > 15) {
      controllerId = 15;
    }
    connectController();
  }
  
  public static void setControllerIdFromName(String name) {
    if (name.equalsIgnoreCase("")) {
      return;
    }
    for (int id = 0; id < 16; id++) {
      if (GLFW.glfwJoystickPresent(id)) {
        String testName = GLFW.glfwGetJoystickName(id);
        if (name.equalsIgnoreCase(testName)) {
          controllerId = id;
          return;
        }
      }
    }
  }
  
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
  
  public static void startInputListening(IntConsumer onChannelChanged) {
    ControllerReader.onChannelChanged = (int channel) -> {
      ControllerReader.onChannelChanged = null;
      onChannelChanged.accept(channel);
    };
    axisSnapshot = axis.clone();
    buttonSnapshot = button.clone();
  }
  
  public static void startRangeListening(Consumer<float[]> onRangeFound) {
    if (axis == null) return;
    
    ControllerReader.onRangeFound = (float[] range) -> {
      ControllerReader.onRangeFound = null;
      onRangeFound.accept(range);
    };
    rangeSnapshot = new float[axis.length * 2];
    for (int i = 0; i < axis.length; i++) {
      int j = i * 2;
      rangeSnapshot[j] = axis[i];
      rangeSnapshot[j + 1] = axis[i];
    }
    lastRangeChangeTime = System.currentTimeMillis();
  }
  
  private static float safeReadAxisWithRange(int channel) {
    float raw = 0;
    if (axis != null && 0 <= channel && channel <= axis.length - 1) {
      raw = axis[channel];
    }
    
    float min = safeReadRangeMin(channel);
    float max = safeReadRangeMax(channel);
    
    if (max - min < FastMath.ZERO_TOLERANCE) {
      // This channel hasn't been range tested.
      max = 1;
      min = -1;
    }
    
    if (raw < min) {
      return -1;
    }
    if (raw > max) {
      return 1;
    }
    
    float d = raw - min;
    float p = d / (max - min); // [0 - 1]
    return p * 2f - 1f;
  }
  
  public static float safeReadRangeMin(int channel) {
    int j = channel * 2;
    float[] safeRange = range !=
                        null ? range : convertToPlain(ControllerConfig.getDefaultRange());
    if (0 <= j && j <= safeRange.length - 1) {
      return safeRange[j];
    } else {
      return -1;
    }
  }
  
  public static float safeReadRangeMax(int channel) {
    int j = channel * 2;
    float[] safeRange = range !=
                        null ? range : convertToPlain(ControllerConfig.getDefaultRange());
    if (0 <= j + 1 && j + 1 <= safeRange.length - 1) {
      return safeRange[j + 1];
    } else {
      return 1;
    }
  }
  
  public static float safeReadRangeSnapshotMin(int channel) {
    int j = channel * 2;
    float[] safeRange = rangeSnapshot != null ? rangeSnapshot : convertToPlain(
      ControllerConfig.getDefaultRange());
    if (0 <= j && j <= safeRange.length - 1) {
      return safeRange[j];
    } else {
      return -1;
    }
  }
  
  public static float safeReadRangeSnapshotMax(int channel) {
    int j = channel * 2;
    float[] safeRange = rangeSnapshot != null ? rangeSnapshot : convertToPlain(
      ControllerConfig.getDefaultRange());
    if (0 <= j + 1 && j + 1 <= safeRange.length - 1) {
      return safeRange[j + 1];
    } else {
      return 1;
    }
  }
  
  private static float getAxisRaw(int channel) {
    if (axis == null) {
      return 0;
    }
    if (channel < 0) {
      return 0;
    }
    if (channel >= axis.length) {
      return 0;
    }
    return axis[channel];
  }
  
  private static float getAxisSnapshotRaw(int channel) {
    if (axisSnapshot == null) {
      return 0;
    }
    if (channel < 0) {
      return 0;
    }
    if (channel >= axisSnapshot.length) {
      return 0;
    }
    return axisSnapshot[channel];
  }
  
  public static float getAxisDiffOnChannel(int channel) {
    float axisValue = getAxisRaw(channel);
    float axisSnapshotValue = getAxisSnapshotRaw(channel);
    return axisValue - axisSnapshotValue;
  }
  
  private static float[] getAxisDiff() {
    float[] diff = new float[axis.length];
    for (int i = 0; i < diff.length; i++) {
      diff[i] = FastMath.abs(axis[i] - axisSnapshot[i]);
    }
    return diff;
  }
  
  private static boolean[] getButtonDiff() {
    boolean[] diff = new boolean[button.length];
    for (int i = 0; i < diff.length; i++) {
      diff[i] = button[i] != buttonSnapshot[i];
    }
    return diff;
  }
  
  private static int getChangedAxis() {
    float[] axisDiff = getAxisDiff();
    int channel = -1;
    for (int i = 0; i < axisDiff.length; i++) {
      if (axisDiff[i] > minDetectionDiff) {
        channel = i;
      }
    }
    return channel;
  }
  
  private static int getChangedButton() {
    boolean[] buttonDiff = getButtonDiff();
    int channel = -1;
    for (int i = 0; i < buttonDiff.length; i++) {
      if (buttonDiff[i]) {
        channel = i;
      }
    }
    return channel;
  }
  
  private static boolean getButtonValue(int channel, boolean invert) {
    if (channel >= 0) {
      return button[channel] ==
             (invert ? GLFW.GLFW_RELEASE : GLFW.GLFW_PRESS);
    } else {
      channel = getAxisLength() + channel;
      if (channel < axis.length) {
        float axisValue = (invert ? -1f : 1f) * axis[channel];
        return axisValue > (1f / 10f);
      } else {
        return false;
      }
    }
  }
  
  public static String getControllerName(int id) {
    if (id == -1) {
      return "";
    }
    if (GLFW.glfwJoystickPresent(id)) {
      return GLFW.glfwGetJoystickName(id);
    } else {
      return "";
    }
  }
  
  public static int getControllerId() {
    return controllerId;
  }
  
  public static void poll() {
    throttle = 0;
    roll = 0;
    pitch = 0;
    yaw = 0;
    ang = 0;
    
    if (controllerId != -1) {
      if (GLFW.glfwJoystickPresent(controllerId)) {
        try {
          if (range == null) {
            ControllerConfig.setRange(ControllerConfig.getDefaultRange());
          }
          
          axisBuffer = GLFW.glfwGetJoystickAxes(controllerId);
          buttonBuffer = GLFW.glfwGetJoystickButtons(controllerId);
          
          if (axis == null || axisBuffer.remaining() != axis.length) {
            axis = new float[axisBuffer.remaining()];
          }
          
          if (button == null ||
              buttonBuffer.remaining() != button.length) {
            button = new byte[buttonBuffer.remaining()];
          }
          
          axisBuffer.get(axis);
          buttonBuffer.get(button);
          
          if (onChannelChanged != null) {
            // We are listening for the next channel change.
            int changedButton = getChangedButton();
            if (changedButton != -1) {
              // +8 because FpvKeyBindingList expects buttons to be shifted up.
              onChannelChanged.accept(changedButton +
                                      getAxisLength());
            } else {
              int changedAxis = getChangedAxis();
              if (changedAxis != -1) onChannelChanged.accept(
                changedAxis);
            }
          }
          
          if (onRangeFound != null) {
            // We are listening for range changes.
            for (int i = 0; i < axis.length; i++) {
              int j = i * 2;
              if (axis[i] < rangeSnapshot[j]) {
                rangeSnapshot[j] = axis[i];
                lastRangeChangeTime = System.currentTimeMillis();
              }
              if (rangeSnapshot[j + 1] < axis[i]) {
                rangeSnapshot[j + 1] = axis[i];
                lastRangeChangeTime = System.currentTimeMillis();
              }
            }
            
            float diffThottle = safeReadRangeSnapshotMax(ControllerConfig.getThrottleChannel()) -
                                safeReadRangeSnapshotMin(ControllerConfig.getThrottleChannel());
            float diffYaw = safeReadRangeSnapshotMax(ControllerConfig.getYawChannel()) -
                            safeReadRangeSnapshotMin(ControllerConfig.getYawChannel());
            float diffPitch = safeReadRangeSnapshotMax(ControllerConfig.getPitchChannel()) -
                              safeReadRangeSnapshotMin(ControllerConfig.getPitchChannel());
            float diffRoll = safeReadRangeSnapshotMax(ControllerConfig.getRollChannel()) -
                             safeReadRangeSnapshotMin(ControllerConfig.getRollChannel());
            
            if (
              diffThottle >= minDetectionDiff &&
              diffYaw >= minDetectionDiff &&
              diffPitch >= minDetectionDiff &&
              diffRoll >= minDetectionDiff &&
              System.currentTimeMillis() - lastRangeChangeTime > 1500
            ) {
              onRangeFound.accept(rangeSnapshot);
            }
          }
          
          throttle = (ControllerConfig.getInvertThrottle() ? -1f : 1f) * safeReadAxisWithRange(
            ControllerConfig.getThrottleChannel());
          roll = (ControllerConfig.getInvertRoll() ? -1f : 1f) * safeReadAxisWithRange(ControllerConfig.getRollChannel());
          pitch = (ControllerConfig.getInvertPitch() ? -1f : 1f) *
                  safeReadAxisWithRange(ControllerConfig.getPitchChannel());
          yaw = (ControllerConfig.getInvertYaw() ? -1f : 1f) * safeReadAxisWithRange(ControllerConfig.getYawChannel());
          
          if (ControllerConfig.getAngleChannel() < axis.length) {
            ang = (ControllerConfig.getInvertAngle() ? -1f : 1f) * axis[ControllerConfig.getAngleChannel()];
          }
          
          if (FastMath.abs(angleAtNotify - ang) > 0.05) {
            // Angle has changed enough to trigger notification
            angleAtNotify = ang;
            angleChangeTime = System.currentTimeMillis();
          }
          
          boolean prevArm = rawArm;
          boolean prevRightClick = rightClick;
          rawArm = getButtonValue(ControllerConfig.getArmChannel(), ControllerConfig.getInvertArm());
          customAngle = getButtonValue(
            ControllerConfig.getActivateAngleChannel(),
            ControllerConfig.getInvertActivateAngle()
          );
          rightClick = getButtonValue(
            ControllerConfig.getRightClickChannel(),
            ControllerConfig.getInvertRightClick()
          );
          if (prevArm != rawArm) {
            handleArmToggle();
          }
          if (prevRightClick != rightClick) {
            handleRightClickToggle();
          }
          
          //      String a = "";
          //      for (int i = 0; i < button.length; i++) {
          //        a += button[i] + ", ";
          //      }
          //      Main.LOGGER.debug(a);
        } catch (Exception e) {
          Main.LOGGER.error(e);
        }
      }
    }
  }
  
  public static void handleArmToggle() {
    // To figure out what this code does,
    // look at the spec below.
    // Each line in the spec says:
    // current_state -> next_state
    // Every time this function is called,
    // it looks at the current state,
    // then figures out the next state based on
    // the spec's rules.
    
    // "noop" means "no-operation". It means "do nothing".
    
    // momentary:
    // !arm && rawArm -> arm
    // arm && !rawArm too soon -> arm noop
    // arm && rawArm -> arm noop
    // arm && !rawArm waited -> !arm
    
    // switch:
    // !arm && rawArm -> arm
    // arm && !rawArm -> !arm
    
    boolean nextArm = arm;
    
    if (!arm && rawArm) {
      // attempt to arm
      if ((throttle + 1f) / 2f > 0.05f) {
        //        throttleWarning = true;
        //        return;
      } else {
        //        throttleWarning = false;
      }
      long now = System.currentTimeMillis();
      int diff = (int) (now - lastArm);
      lastArm = now;
      if (diff < 200) {
        // too soon. likely a momentary click
        return;
      }
      
      nextArm = true;
      armWaitOneTick = false;
    } else if (arm && !rawArm) {
      // attempt to disarm
      long now = System.currentTimeMillis();
      int diff = (int) (now - lastArm);
      lastArm = now;
      if (diff < 200) {
        // disarm too soon. Likely a momentary click.
        return;
      }
      nextArm = false;
    }
    
    Minecraft minecraft = Minecraft.getInstance();
    
    ClientPlayerEntity player = minecraft.player;
    if (player != null) {
      // Disable normal keyboard movement
      GameSettings gameSettings = minecraft.options;
      ((KeyBindingInterceptor) gameSettings.keyUp).setInterceptionActive(
        nextArm);
      ((KeyBindingInterceptor) gameSettings.keyDown).setInterceptionActive(
        nextArm);
      ((KeyBindingInterceptor) gameSettings.keyLeft).setInterceptionActive(
        nextArm);
      ((KeyBindingInterceptor) gameSettings.keyRight).setInterceptionActive(
        nextArm);
      ((KeyBindingInterceptor) gameSettings.keyJump).setInterceptionActive(
        nextArm);
      
      if (nextArm) {
        Quaternion rot = (new Quaternion()).fromAngles(
          (player.xRot +
           DroneBuild.cameraAngle) *
          rads,
          -player.yRot * rads,
          0
        );
        IPhysicsCore core = PhysicsState.getCore();
        core.setDroneLook(rot.mult(Vector3f.UNIT_Z));
        core.setDroneUp(rot.mult(Vector3f.UNIT_Y));
        core.setDroneLeft(core.getDroneUp().cross(core.getDroneLook()));;
        if (player.level.isClientSide) {
          DistExecutor.runWhenOn(
            Dist.CLIENT,
            () -> () -> {
              if (minecraft.screen == null) {
                minecraft.getSoundManager().play(new DroneSound(player));
              }
            }
          );
          DistExecutor.runWhenOn(
            Dist.CLIENT,
            () -> () -> Network.updateArmState(player)
          );
        }
        
        if (!arm) {
          disarmFov = (float) minecraft.options.fov;
        }
      } else {
        DistExecutor.runWhenOn(
          Dist.CLIENT,
          () -> () -> Network.updateArmState(player)
        );
        player.setPose(Pose.STANDING);
        
        Vector3f motion = PhysicsState.getCore().getVelocity().mult(0.05f); // 1 tick
        player.setDeltaMovement(new Vector3d(motion.x, motion.y, motion.z));
        
        minecraft.options.fov = disarmFov;
        
        //        try {
        //          Main.setPose.invoke(player, Pose.STANDING);
        //        } catch(Exception e) {
        //          Main.LOGGER.debug(e);
        //        }
        //        player.setNoGravity(false);
        //        player.abilities.flying = false;
        //        player.isAirBorne = false;
        //        player.setOnGround(true);
        //        player.onUpdateAbilities();
      }
      
      arm = nextArm;
    }
  }
  
  public static void handleRightClickToggle() {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft != null) {
      KeyBinding kb = minecraft.options.keyUse;
      KeyBinding.set(kb.getKey(), ControllerReader.rightClick);
    }
  }
  
  public static float getThrottle() {
    return throttle;
  }
  
  public static float getYaw() {
    return yaw;
  }
  
  public static float getPitch() {
    return pitch;
  }
  
  public static float getRoll() {
    return roll;
  }
  
  /*
  protected static float ang = 0;
  protected static boolean arm = false;
  protected static float switchlessAngle = 30;
  protected static boolean customAngle = false;
  protected static boolean rightClick = false;
  */
  public static float getAngle() {
    return ang;
  }
  
  public static boolean getArm() {
    return arm;
  }
  
  public static void setArm(boolean value) {
    arm = value;
  }
  
  public static boolean getCustomAngle() {
    return customAngle;
  }
  
  public static boolean getRightClick() {
    return rightClick;
  }
}
