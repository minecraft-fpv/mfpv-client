package com.gluecode.fpvdrone.physics;

import com.jme3.math.FastMath;

/*
Unless otherwise stated, all constants here should be natural constants.

Constants should be in SI units.

If a constant isn't natural, then it should have a comment next to it
explaining that it is tuned.
* */
public class PhysicsConstants {
  public static final float rads = (float) (Math.PI / 180);
  public static final float degs = (float) (180 / Math.PI);
  
  public static final float toSIKv = 0.104719755f;
  public static final float inches = 39.3701f; // inches / meter
  
  // Densities:
  public static final float airDensity = 1.225F; // kg / m^3
  public static final float batteryDensity = 2726f; // kg / m^3
  public static final float polycarbonateDensity = 1220; // kg / m^3
  public static final float steelDensity = 8050;
  public static final float neodymiumDensity = 7000;
  public static final float motorSpecificHeatCapacity = (0.385f * 1000f + 0.466f * 1000f) / 2f; // J / (kg * K) average of copper and steel
  public static final float motorMassCoefficient = 5527; // kg / m^3 motor volume * coeff = motor mass
  public static final float carbonFiberDensity = 1550;
  public static final float aluminumDensity = 2700;
  public static final float stackDensity = 750; // Estimated since the electronic stack is mixture of materials.
  
  // Electricity:
  public static final float vac = FastMath.PI * 4 * FastMath.pow(10, -7);
  public static final float resistancePer1000Ft = 26f;
  public static final float tempCoeff = 0.00393f;
  public static final float refTemp = 293.15f; // 20 Celcius
  public static final float heatTransfer = 2000f; // tuned value. In reality, this changes depending on several factors, one of which is the air flow speed past the motor.
}
