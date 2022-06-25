package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.race.RaceClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class GateIndexPacket {
  private int gateIndex;
  private UUID trackId;
  private UUID userId;

  public GateIndexPacket(int gateIndex, UUID trackId, UUID userId) {
    this.gateIndex = gateIndex;
    this.trackId = trackId;
    this.userId = userId;
  }

  public static GateIndexPacket decode(FriendlyByteBuf buffer) {
    return new GateIndexPacket(
      buffer.readInt(),
      new UUID(buffer.readLong(), buffer.readLong()),
      new UUID(buffer.readLong(), buffer.readLong())
    );
  }

  public static void encode(GateIndexPacket msg, FriendlyByteBuf buffer) {
    buffer.writeInt(msg.gateIndex);
    buffer.writeLong(msg.trackId.getMostSignificantBits());
    buffer.writeLong(msg.trackId.getLeastSignificantBits());
    buffer.writeLong(msg.userId.getMostSignificantBits());
    buffer.writeLong(msg.userId.getLeastSignificantBits());
  }

  public static void handle(
    GateIndexPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> GateIndexPacket.handleClient(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }

  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    GateIndexPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    RaceClient.handleGateIndexPacket(
      msg.gateIndex,
      msg.trackId,
      msg.userId
    );
  }
}
