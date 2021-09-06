package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.network.DroneState;
import com.gluecode.fpvdrone.physics.PhysicsState;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.gluecode.fpvdrone.util.Transforms;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class CameraManager {
  private static final float rads = (float) (Math.PI / 180);
  private static final float degs = (float) (180 / Math.PI);
  
  public static float cameraRoll = 0;
  
  private static boolean showCrosshairs = false;
  private static boolean showBlockOutline = true;
  private static boolean showStickOverlay = true;
  
  private static long cameraTime = System.currentTimeMillis();
  
  public static boolean getShowCrosshairs() {
    return showCrosshairs;
  }
  
  public static boolean getShowBlockOutline() {
    return showBlockOutline;
  }
  
  public static boolean getShowStickOverlay() {
    return showStickOverlay;
  }
  
  public static void setShowCrosshairs(boolean value) {
    showCrosshairs = value;
  }
  
  public static void setShowBlockOutline(boolean value) {
    showBlockOutline = value;
  }
  
  public static void setShowStickOverlay(boolean value) {
    showStickOverlay = value;
  }
  
  public static void resetShowCrosshairs() {
    setShowCrosshairs(getDefaultShowCrosshairs());
  }
  
  public static void resetShowBlockOutline() {
    setShowBlockOutline(getDefaultShowBlockOutline());
  }
  
  public static void resetShowStickOverlay() {
    setShowStickOverlay(getDefaultShowStickOverlay());
  }
  
  public static boolean getDefaultShowCrosshairs() {
    if (ControllerConfig.getIsGamepad()) {
      return true;
    } else {
      return SettingsLoader.defaultShowCrosshairs;
    }
  }
  
  public static boolean getDefaultShowBlockOutline() {
    return SettingsLoader.defaultShowBlockOutline;
  }
  
  public static boolean getDefaultShowStickOverlay() {
    return SettingsLoader.defaultShowStickOverlay;
  }
  
  
  
  /*
  Override's vanilla camera while in spectate mode.
  
  Our specator camera controller has more interpolation than the vanilla
  camera controller.
  * */
  @SubscribeEvent
  public static void setSpectateCamera(EntityViewRenderEvent.CameraSetup event) {
    Minecraft minecraft = Minecraft.getInstance();
    Entity cameraEntity = minecraft.getCameraEntity();
    PlayerEntity self = minecraft.player;
    if (cameraEntity instanceof PlayerEntity && !self.equals(cameraEntity)) {
      UUID uuid = cameraEntity.getUUID();
      boolean isArmed = Main.entityArmStates.getOrDefault(uuid, false);
      if (isArmed) {
        DroneState playerOrientation = DroneState.getInterpolated(
          uuid,
          (float) event.getRenderPartialTicks()
        );
        float[] angles = playerOrientation.getAngles();
        float cameraYaw = angles[3];
        float cameraPitch = angles[4];
        float cameraRoll = angles[5];
        event.setYaw(cameraYaw);
        event.setPitch(cameraPitch);
        event.setRoll(cameraRoll);
      }
    }
  }
  
  /*
  Overrides the vanilla camera while in drone mode.
  
  // todo: separate out non-camera stuff
  * */
  @SubscribeEvent
  public static void setCamera(EntityViewRenderEvent.CameraSetup event) {
    long tickTime = System.currentTimeMillis();
    float elapsed = Math.toIntExact(tickTime - cameraTime) / 1000F;
    cameraTime = tickTime;
  
    //    Main.LOGGER.info("elapsed: " + 1f / elapsed);
    Minecraft minecraft = Minecraft.getInstance();
    PlayerEntity player = minecraft.player;
    if (minecraft.level != null &&
        player != null &&
        !minecraft.isPaused()) {
      if (ControllerReader.getArm()) {
        if (SettingsLoader.currentUseRealtimePhysics) {
          // Force the camera position to the latest player position
          // because physics just finished running before this function
          // was called.
          
          Object o = minecraft.getCameraEntity() ==
                     null ? minecraft.player : minecraft.getCameraEntity();
          
          if (o instanceof Entity) {
            minecraft.gameRenderer.getMainCamera().setup(
              minecraft.level,
              (Entity) o,
              !minecraft.options.getCameraType().isFirstPerson(),
              minecraft.options.getCameraType().isMirrored(),
              1
            );
          }
        }
        
        // Force-set the FOV:
        int height = minecraft.getWindow().getGuiScaledHeight();
        int width = minecraft.getWindow().getGuiScaledWidth();
        float diagonal = FastMath.sqrt(height * height + width * width);
        float verticalFov = 2f * FastMath.atan(height / diagonal *
                                               FastMath.tan(
                                                 SettingsLoader.currentFov *
                                                 rads / 2f));
        minecraft.options.fov = verticalFov * degs;
        
        // Determine camera rotation:
        float cameraAngle = DroneBuild.getCameraAngle();
        Quaternion cameraRot = (new Quaternion()).fromAngleAxis(
          -cameraAngle * rads,
          PhysicsState.getCore().getDroneLeft()
        );
        Vector3f cameraLook = cameraRot.mult(PhysicsState.getCore().getDroneLook());
        Vector3f cameraUp = cameraRot.mult(PhysicsState.getCore().getDroneUp());
  
        float[] droneAngles = Transforms.getWorldEulerAngles(PhysicsState.getCore().getDroneLook(), PhysicsState.getCore().getDroneUp());
        float droneYaw = droneAngles[0];
        float dronePitch = droneAngles[1];
        float droneRoll = droneAngles[2];
        
        float[] eulerAngles = Transforms.getWorldEulerAngles(cameraLook, cameraUp);
        float cameraYaw = eulerAngles[0];
        float cameraPitch = eulerAngles[1];
        float cameraRoll = eulerAngles[2];
        
        // Minecraft yaw doesn't like it when there are jump discontinuities.
        // Player yaw must be incremented or decremented smoothly:
        if (droneYaw < 0f) {
          droneYaw += 360f;
        }
        float prevYaw = player.yRot % 360f;
        if (prevYaw < 0f) {
          prevYaw += 360f;
        }
        float ydiff = droneYaw - prevYaw;
        if (ydiff > 180f) {
          ydiff -= 360f;
        } else if (ydiff < -180f) {
          ydiff += 360f;
        }
        player.yRot += ydiff; // ydiff is based on droneYaw, not cameraYaw
        player.xRot = cameraPitch;
        
        // Set the renderer's camera rotation:
        event.setYaw(cameraYaw);
        event.setPitch(cameraPitch);
        event.setRoll(cameraRoll);
        
        // Set camera distance (third person or first person):
        if (minecraft.options.getCameraType() ==
            PointOfView.THIRD_PERSON_BACK) {
          ActiveRenderInfo info = event.getInfo();
          double partialTicks = event.getRenderPartialTicks();
          Entity renderViewEntity = info.getEntity();
          
          // Set direction according to drone camera
          info.setRotation(cameraYaw, cameraPitch);
          
          // Reset camera position according to how it is done in ActiveRenderInfo.update();
          info.setPosition(
            MathHelper.lerp(
              (double) partialTicks,
              renderViewEntity.xo,
              renderViewEntity.getX()
            ),
            MathHelper.lerp(
              (double) partialTicks,
              renderViewEntity.yo,
              renderViewEntity.getY()
            ) +
            (double) MathHelper.lerp(
              partialTicks,
              info.eyeHeightOld,
              info.eyeHeight
            ),
            MathHelper.lerp(
              (double) partialTicks,
              renderViewEntity.zo,
              renderViewEntity.getZ()
            )
          );
          
          // Move the camera the same way it's done in ActiveRenderInfo.update() but with a different startingDistance.
          event.getInfo().move(-event.getInfo()
            .getMaxZoom(0.5), 0.0D, 0.0D);
        } else if (minecraft.options.getCameraType() ==
                   PointOfView.THIRD_PERSON_FRONT) {
          ActiveRenderInfo info = event.getInfo();
          double partialTicks = event.getRenderPartialTicks();
          Entity renderViewEntity = info.getEntity();
          
          // Set direction according to drone camera
          event.setYaw(cameraYaw + 180);
          event.setPitch(-cameraPitch);
          event.setRoll(-cameraRoll);
          info.setRotation(cameraYaw + 180, -cameraPitch);
          
          // Reset camera position according to how it is done in ActiveRenderInfo.update();
          info.setPosition(
            MathHelper.lerp(
              (double) partialTicks,
              renderViewEntity.xo,
              renderViewEntity.getX()
            ),
            MathHelper.lerp(
              (double) partialTicks,
              renderViewEntity.yo,
              renderViewEntity.getY()
            ) +
            (double) MathHelper.lerp(
              partialTicks,
              info.eyeHeightOld,
              info.eyeHeight
            ),
            MathHelper.lerp(
              (double) partialTicks,
              renderViewEntity.zo,
              renderViewEntity.getZ()
            )
          );
          
          // Move the camera the same way it's done in ActiveRenderInfo.update() but with a different startingDistance.
          event.getInfo().move(-event.getInfo()
            .getMaxZoom(0.5), 0.0D, 0.0D);
        }
      }
    }
  }
}
