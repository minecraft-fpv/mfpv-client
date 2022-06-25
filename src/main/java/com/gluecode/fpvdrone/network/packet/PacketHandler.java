package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.race.SerialRaceGate;
import com.gluecode.fpvdrone.race.SerialRaceTrack;
//import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
  private static final String PROTOCOL_VERSION = Integer.toString(4);
  private static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
    .named(new ResourceLocation(Main.MOD_ID, "main_channel"))
    .clientAcceptedVersions((ver) -> true)
    .serverAcceptedVersions((ver) -> true)
    .networkProtocolVersion(() -> PROTOCOL_VERSION)
    .simpleChannel();
  
  private static int disc = 0;
  
  public static void register() {
    HANDLER.registerMessage(
      disc++,
      LapStartPacket.class,
      LapStartPacket::encode,
      LapStartPacket::decode,
      LapStartPacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      LapBestPacket.class,
      LapBestPacket::encode,
      LapBestPacket::decode,
      LapBestPacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      GateIndexPacket.class,
      GateIndexPacket::encode,
      GateIndexPacket::decode,
      GateIndexPacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      SerialRaceTrack.class,
      SerialRaceTrack::encode,
      SerialRaceTrack::decode,
      SerialRaceTrack::handle
    );
    HANDLER.registerMessage(
      disc++,
      SerialRaceGate.class,
      SerialRaceGate::encode,
      SerialRaceGate::decode,
      SerialRaceGate::handle
    );
    HANDLER.registerMessage(
      disc++,
      DroneBuildPacket.class,
      DroneBuildPacket::encode,
      DroneBuildPacket::decode,
      DroneBuildPacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      SetArmPacket.class,
      SetArmPacket::encode,
      SetArmPacket::decode,
      SetArmPacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      SetRaceModePacket.class,
      SetRaceModePacket::encode,
      SetRaceModePacket::decode,
      SetRaceModePacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      SetBuildModePacket.class,
      SetBuildModePacket::encode,
      SetBuildModePacket::decode,
      SetBuildModePacket::handle
    );
    HANDLER.registerMessage(
      disc++,
      DroneStatePacket.class,
      DroneStatePacket::encode,
      DroneStatePacket::decode,
      DroneStatePacket::handle
    );
  }
  
  /**
   * Sends a packet to the server.<br>
   * Must be called Client side.
   */
  public static void sendToServer(Object msg) {
    HANDLER.sendToServer(msg);
  }
  
  public static void sendToAll(Object msg) {
    HANDLER.send(PacketDistributor.ALL.noArg(), msg);
  }
  
  /**
   * Send a packet to a specific player.<br>
   * Must be called Server side.
   */
//  public static void sendTo(Object msg, ServerPlayerEntity player) {
//    if (!(player instanceof FakePlayer)) {
//      HANDLER.sendTo(
//        msg,
//        player.connection.connection,
//        NetworkDirection.PLAY_TO_CLIENT
//      );
//    }
//  }
}
