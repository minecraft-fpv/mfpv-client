package com.gluecode.fpvdrone.race;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Supplier;

public class SerialRaceTrack {
  public UUID trackId;
  public String name;
  public ArrayList<SerialRaceGate> gates;

  public SerialRaceTrack(
    UUID trackId,
    String name,
    ArrayList<SerialRaceGate> gates
  ) {
    this.trackId = trackId;
    this.name = name;
    this.gates = gates;
  }

  public static void encode(SerialRaceTrack msg, FriendlyByteBuf buffer) {
    buffer.writeLong(msg.trackId.getMostSignificantBits());
    buffer.writeLong(msg.trackId.getLeastSignificantBits());
    buffer.writeUtf(msg.name);
    SerialRaceTrack.encodeGates(msg.gates, buffer);
  }

  public static SerialRaceTrack decode(FriendlyByteBuf buffer) {
    return new SerialRaceTrack(new UUID(
      buffer.readLong(),
      buffer.readLong()
    ), buffer.readUtf(), decodeGates(buffer));
  }

  public static void encodeGates(
    ArrayList<SerialRaceGate> gates,
    FriendlyByteBuf buffer
  ) {
    buffer.writeVarInt(gates.size());

    for (int i = 0; i < gates.size(); i++) {
      SerialRaceGate gate = gates.get(i);
      SerialRaceGate.encode(gate, buffer);
    }
  }

  public static ArrayList<SerialRaceGate> decodeGates(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();

    ArrayList<SerialRaceGate> gates = new ArrayList<>();

    for (int i = 0; i < size; i++) {
      gates.add(SerialRaceGate.decode(buffer));
    }

    return gates;
  }

  public int hashCode() {
    return this.trackId.hashCode();
  }

  public boolean equals(Object o) {
    if (!(o instanceof SerialRaceTrack)) {
      return false;
    }
    if (this == o) {
      return true;
    }
    return this.trackId.equals(((SerialRaceTrack) o).trackId);
  }

  public static void handle(
    SerialRaceTrack msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> SerialRaceTrack.handleClient(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }

  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    SerialRaceTrack msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    RaceClient.loadTrack(msg);
  }
}
