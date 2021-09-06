package com.gluecode.fpvdrone.physics;

import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import net.minecraft.util.math.vector.Vector3d;

public class PhysicsCollision {
  private static final float elastic = 0.2f;

  /*
  Given a desired displacement and an allowed displacement, figure out the final velocity of the object after colliding.
  * */
  public static CollisionResults getCollisionResults(
    Vector3f displacement,
    Vector3d clipped,
    Vector3f preCollisionVelocity
  ) {
    Vector3f finalDisplacement = null;
    Vector3f finalVelocity = null;
    float desiredSpeed = preCollisionVelocity.length();

    Vector3f clippedF = new Vector3f(
      (float) clipped.x,
      (float) clipped.y,
      (float) clipped.z
    );
    Vector3f vClip = clippedF.subtract(displacement);
    float angle = displacement.normalize().angleBetween(vClip.mult(-1)
      .normalize());
    Vector3f cProj = clippedF.project(vClip.normalize());

    if (Float.isNaN(cProj.x)) {
      // This happens if clippedF and vClip are both 0 vectors.
      CollisionResults results = new CollisionResults();
      results.displacement = clippedF;
      results.velocity = new Vector3f(0, 0, 0);
      return results;
    }

    Vector3f hClip = cProj.subtract(clippedF)
      .normalizeLocal()
      .mult(vClip.length() * FastMath.tan(angle));
    Vector3f puncture = clippedF.add(hClip);
    Vector3f bounceFinal = clippedF.add(vClip);
    Vector3f bounceDirection = bounceFinal.subtract(puncture).normalize();

    float glancing = FastMath.sin(angle);
    float outSpeed = desiredSpeed *
                     (glancing * 0.65f + (1f - glancing) * elastic);
    if (outSpeed < 1f) {
      finalDisplacement = clippedF;
      finalVelocity = bounceDirection.mult(0);
    } else {
      finalDisplacement = bounceFinal;
      finalVelocity = bounceDirection.mult(outSpeed);
    }
    CollisionResults results = new CollisionResults();
    results.displacement = finalDisplacement;
    results.velocity = finalVelocity;
    return results;
  }
}
