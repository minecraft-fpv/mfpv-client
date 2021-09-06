package com.gluecode.fpvdrone.network.packet;

import com.gluecode.fpvdrone.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SetArmPacket {
  private final boolean armed;
  private final long uuidMostSig;
  private final long uuidLeastSig;
  
  public SetArmPacket(boolean isArmed, UUID uuid) {
    this.armed = isArmed;
    this.uuidMostSig = uuid.getMostSignificantBits();
    this.uuidLeastSig = uuid.getLeastSignificantBits();
  }
  
  public static SetArmPacket decode(PacketBuffer buffer) {
    return new SetArmPacket(
      buffer.readBoolean(),
      new UUID(buffer.readLong(), buffer.readLong())
    );
  }
  
  public static void encode(SetArmPacket msg, PacketBuffer buffer) {
    buffer.writeBoolean(msg.armed);
    buffer.writeLong(msg.uuidMostSig);
    buffer.writeLong(msg.uuidLeastSig);
  }
  
  public static void handle(
    SetArmPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    // Note:
    // @OnlyIn(Dist.DEDICATED_SERVER) will never run on clients
    // Marked code will be removed from client-side builds.
    
    // If you need something to run on the integrated server, which is included in the client-side build,
    // then you need to use
    // `boolean isIntegratedServer = !isRemote`
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> SetArmPacket.handleClient(msg, contextSupplier)
      );
      DistExecutor.runWhenOn(
        Dist.DEDICATED_SERVER,
        () -> () -> SetArmPacket.handleServer(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }
  
  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    SetArmPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    UUID uuid = new UUID(msg.uuidMostSig, msg.uuidLeastSig);
    PlayerEntity player = Minecraft.getInstance().player;
    if (player == null || !uuid.equals(player.getUUID())) {
      // Do not set self because when self sends the packet, it will echo back.
      // The echoed packet is out-of-date compared to self.
      Main.entityArmStates.put(uuid, msg.armed);
      if (!msg.armed) {
        Main.droneRenderers.remove(uuid);
      }
    }
    
    boolean isIntegratedServer = !Main.isClientSide();
    if (isIntegratedServer) {
      PacketHandler.sendToAll(msg);
    }
  }
  
  @OnlyIn(Dist.DEDICATED_SERVER)
  private static void handleServer(
    SetArmPacket msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    UUID uuid = new UUID(msg.uuidMostSig, msg.uuidLeastSig);
    Main.entityArmStates.put(uuid, msg.armed);
    if (!msg.armed) {
      Main.droneRenderers.remove(uuid);
    }
    
    PacketHandler.sendToAll(msg);
  }
}
