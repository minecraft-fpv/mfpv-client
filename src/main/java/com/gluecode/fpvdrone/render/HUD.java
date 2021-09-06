package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.physics.PhysicsState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class HUD {
  
  @SubscribeEvent
  public static void handleOverlayRender(RenderGameOverlayEvent.Text event) {
    // on screen debugging:
    if (ControllerReader.getCustomAngle()) {
      long tickTime = System.currentTimeMillis();
      float elapsed = Math.toIntExact(tickTime - ControllerReader.angleChangeTime) / 1000F;
      if (elapsed < 3F) {
        event.getLeft().add("angle: " + DroneBuild.getCameraAngle());
      }
    }
    
    if (PhysicsState.getCore().isOverheat()) {
      event.getLeft().add("Overheating...");
    }
  }
}
