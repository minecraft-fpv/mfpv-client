package com.gluecode.fpvdrone.physics;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.input.MouseManager;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.gluecode.fpvdrone.util.Transforms;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class DefaultPhysicsCore implements IPhysicsCore {
  private static final float rads = (float) (Math.PI / 180);
  private static final float degs = (float) (180 / Math.PI);
  
  private static float[] motorVel = new float[4];
  private static float[] motorPos = new float[4];
  private static Vector3f velocity = new Vector3f(0, 0, 0);
  
  private static Vector3f droneLook = new Vector3f(0, 0, 1);
  private static Vector3f droneUp = new Vector3f(0, 1, 0);
  private static Vector3f droneLeft = new Vector3f(1, 0, 0);
  
  private static boolean initDone = false;
  
  private static void init() {
  
  }
  
  @Override
  public void step(float dt) {
    if (!initDone) {
      init();
      initDone = true;
    }
    staticStep(dt);
  }
  
  @Override
  public float[] getMotorVel() {
    return motorVel;
  }
  
  @Override
  public float[] getMotorPos() {
    return motorPos;
  }
  
  @Override
  public Vector3f getVelocity() {
    return velocity;
  }
  
  @Override
  public void setVelocity(Vector3f value) {
    velocity = value;
  }
  
  @Override
  public Vector3f getDroneLook() {
    return droneLook;
  }
  
  @Override
  public void setDroneLook(Vector3f value) {
    droneLook = value;
  }
  
  @Override
  public Vector3f getDroneUp() {
    return droneUp;
  }
  
  @Override
  public void setDroneUp(Vector3f up) {
    droneUp = up;
  }
  
  @Override
  public Vector3f getDroneLeft() {
    return droneLeft;
  }
  
  @Override
  public void setDroneLeft(Vector3f value) {
    droneLeft = value;
  }
  
  @Override
  public boolean isOverheat() {
    return false;
  }
  
  private static void staticStep(float dt) {
    long tic = System.currentTimeMillis();
  
    rotateDrone(dt);
  
    Vector3f gravity = PhysicsState.getGravity();
    float mass = PhysicsState.getDroneMass();
    boolean flightMode3d = PhysicsState.getFlightMode3d();
    float throttle = PhysicsState.getThrottle();
    int batteryCells = PhysicsState.getBatteryCells();
    float kv = PhysicsState.getKv();
    
    float speed = velocity.length();
    
    // Drag is a force, so it needs to be converted to an acceleration before adding to gravity.
    // After converting drag is in b / t^2 so it needs to be added to acceleration after the acceleration is converted to b / t^2
    // The dragFactor is baked down from the drag coefficient definition, with the assumption that the drone is
    // equivalent to a 10cm angled cube.
    // 1.225 = mass density of air
    // .0314 = area of 10 cm disc
    // .8 = drag coefficient of angled cube
    // assume 1500 kg/m^3 density of drone (more dense than water but less than concrete)
    //        float area = 0.0314F;
    float radius = FastMath.pow(
      3f * mass /
      (PhysicsConstants.batteryDensity * 4f * FastMath.PI),
      1f / 3f
    );
    float area = FastMath.PI * radius * radius;
    float dragCoefficient = 1.05F;
    float dragFactor = (PhysicsConstants.airDensity * area * dragCoefficient) /
                       2F; // kg / m
    
    float throt = 0;
    if (flightMode3d) {
      throt = FastMath.clamp(
        throttle,
        -1f,
        1.0F
      );
    } else {
      throt = FastMath.clamp(
        (throttle + 1F) * 0.5F,
        0,
        1.0F
      );
    }
    
    float sag = Mth.lerp(FastMath.abs(throt), 4.0f, 3.6f);
    float vbat = sag * batteryCells * throt;
    
    float lowestAllowedFrameRate = 15f;
    int maxAllowedMillis = (int) (1000f / lowestAllowedFrameRate);
    
    if (!SettingsLoader.currentUseRealtimePhysics) {
      // assume 3 frames per tick, which is the case for 60 FDroneBuild.
      maxAllowedMillis *= 3;
    }
    
    float maxTimeStep = 1f / 128f;
    int maxIterations = (int) (dt / maxTimeStep) + 10;
    float timeProcessed = 0;
    for (int i = 0; i < maxIterations &&
                    (dt - timeProcessed) > 0.00001f; i++) {
      float remainingTime = dt - timeProcessed;
      float timeStep = Math.min(maxTimeStep, remainingTime);
      
      Vector3f ambientDragForce = velocity.normalize().mult(-1f *
                                                            speed *
                                                            speed *
                                                            dragFactor); // m/s * m/s * kg / m = kg*m/s^2 div mass to get acceleration
      
      // netForce must be calculated before motorVel is mutated.
      Vector3f gravityForce = gravity.mult(mass);
      Vector3f netForce = gravityForce.add(new Vector3f(
        (float) ambientDragForce.x,
        (float) ambientDragForce.y,
        (float) ambientDragForce.z
      ));
      
      /*
      * Q: Why doesn't this physics feel the same as the .jar downloaded
      * from the official curseforge?
      *
      * A: The physics seen here is not the same code
      * running in the curseforge jar.
      * That code is not open source.
      * */
      float strength = kv * 0.1f;
      float efficiency = Mth.lerp(throt, 0.35f, 1f);
      Vector3f thrust = droneUp.mult(throt * efficiency / mass * strength);
      netForce.addLocal(thrust);
      
      Vector3f acceleration = netForce.mult(1f / mass);
      Vector3f nextVelocity = velocity.add(acceleration.mult(
        timeStep)); // in tick units, 1 tick = 1, so accel = velo = dist
      velocity = PhysicsState.collideAndMove(nextVelocity, timeStep);
      
      if (System.currentTimeMillis() - tic > maxAllowedMillis) {
        Main.LOGGER.info("Physics simulation running behind!");
        break;
      }
      
      timeProcessed += timeStep;
    }
  }
  
  private static void rotateDrone(float elapsed) {
    Minecraft minecraft = Minecraft.getInstance();
    
    float deadzone = 0; // todo: implement this as ControllerConfig.getDeadzone();
    
    float mouseYawDiff = MouseManager.yposDiff();
    float mousePitchDiff = MouseManager.getMousePitchDiff();
    float mouseRollDiff = MouseManager.getMouseRollDiff();
    
    // apply deadzone
    float yawDiff = Transforms.bfRate(
      ControllerReader.getYaw(),
      ControllerConfig
        .getYawRate(),
      ControllerConfig.getYawSuper(),
      ControllerConfig.getYawExpo()
    ) *
                    elapsed *
                    rads + mouseYawDiff;
    float pitchDiff = Transforms.bfRate(
      ControllerReader.getPitch(),
      ControllerConfig.getPitchRate(),
      ControllerConfig.getPitchSuper(),
      ControllerConfig.getPitchExpo()
    ) * elapsed * rads + mousePitchDiff;
    float rollDiff = Transforms.bfRate(
      ControllerReader.getRoll(),
      ControllerConfig.getRollRate(),
      ControllerConfig.getRollSuper(),
      ControllerConfig.getRollExpo()
    ) *
                     elapsed *
                     rads + mouseRollDiff;
    if (Math.abs(yawDiff) < deadzone * elapsed * rads) {
      yawDiff = 0;
    }
    if (Math.abs(pitchDiff) < deadzone * elapsed * rads) {
      pitchDiff = 0;
    }
    if (Math.abs(rollDiff) < deadzone * elapsed * rads) {
      rollDiff = 0;
    }
    
    if (minecraft.options.getCameraType() ==
        CameraType.THIRD_PERSON_FRONT) {
      //          yawDiff *= -1;
      pitchDiff *= -1;
      rollDiff *= -1;
    }
    
    // Update drone rotation. Needs to be done on frame render in order to avoid stutter.
    //        Main.LOGGER.debug(yawDiff);
    Quaternion yawRot = (new Quaternion()).fromAngleAxis(
      -yawDiff,
      droneUp
    );
    droneLook = yawRot.mult(droneLook);
    droneLeft = yawRot.mult(droneLeft);
    
    //        Main.LOGGER.debug(pitchDiff);
    Quaternion pitchRot = (new Quaternion()).fromAngleAxis(
      pitchDiff,
      droneLeft
    );
    droneLook = pitchRot.mult(droneLook);
    droneUp = pitchRot.mult(droneUp);
    
    //        Main.LOGGER.debug(rollDiff);
    Quaternion rollRot = (new Quaternion()).fromAngleAxis(
      rollDiff,
      droneLook
    );
    droneUp = rollRot.mult(droneUp);
    droneLeft = rollRot.mult(droneLeft);
    
    droneLook.normalizeLocal();
    droneUp.normalizeLocal();
    droneLeft.normalizeLocal();
    
    // todo: alternately set right or up equal to cross product.
    droneLeft = droneUp.cross(droneLook);
  }
}
