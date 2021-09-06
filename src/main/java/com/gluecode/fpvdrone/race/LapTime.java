package com.gluecode.fpvdrone.race;

import com.jme3.math.FastMath;

import java.util.UUID;

public class LapTime implements Comparable<LapTime> {
  public UUID userId;
  public int millis;

  public LapTime(UUID userId, int millis) {
    this.userId = userId;
    this.millis = millis;
  }

  @Override
  public int compareTo(LapTime o) {
    return Integer.compare(this.millis, o.millis);
  }
}
