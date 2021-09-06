package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.network.DroneState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class DroneStatePacket {
  private final long uuidMostSig;
  private final long uuidLeastSig;
  
  // current quaternion rotation:
  private final float x;
  private final float y;
  private final float z;
  private final float w;
  
  // motor velocities:
  private final float motorVel1;
  private final float motorVel2;
  private final float motorVel3;
  private final float motorVel4;
  
  // current stick inputs:
//  private final float throttle;
//  private final float yaw;
//  private final float pitch;
//  private final float roll;
  
  private final long timeSent;
  // world euler angles.
//  private final float droneYaw;
//  private final float dronePitch;
//  private final float droneRoll;
//  private final float cameraYaw;
//  private final float cameraPitch;
//  private final float cameraRoll;
  
  
  public DroneStatePacket(
    UUID uuid,
    float x,
    float y,
    float z,
    float w,
    float motorVel1,
    float motorVel2,
    float motorVel3,
    float motorVel4,
    long timeSent
  ) {
    this.uuidMostSig = uuid.getMostSignificantBits();
    this.uuidLeastSig = uuid.getLeastSignificantBits();
    this.x = x;
    this.y = y;
    this.z = z;
    this.w = w;
    this.motorVel1 = motorVel1;
    this.motorVel2 = motorVel2;
    this.motorVel3 = motorVel3;
    this.motorVel4 = motorVel4;
    this.timeSent = timeSent;
  }
  
  public static DroneStatePacket decode(PacketBuffer buffer) {
    return new DroneStatePacket(
      new UUID(
        buffer.readLong(),
        buffer.readLong()
      ),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readLong()
    );
  }
  
  public static void encode(DroneStatePacket msg, PacketBuffer buffer) {
    buffer.writeLong(msg.uuidMostSig);
    buffer.writeLong(msg.uuidLeastSig);
    buffer.writeFloat(msg.x);
    buffer.writeFloat(msg.y);
    buffer.writeFloat(msg.z);
    buffer.writeFloat(msg.w);
    buffer.writeFloat(msg.motorVel1);
    buffer.writeFloat(msg.motorVel2);
    buffer.writeFloat(msg.motorVel3);
    buffer.writeFloat(msg.motorVel4);
    buffer.writeLong(msg.timeSent);
  }
  
  public static void handle(
    DroneStatePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> DroneStatePacket.handleClient(
          msg,
          contextSupplier
        )
      );
      DistExecutor.runWhenOn(
        Dist.DEDICATED_SERVER,
        () -> () -> DroneStatePacket.handleServer(
          msg,
          contextSupplier
        )
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }
  
  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    DroneStatePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    UUID uuid = new UUID(msg.uuidMostSig, msg.uuidLeastSig);
    PlayerEntity player = Minecraft.getInstance().player;
    if (player == null || !uuid.equals(player.getUUID())) {
      // Do not set self because when self sends the packet, it will echo back.
      // The echoed packet is out-of-date compared to self.
      DroneState droneOrientation = new DroneState(
        msg.x,
        msg.y,
        msg.z,
        msg.w,
        msg.motorVel1,
        msg.motorVel2,
        msg.motorVel3,
        msg.motorVel4,
        msg.timeSent
      );
      DroneState.update(
        uuid,
        droneOrientation
      );
    }
    
    boolean isIntegratedServer = !Main.isClientSide();
    if (isIntegratedServer) {
      PacketHandler.sendToAll(msg);
    }
  }
  
  @OnlyIn(Dist.DEDICATED_SERVER)
  private static void handleServer(
    DroneStatePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    PacketHandler.sendToAll(msg);
  }
}
