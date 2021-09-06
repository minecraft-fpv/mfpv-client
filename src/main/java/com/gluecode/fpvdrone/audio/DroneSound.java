package com.gluecode.fpvdrone.audio;

import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.physics.PhysicsState;
import com.jme3.math.FastMath;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DroneSound extends TickableSound {
  private final ClientPlayerEntity player;
  
  public DroneSound(ClientPlayerEntity playerIn) {
    super(SoundEvents.ELYTRA_FLYING, SoundCategory.PLAYERS);
    this.player = playerIn;
    this.looping = true;
    this.delay = 0;
    this.volume = 0.1F;
  }
  
  public void tick() {
    //        float throt = FastMath.clamp((InputHandler.throttle + 1F) * 0.5F, 0F, 1.0F);
    if (!this.player.removed && ControllerReader.getArm()) {
      this.x = (float) this.player.getX();
      this.y = (float) this.player.getY();
      this.z = (float) this.player.getZ();
      
      float kv = (DroneBuild.getMotorKv() * 0.104719755f);
      float maxSpeed = kv * DroneBuild.getBatteryCells() * 4.2f;
      float lerp = FastMath.abs(PhysicsState.getCore().getMotorVel()[0]) / maxSpeed;
      
      this.volume = MathHelper.lerp(lerp, 0.25f, 1f);
      this.pitch = MathHelper.lerp(lerp, 0.5f, 2f);
      
      //            Main.LOGGER.debug("volume: " + this.volume);
      //            Main.LOGGER.debug("pitch: " + this.pitch);
    } else {
      this.stop();
    }
  }
}
