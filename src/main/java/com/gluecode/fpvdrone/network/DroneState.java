package com.gluecode.fpvdrone.network;

//import com.gluecode.fpvdrone.math.Quaternion;
import com.gluecode.fpvdrone.physics.PhysicsState;
import com.gluecode.fpvdrone.util.Transforms;
import com.google.common.collect.Maps;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
//import net.minecraft.util.math.vector.Quaternion;
//import net.minecraft.util.math.vector.Vector3f;
import com.jme3.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;

import java.util.Map;
import java.util.UUID;

/*
* The purpose of this file is mainly for implementing spectator mode.
* */
public class DroneState {
  public static final float rads = FastMath.PI / 180f;
  public static final float degs = 180f / FastMath.PI;
  
  public static Map<UUID, DroneState> oldMap = Maps.newConcurrentMap();
  public static Map<UUID, DroneState> lastMap = Maps.newConcurrentMap();
  public static Map<UUID, Quaternion> prevPredictedMap = Maps.newConcurrentMap();
  
  // Motor position is calculated by the client, so clients do not agree on motor position.
  // But they agree on motor velocity.
  public static Map<UUID, float[]> motorPosMap = Maps.newConcurrentMap();
  
  public float x;
  public float y;
  public float z;
  public float w;
  public float[] motorVel;
  public long timeSent;
  public long timeReceived;
  
