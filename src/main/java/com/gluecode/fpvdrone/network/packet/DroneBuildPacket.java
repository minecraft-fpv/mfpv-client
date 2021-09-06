package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.entity.DroneBuild;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class DroneBuildPacket {
  private UUID userId;
  private float red;
  private float green;
  private float blue;
  private float cameraAngle;
  private float frameWidth;
  private float frameHeight;
  private float frameLength;
  private float motorWidth;
  private float motorHeight;
  private int batteryCells;
  private int batteryMah;
  private int nBlades;
  private float bladeLength;
  private float bladeWidth;
  private float armWidth;
  private float armThickness;
  private float txaLength;
  private boolean showProCam;
  private boolean isHeroCam;
  private boolean isToothpick;
  
  public DroneBuildPacket(
    UUID userId,
    float red,
    float green,
    float blue,
    float cameraAngle,
    float frameWidth,
    float frameHeight,
    float frameLength,
    float motorWidth,
    float motorHeight,
    int batteryCells,
    int batteryMah,
    int nBlades,
    float bladeLength,
    float bladeWidth,
    float armWidth,
    float armThickness,
    float txaLength,
    boolean showProCam,
    boolean isHeroCam,
    boolean isToothpick
  ) {
    this.userId = userId;
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.cameraAngle = cameraAngle;
    this.frameWidth = frameWidth;
    this.frameHeight = frameHeight;
    this.frameLength = frameLength;
    this.motorWidth = motorWidth;
    this.motorHeight = motorHeight;
    this.batteryCells = batteryCells;
    this.batteryMah = batteryMah;
    this.nBlades = nBlades;
    this.bladeLength = bladeLength;
    this.bladeWidth = bladeWidth;
    this.armWidth = armWidth;
    this.armThickness = armThickness;
    this.txaLength = txaLength;
    this.showProCam = showProCam;
    this.isHeroCam = isHeroCam;
    this.isToothpick = isToothpick;
  }
  
  public DroneBuildPacket(
    UUID userId,
    DroneBuild build
  ) {
    this.userId = userId;
    this.red = build.red;
    this.green = build.green;
    this.blue = build.blue;
    this.cameraAngle = build.cameraAngle;
    this.frameWidth = build.frameWidth;
    this.frameHeight = build.frameHeight;
    this.frameLength = build.frameLength;
    this.motorWidth = build.motorWidth;
    this.motorHeight = build.motorHeight;
    this.batteryCells = build.batteryCells;
    this.batteryMah = build.batteryMah;
    this.nBlades = build.nBlades;
    this.bladeLength = build.bladeLength;
    this.bladeWidth = build.bladeWidth;
    this.armWidth = build.armWidth;
    this.armThickness = build.armThickness;
    this.txaLength = build.txaLength;
    this.showProCam = build.showProCam;
    this.isHeroCam = build.isHeroCam;
    this.isToothpick = build.isToothpick;
  }
  
  public static DroneBuildPacket decode(PacketBuffer buffer) {
    return new DroneBuildPacket(
      new UUID(buffer.readLong(), buffer.readLong()),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readFloat(),
      buffer.readBoolean(),
      buffer.readBoolean(),
      buffer.readBoolean()
    );
  }
  
  public static void encode(DroneBuildPacket msg, PacketBuffer buffer) {
    buffer.writeLong(msg.userId.getMostSignificantBits());
    buffer.writeLong(msg.userId.getLeastSignificantBits());
    buffer.writeFloat(msg.red);
    buffer.writeFloat(msg.green);
    buffer.writeFloat(msg.blue);
    buffer.writeFloat(msg.cameraAngle);
    buffer.writeFloat(msg.frameWidth);
    buffer.writeFloat(msg.frameHeight);
    buffer.writeFloat(msg.frameLength);
    buffer.writeFloat(msg.motorWidth);
    buffer.writeFloat(msg.motorHeight);
    buffer.writeInt(msg.batteryCells);
    buffer.writeInt(msg.batteryMah);
    buffer.writeInt(msg.nBlades);
    buffer.writeFloat(msg.bladeLength);
    buffer.writeFloat(msg.bladeWidth);
    buffer.writeFloat(msg.armWidth);
    buffer.writeFloat(msg.armThickness);
    buffer.writeFloat(msg.txaLength);
    buffer.writeBoolean(msg.showProCam);
    buffer.writeBoolean(msg.isHeroCam);
    buffer.writeBoolean(msg.isToothpick);
  }
  
  public static void handle(
    DroneBuildPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> DroneBuildPacket.handleClient(msg, contextSupplier)
      );
      DistExecutor.runWhenOn(
        Dist.DEDICATED_SERVER,
        () -> () -> DroneBuildPacket.handleServer(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }
  
  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    DroneBuildPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    PlayerEntity player = Minecraft.getInstance().player;
    if (player == null || !msg.userId.equals(player.getUUID())) {
      // Do not set self because when self sends the packet, it will echo back.
      // The echoed packet is out-of-date compared to self.
      DroneBuild build = new DroneBuild(
        msg.red,
        msg.green,
        msg.blue,
        msg.cameraAngle,
        msg.frameWidth,
        msg.frameHeight,
        msg.frameLength,
        msg.motorWidth,
        msg.motorHeight,
        msg.batteryCells,
        msg.batteryMah,
        msg.nBlades,
        msg.bladeLength,
        msg.bladeWidth,
        msg.armWidth,
        msg.armThickness,
        msg.txaLength,
        msg.showProCam,
        msg.isHeroCam,
        msg.isToothpick
      );
      Main.droneBuilds.put(msg.userId, build);
      Main.droneRenderers.remove(msg.userId); // forces renderer to rebuild using new droneBuild.
    }
    
    boolean isIntegratedServer = !Main.isClientSide();
    if (isIntegratedServer) {
      PacketHandler.sendToAll(msg);
    }
  }
  
  @OnlyIn(Dist.DEDICATED_SERVER)
  private static void handleServer(
    DroneBuildPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    PacketHandler.sendToAll(msg);
  }
}
