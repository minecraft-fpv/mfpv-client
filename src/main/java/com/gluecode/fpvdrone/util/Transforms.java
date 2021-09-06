package com.gluecode.fpvdrone.util;

import com.gluecode.fpvdrone.physics.PhysicsConstants;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Transforms {
  
  /**
   * @return [yaw, pitch, roll]
   */
  public static float[] getWorldEulerAngles(Vector3f forward, Vector3f up) {
    float worldYaw = 0;
    float worldPitch = 0;
    float worldRoll = 0;
    
    Vector3f axis = Vector3f.NAN;
    
    if (forward.x != 0 || forward.z != 0) {
      // Calc yaw
      Vector3f forwardProj = new Vector3f(forward.x, 0, forward.z);
      Quaternion worldYawRot = (new Quaternion());
      worldYawRot.lookAt(forwardProj, Vector3f.UNIT_Y);
      worldYaw = worldYawRot.toAngleAxis(axis);
      worldYaw = worldYaw * axis.y * PhysicsConstants.degs;
      
      // Calc pitch
      Quaternion antiYawRot = worldYawRot.inverse();
      forwardProj = antiYawRot.mult(forward);
      Quaternion worldPitchRot = (new Quaternion());
      worldPitchRot.lookAt(forwardProj, Vector3f.UNIT_Y);
      worldPitch = worldPitchRot.toAngleAxis(axis);
      worldPitch = worldPitch * axis.x * PhysicsConstants.degs;
      
      // Calc roll
      Quaternion rollessRot = (new Quaternion()).fromAngles(
        worldPitch *
        PhysicsConstants.rads,
        worldYaw * PhysicsConstants.rads,
        0
      );
      Vector3f rollessUp = rollessRot.mult(Vector3f.UNIT_Y);
      Vector3f crossUps = rollessUp.cross(up).normalizeLocal();
      float lookAngle = crossUps.angleBetween(forward) * PhysicsConstants.degs;
      float flip = FastMath.abs(lookAngle) > 90 ? -1 : 1;
      worldRoll = rollessUp.angleBetween(up) * PhysicsConstants.degs * flip;
    } else if (forward.y > 0) {
      // looking straight up
      worldYaw = 0;
      worldPitch = -90;
      
      Vector3f upProj = new Vector3f(up.x, 0, up.z);
      Quaternion rot = (new Quaternion());
      rot.lookAt(upProj, Vector3f.UNIT_Y);
      worldRoll = rot.toAngleAxis(axis);
      worldRoll = worldRoll * axis.y * PhysicsConstants.degs;
    } else if (forward.y < 0) {
      // looking straight down
      worldYaw = 0;
      worldPitch = 90;
      
      Vector3f upProj = new Vector3f(up.x, 0, up.z);
      Quaternion rot = (new Quaternion());
      rot.lookAt(upProj, Vector3f.UNIT_Y);
      worldRoll = rot.toAngleAxis(axis);
      worldRoll = worldRoll * axis.y * PhysicsConstants.degs;
    }
    
    // worldYaw is backwards in minecraft
    return new float[]{-worldYaw, worldPitch, worldRoll};
  }
  
  /**
   * @param rcCommand Raw controller input.
   */
  public static float bfRate(
    float rcCommand,
    float rcRate,
    float superRate,
    float expo
  ) {
    float absRcCommand = FastMath.abs(rcCommand);
    
    if (rcRate > 2.0f) {
      rcRate = rcRate + (14.54f * (rcRate - 2.0f));
    }
    
    if (expo != 0)
      rcCommand = rcCommand *
                  FastMath.pow(FastMath.abs(rcCommand), 3) *
                  expo + rcCommand * (1.0f - expo);
    
    float angleRate = 200.0f * rcRate * rcCommand;
    if (superRate != 0) {
      float rcSuperFactor = 1.0f / (FastMath.clamp(
        1.0f -
        absRcCommand *
        (superRate),
        0.01f,
        1.00f
      ));
      angleRate *= rcSuperFactor;
    }
    
    return angleRate;
  }
  
  public static Supplier<Float> getMillimeter(Supplier<Float> getMeter) {
    return () -> {
      return getMeter.get() * 1000f;
    };
  }
  
  public static Consumer<Float> setMillimeter(Consumer<Float> setMeter) {
    return (millimeter) -> {
      setMeter.accept(millimeter / 1000f);
    };
  }
  
  public static Supplier<Float> getInches(Supplier<Float> getMeter) {
    return () -> {
      return getMeter.get() * PhysicsConstants.inches;
    };
  }
  
  public static Consumer<Float> setInches(Consumer<Float> setMeter) {
    return (inches) -> {
      setMeter.accept(inches / PhysicsConstants.inches);
    };
  }
}
