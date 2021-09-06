package com.gluecode.fpvdrone.physics;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.network.DroneState;
import com.gluecode.fpvdrone.network.Network;
import com.gluecode.fpvdrone.network.packet.DroneBuildPacket;
import com.gluecode.fpvdrone.network.packet.PacketHandler;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class PhysicsEvent {
  private static long playerTime = System.currentTimeMillis();
  public static float broadcastedCustomAngle = 0;
  
  
  /*
  Enter a server in a disarmed state.
  
  TODO:
  This might be a good place to check if the server allows flying,
  and then handle accordingly by preventing arming.
  * */
  @SubscribeEvent
  public static void onLoggedIn(ClientPlayerNetworkEvent.LoggedInEvent event) {
    ControllerReader.setArm(false);
  }
  
  /*
  Freeze time when any GUI is open
  by updating lastPhysicsTime before running physics.
  
  Normally, physics runs before lastPhysicsTime is updated.
  * */
  @SubscribeEvent
  public static void onGuiOpen(GuiScreenEvent.DrawScreenEvent.Post event) {
    if (ControllerReader.getArm()) {
      playerTime = System.currentTimeMillis();
    }
  }
  
  /*
  Trigger physics update at tick speed, usually 20 tDroneBuild.
  
  Updating physics in this way will cause vanilla to linearly extrapolate
  the camera position.
  
  It also causes stick input to be read at 20 tDroneBuild. This slow polling rate
  is substantial and makes a difference in control and confidence.
  
  It is not recommended to trigger physics in this way.
  * */
  @SubscribeEvent
  public static void handleMovement(LivingEvent.LivingUpdateEvent event) {
    if (ControllerReader.getArm()) {
      Minecraft minecraft = Minecraft.getInstance();
      ClientPlayerEntity player = minecraft.player;
      if (event.getEntity() == player) {
        if (SettingsLoader.currentUseRealtimePhysics) {
          //        player.setPosAndOldPos(player.getX(), player.getY(), player.getZ());
        } else {
          float elapsedSeconds = ((int) (System.currentTimeMillis() - playerTime)) / 1000F;
          playerTime = System.currentTimeMillis();
          //        PhysicsState.getCore().step(elapsedSeconds);
        }
      }
    }
  }
  
  /*
  Trigger physics update at fps speed, hopefully 60+.
  
  This will cause player position to change at the beginning of rendering.
  
  Note that RenderTickEvent is a very early event in the game loop.
  It runs before any camera or openGL stuff.
  * */
  @SubscribeEvent
  public static void beginRender(TickEvent.RenderTickEvent event) {
    if (ControllerReader.getArm() && Minecraft.getInstance().screen == null) {
      if (event.phase == TickEvent.Phase.START) {
        if (SettingsLoader.currentUseRealtimePhysics) {
          float elapsedSeconds = ((int) (System.currentTimeMillis() - playerTime)) / 1000F;
          playerTime = System.currentTimeMillis();
          PhysicsState.getCore().step(elapsedSeconds);
        }
      }
  
      if (event.phase == TickEvent.Phase.END) {
        // Update player's drone state for third person mode.
        Minecraft minecraft = Minecraft.getInstance();
        PlayerEntity player = minecraft.player;
        if (player != null) {
          Quaternion rot = (new Quaternion());
          rot.lookAt(PhysicsState.getCore().getDroneLook(), PhysicsState.getCore().getDroneUp());
          DroneState.update(
            player.getUUID(),
            new DroneState(
              rot.getX(),
              rot.getY(),
              rot.getZ(),
              rot.getW(),
              PhysicsState.getCore().getMotorVel()[0],
              PhysicsState.getCore().getMotorVel()[1],
              PhysicsState.getCore().getMotorVel()[2],
              PhysicsState.getCore().getMotorVel()[3],
              System.currentTimeMillis()
            )
          );
        }
      }
    }
  }
  
  /*
  Send physics state to server.
  
  This should run after updating physics (trailing trigger).
  * */
  @SubscribeEvent(priority = EventPriority.LOWEST)
  public static void sendSelfToServer(LivingEvent.LivingUpdateEvent event) {
    Minecraft minecraft = Minecraft.getInstance();
    ClientPlayerEntity player = minecraft.player;
    if (player == null) {
      return;
    }
    if (event.getEntity() != player) {
      // Only handle self.
      return;
    }
    
    if (player.level.isClientSide) {
      Network.updateArmState(player);
      
      if (ControllerReader.getArm()) {
        Quaternion rot = (new Quaternion());
        rot.lookAt(
          PhysicsState.getCore().getDroneLook().clone(),
          PhysicsState.getCore().getDroneUp().clone()
        );
        
        Network.updateDroneState(
          player,
          rot,
          PhysicsState.getCore().getMotorVel()[0],
          PhysicsState.getCore().getMotorVel()[1],
          PhysicsState.getCore().getMotorVel()[2],
          PhysicsState.getCore().getMotorVel()[3]
        );
        
        if (ControllerReader.getCustomAngle()) {
          float currentCustomAngle = DroneBuild.getCameraAngle();
          if (currentCustomAngle != broadcastedCustomAngle) {
            broadcastedCustomAngle = currentCustomAngle;
            DroneBuildPacket droneBuildPacket = new DroneBuildPacket(
              player.getUUID(), DroneBuild.getSelf()
            );
            PacketHandler.sendToServer(droneBuildPacket);
          }
        }
      }
    }
  }
}
