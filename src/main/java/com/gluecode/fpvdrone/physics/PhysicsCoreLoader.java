package com.gluecode.fpvdrone.physics;

/*
  This file uses preprocessor directive using:
  https://github.com/manifold-systems/manifold/tree/master/manifold-deps-parent/manifold-preprocessor
  
  You will continue to see syntax errors unless you buy the $20 IntelliJ plugin,
  but you do not need to buy the plugin in order to compile the project.
  The project will still compile despite these preprocessor syntax errors.
  You can ignore this file.
* */

public class PhysicsCoreLoader {
  public static void load() {
#if ADVANCED_PHYSICS
    PhysicsState.setCore(new AdvancedPhysicsCore());
#else
    PhysicsState.setCore(new DefaultPhysicsCore());
#endif
  }
}