  public DroneState(
    float x,
    float y,
    float z,
    float w,
    float motorVel1,
    float motorVel2,
    float motorVel3,
    float motorVel4,
    long timeSent
  ) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
    this.motorVel = new float[]{
      motorVel1,
      motorVel2,
      motorVel3,
      motorVel4
    };
    this.timeSent = timeSent;
    this.timeReceived = System.currentTimeMillis();
  }
  
  public static void update(
    UUID uuid,
    DroneState nextPlayerOrientation
  ) {
    DroneState last = lastMap.getOrDefault(uuid, null);
    if (last != null) {
      oldMap.put(uuid, last);
    }
    lastMap.put(uuid, nextPlayerOrientation);
  }
  
  public static DroneState getInterpolated(UUID uuid, float partialTicks) {
    DroneState last = lastMap.getOrDefault(uuid, null);
    DroneState old = oldMap.getOrDefault(uuid, null);
    
    if (last == null) {
      Quaternion rot = (new Quaternion());
      return new DroneState(
        rot.getX(),
        rot.getY(),
        rot.getZ(),
        rot.getW(),
        0,
        0,
        0,
        0,
        System.currentTimeMillis()
      );
    }
    
    if (old == null) {
      return last;
    }
  
    PlayerEntity self = Minecraft.getInstance().player;
    if (self != null && self.getUUID().equals(uuid)) {
      // self is always up-to-date, so no interpolation is needed.
      return last;
    }
    
    Quaternion predicted = getPrediction(old, last);
    Quaternion smoothed = getSmoothed(uuid, predicted);
    
    return new DroneState(
      smoothed.getX(),
      smoothed.getY(),
      smoothed.getZ(),
      smoothed.getW(),
      last.motorVel[0],
      last.motorVel[1],
      last.motorVel[2],
      last.motorVel[3],
      last.timeSent
    );
  }
  
  public static Quaternion getPrediction(DroneState old, DroneState last) {
    Quaternion droneLastRot = new Quaternion(
      last.x,
      last.y,
      last.z,
      last.w
    );
  
    Quaternion droneOldRot = new Quaternion(
      old.x,
      old.y,
      old.z,
      old.w
    );
  
    Quaternion diff = droneLastRot.mult(droneOldRot.inverse());
    Vector3f axis = Vector3f.NAN;
    float angle = diff.toAngleAxis(axis);
  
    if (FastMath.abs(angle) > FastMath.PI) {
      // Only accept angle less than 180 deg.
      angle = FastMath.TWO_PI - angle;
      axis.negateLocal();
    }
  
    if (angle < FastMath.ZERO_TOLERANCE) {
      return droneLastRot;
    }
  
    if (last.timeSent - old.timeSent == 0) {
      // avoid div by 0
      return droneLastRot;
    }
  
  
    float dt = (int) (last.timeSent - old.timeSent) / 1000f;
    float vel = angle / dt; // rad / sec
  
    int timeSinceLastReceived = (int) (System.currentTimeMillis() -
                                       last.timeReceived);
    float t = timeSinceLastReceived / 1000f;
    Quaternion futureRot = (new Quaternion()).fromAngleAxis(vel * t, axis);
    Quaternion predicted = futureRot.mult(droneLastRot);
    return predicted;
  }
  
  public static Quaternion getSmoothed(UUID uuid, Quaternion predicted) {
    Quaternion prevPredicted = prevPredictedMap.getOrDefault(uuid, null);
    if (prevPredicted != null) {
      Quaternion diffPredicted = predicted.mult(prevPredicted.inverse());
      Vector3f axisPredicted = Vector3f.NAN;
      float anglePredicted = diffPredicted.toAngleAxis(axisPredicted);
      if (FastMath.abs(anglePredicted) > FastMath.PI) {
        // Only accept angle less than 180 deg.
        anglePredicted = FastMath.TWO_PI - anglePredicted;
        axisPredicted.negateLocal();
      }
      if (!(anglePredicted < FastMath.ZERO_TOLERANCE)) {
        // angle is big enough to warrent smoothing.
        Quaternion smoothingRot = (new Quaternion()).fromAngleAxis(anglePredicted * 0.2f, axisPredicted);
        predicted = smoothingRot.mult(prevPredicted);
      }
    }
    prevPredictedMap.put(uuid, predicted);
    return predicted;
  }
  
  public float[] getAngles() {
    Quaternion droneLastRot = new Quaternion(
      this.x,
      this.y,
      this.z,
      this.w
    );
    
    Vector3f droneLook = new Vector3f(0, 0, 1);
    Vector3f droneUp = new Vector3f(0, 1, 0);
    
    droneLook = droneLastRot.mult(droneLook);
    droneUp = droneLastRot.mult(droneUp);
    Vector3f droneLeft = droneUp.cross(droneLook);
    
    Quaternion cameraRot = (new Quaternion()).fromAngleAxis(
      -30 * rads,
      droneLeft
    );
    Vector3f cameraLook = cameraRot.mult(droneLook);
    Vector3f cameraUp = cameraRot.mult(droneUp);
    
    float[] droneAngles = Transforms.getWorldEulerAngles(droneLook, droneUp);
    float[] cameraAngles = Transforms.getWorldEulerAngles(cameraLook, cameraUp);
    
    float[] angles = new float[6];
    angles[0] = droneAngles[0];
    angles[1] = droneAngles[1];
    angles[2] = droneAngles[2];
    angles[3] = cameraAngles[0];
    angles[4] = cameraAngles[1];
    angles[5] = cameraAngles[2];
    
    return angles;
  }
  
  public static float[] getMotorPos(UUID uuid, float elapsed) {
    float[] motorPos;
    
    PlayerEntity self = Minecraft.getInstance().player;
    if (self != null && self.getUUID().equals(uuid)) {
      // self is always up-to-date, so no interpolation is needed.
      motorPos = PhysicsState.getCore().getMotorPos();
    } else {
      motorPos = DroneState.motorPosMap.getOrDefault(uuid, null);
      if (motorPos == null) {
        motorPos = new float[]{0, 0, 0, 0};
      }
    }
    
    DroneState last = DroneState.lastMap.getOrDefault(uuid, null);
    
    if (last != null) {
      float[] motorVel = last.motorVel;
      for (int motor = 0; motor < 4; motor++) {
        motorPos[motor] += motorVel[motor] *
                           elapsed;
        if (motorPos[motor] < 0) {
          motorPos[motor] = FastMath.TWO_PI +
                            motorPos[motor] %
                            FastMath.TWO_PI;
        } else {
          motorPos[motor] = motorPos[motor] %
                            FastMath.TWO_PI;
        }
      }
    }
    
    return motorPos;
  }
}
