package com.gluecode.fpvdrone.audio;

import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.physics.PhysicsState;
import com.jme3.math.FastMath;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DroneSound extends AbstractTickableSoundInstance {
  private final LocalPlayer player;
  
  public DroneSound(LocalPlayer playerIn) {
    super(SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS);
    this.player = playerIn;
    this.looping = true;
    this.delay = 0;
    this.volume = 0.1F;
  }
  
  public void tick() {
    //        float throt = FastMath.clamp((InputHandler.throttle + 1F) * 0.5F, 0F, 1.0F);
    if (!this.player.isRemoved() && ControllerReader.getArm()) {
      this.x = (float) this.player.getX();
      this.y = (float) this.player.getY();
      this.z = (float) this.player.getZ();
      
      float kv = (DroneBuild.getMotorKv() * 0.104719755f);
      float maxSpeed = kv * DroneBuild.getBatteryCells() * 4.2f;
      float lerp = FastMath.abs(PhysicsState.getCore().getMotorVel()[0]) / maxSpeed;
      
      this.volume = Mth.lerp(lerp, 0.25f, 1f);
      this.pitch = Mth.lerp(lerp, 0.5f, 2f);
      
      //            Main.LOGGER.debug("volume: " + this.volume);
      //            Main.LOGGER.debug("pitch: " + this.pitch);
    } else {
      this.stop();
    }
  }
}
