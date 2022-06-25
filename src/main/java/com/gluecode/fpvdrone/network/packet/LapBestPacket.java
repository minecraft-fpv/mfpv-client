package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.race.RaceClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class LapBestPacket {
  private int millis;
  private UUID trackId;
  private UUID userId;

  public LapBestPacket(int millis, UUID trackId, UUID userId) {
    this.millis = millis;
    this.trackId = trackId;
    this.userId = userId;
  }

  public static LapBestPacket decode(FriendlyByteBuf buffer) {
    return new LapBestPacket(
      buffer.readInt(),
      new UUID(buffer.readLong(), buffer.readLong()),
      new UUID(buffer.readLong(), buffer.readLong())
    );
  }

  public static void encode(LapBestPacket msg, FriendlyByteBuf buffer) {
    buffer.writeInt(msg.millis);
    buffer.writeLong(msg.trackId.getMostSignificantBits());
    buffer.writeLong(msg.trackId.getLeastSignificantBits());
    buffer.writeLong(msg.userId.getMostSignificantBits());
    buffer.writeLong(msg.userId.getLeastSignificantBits());
  }

  public static void handle(
    LapBestPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> LapBestPacket.handleClient(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }

  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    LapBestPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    RaceClient.handleLapBestPacket(msg.millis, msg.trackId, msg.userId);
  }
}
