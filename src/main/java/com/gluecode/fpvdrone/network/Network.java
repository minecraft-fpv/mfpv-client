package com.gluecode.fpvdrone.network;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.network.packet.DroneBuildPacket;
import com.gluecode.fpvdrone.network.packet.DroneStatePacket;
import com.gluecode.fpvdrone.network.packet.PacketHandler;
import com.gluecode.fpvdrone.network.packet.SetArmPacket;
import com.jme3.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.UUID;

@OnlyIn(Dist.CLIENT)
public class Network {
  public static void updateArmState(Player player) {
    // Poses are automatically synced from server->client, so we don't have to worry about other players on the client
    if (player == Minecraft.getInstance().player) {
      updateClientArmState();
    }
  }
  
  public static void updateDroneState(
    Player player,
    Quaternion rot,
    float motorVel1,
    float motorVel2,
    float motorVel3,
    float motorVel4
  ) {
    UUID uuid = player.getUUID();
    PacketHandler.sendToServer(new DroneStatePacket(
      uuid,
      rot.getX(),
      rot.getY(),
      rot.getZ(),
      rot.getW(),
      motorVel1,
      motorVel2,
      motorVel3,
      motorVel4,
      System.currentTimeMillis()
    ));
  }
  
  private static void updateClientArmState() {
    Player player = Minecraft.getInstance().player;
    if (player != null) {
      UUID uuid = player.getUUID();
      if (ControllerReader.getArm() != Main.entityArmStates.getOrDefault(
        uuid,
        false
      )) {
        PacketHandler.sendToServer(new SetArmPacket(
          ControllerReader.getArm(),
          uuid
        ));
        
        if (ControllerReader.getArm()) {
          // When arming, the current build is re-sent to the server,
          // which will forward it to all users,
          // which will cause all users to rebuild the DroneRenderer.
          PacketHandler.sendToServer(
            new DroneBuildPacket(uuid, DroneBuild.getSelf())
          );
        }
      }
      Main.entityArmStates.put(uuid, ControllerReader.getArm());
    }
  }
}
