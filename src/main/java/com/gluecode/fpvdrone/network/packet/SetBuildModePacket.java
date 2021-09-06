package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.race.RaceClient;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SetBuildModePacket {
  private final boolean value;

  public SetBuildModePacket(boolean value) {
    this.value = value;
  }

  public static SetBuildModePacket decode(PacketBuffer buffer) {
    return new SetBuildModePacket(buffer.readBoolean());
  }

  public static void encode(SetBuildModePacket msg, PacketBuffer buffer) {
    buffer.writeBoolean(msg.value);
  }

  public static void handle(
    SetBuildModePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> SetBuildModePacket.handleClient(
          msg,
          contextSupplier
        )
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }

  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    SetBuildModePacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    RaceClient.handleBuildModePacket(msg.value);
  }
}
