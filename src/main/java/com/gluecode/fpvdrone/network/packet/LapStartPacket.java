package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.race.RaceClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class LapStartPacket {
  private long startTimeMillis;
  private long uuidMostSig;
  private long uuidLeastSig;
  
  public LapStartPacket(long startTimeMillis, UUID uuid) {
    this.startTimeMillis = startTimeMillis;
    this.uuidMostSig = uuid.getMostSignificantBits();
    this.uuidLeastSig = uuid.getLeastSignificantBits();
  }
  
  public static LapStartPacket decode(PacketBuffer buffer) {
    return new LapStartPacket(
      buffer.readLong(),
      new UUID(buffer.readLong(), buffer.readLong())
    );
  }
  
  public static void encode(LapStartPacket msg, PacketBuffer buffer) {
    buffer.writeLong(msg.startTimeMillis);
    buffer.writeLong(msg.uuidMostSig);
    buffer.writeLong(msg.uuidLeastSig);
  }
  
  public static void handle(
    LapStartPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> LapStartPacket.handleClient(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }
  
  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    LapStartPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    RaceClient.handleLapStartPacket(
      msg.startTimeMillis,
      new UUID(msg.uuidMostSig, msg.uuidLeastSig)
    );
  }
}
