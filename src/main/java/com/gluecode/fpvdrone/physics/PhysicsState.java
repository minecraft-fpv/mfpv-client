package com.gluecode.fpvdrone.physics;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
//import com.mojang.math.Vector3d;

public class PhysicsState {
  private static IPhysicsCore core = new DefaultPhysicsCore();
  
  public static IPhysicsCore getCore() {
    return core;
  }
  
  public static void setCore(IPhysicsCore inCore) {
    if (inCore == null) return;
    core = inCore;
  }
  
  private static final Vector3f gravity = new Vector3f(0, -9.80665F, 0);
  public static Vector3f getGravity() {
    return gravity;
  }
  
  /*
  * Handles collisions and moves the player.
  *
  * Returns the velocity the player has after handling collision.
  * */
  public static Vector3f collideAndMove(Vector3f nextVelocity, float elapsed) {
    if (!ControllerReader.getArm()) {
      return nextVelocity;
    }
    
    Minecraft minecraft = Minecraft.getInstance();
    Player entity = minecraft.player;
  
    if (entity == null) {
      return nextVelocity;
    }
  
    Main.LOGGER.info("collide 4");
    
    float limit = 500; // m/s
    float elastic = 0.2f;
    //    float limit = 500.408f;
    if (nextVelocity.length() > limit) {
      nextVelocity.normalizeLocal().multLocal(limit);
    }
  
    Vector3f displacement = nextVelocity.mult(elapsed);
    Vec3 desired = new Vec3(
      displacement.x,
      displacement.y,
      displacement.z
    );
    float desiredSpeed = nextVelocity.length();
    if (entity.noPhysics) {
      entity.setPos(entity.getX() + desired.x, entity.getY() + desired.y, entity.getZ() + desired.z);
      return nextVelocity;
    } else {
      Vec3 clipped = entity.collide(desired);
    
      boolean collidedHorizontally = !Mth.equal(
        desired.x,
        clipped.x
      ) || !Mth.equal(desired.z, clipped.z);
      boolean collidedVertically = !Mth.equal(
        desired.y,
        clipped.y
      );
      //      boolean onGround = collidedVertically && desired.y < 0.0D;
      boolean collided = collidedHorizontally || collidedVertically;
    
      if (collided) {
        CollisionResults collisionResults = PhysicsCollision.getCollisionResults(
          displacement,
          clipped,
          nextVelocity
        );

        entity.setPos(entity.getX() + collisionResults.displacement.x, entity.getY() + collisionResults.displacement.y, collisionResults.displacement.z);

        return collisionResults.velocity;
      }
    
      entity.setBoundingBox(entity.getBoundingBox().move(clipped));
      if (SettingsLoader.currentUseRealtimePhysics) {
        entity.setPos(entity.getX(), entity.getY(), entity.getZ());
        entity.setOldPosAndRot();
      }
      //entity.setLocationFromBoundingbox();
      AABB axisalignedbb = entity.getBoundingBox();
      entity.setPosRaw((axisalignedbb.minX + axisalignedbb.maxX) / 2.0D, axisalignedbb.minY, (axisalignedbb.minZ + axisalignedbb.maxZ) / 2.0D);
    
      Vec3 clippedVelocity = clipped.scale(1f / elapsed);
      return new Vector3f(
        (float) clippedVelocity.x,
        (float) clippedVelocity.y,
        (float) clippedVelocity.z
      );
    }
  }
  
  public static float getThrottle() {
    return ControllerReader.getThrottle();
  }
  
  public static float getDroneMass() {
    return DroneBuild.getDroneMass();
  }
  
  public static float getPropRadius() {
    return DroneBuild.getPropDiameter() * 0.5f;
  }
  
  public static int getBatteryCells() {
    return DroneBuild.getBatteryCells();
  }
  
  public static float getKv() {
    return DroneBuild.getMotorKv();
  }
  
  public static int getBlades() {
    return DroneBuild.getBlades();
  }
  
  public static float getMotorMass() {
    return DroneBuild.getMotorMass();
  }
  
  public static float getMotorWidth() {
    return DroneBuild.getMotorWidth();
  }
  
  public static float getMotorHeight() {
    return DroneBuild.getMotorHeight();
  }
  
  public static float getPropPitch() {
    return DroneBuild.getPropPitch();
  }
  
  public static float getPropWidth() {
    return DroneBuild.getPropWidth();
  }
  
  public static float getBladeMass(float propDiameter) {
    return DroneBuild.getBladeMass(propDiameter);
  }
  
  public static float getBellMass() {
    return DroneBuild.getBellMass();
  }
  
  public static boolean getFlightMode3d() {
    return DroneBuild.getFlightMode3d();
  }
}
