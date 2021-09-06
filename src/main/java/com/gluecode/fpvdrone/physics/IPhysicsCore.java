package com.gluecode.fpvdrone.physics;

import com.jme3.math.Vector3f;

public interface IPhysicsCore {
  void step(float dt);
  float[] getMotorVel();
  float[] getMotorPos();
  Vector3f getVelocity();
  void setVelocity(Vector3f value);
  Vector3f getDroneLook();
  void setDroneLook(Vector3f value);
  Vector3f getDroneUp();
  void setDroneUp(Vector3f value);
  Vector3f getDroneLeft();
  void setDroneLeft(Vector3f value);
  boolean isOverheat();
}
