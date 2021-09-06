package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.race.RaceClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SetRaceModePacket {
  private final boolean value;
  private final UUID trackId;
  private final UUID userId;

  public SetRaceModePacket(boolean value, UUID trackUUID, UUID userUUID) {
    this.value = value;
    this.trackId = trackUUID;
    this.userId = userUUID;
  }

  public static SetRaceModePacket decode(PacketBuffer buffer) {
    return new SetRaceModePacket(
      buffer.readBoolean(),
      new UUID(buffer.readLong(), buffer.readLong()),
      new UUID(buffer.readLong(), buffer.readLong())
    );
  }

  public static void encode(SetRaceModePacket msg, PacketBuffer buffer) {
    buffer.writeBoolean(msg.value);
    buffer.writeLong(msg.trackId.getMostSignificantBits());
    buffer.writeLong(msg.trackId.getLeastSignificantBits());
    buffer.writeLong(msg.userId.getMostSignificantBits());
    buffer.writeLong(msg.userId.getLeastSignificantBits());
  }

  public static void handle(
    SetRaceModePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> SetRaceModePacket.handleClient(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }

  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    SetRaceModePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    RaceClient.handleRaceModePacket(msg.value, msg.trackId, msg.userId);
  }
}
