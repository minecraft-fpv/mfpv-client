package com.gluecode.fpvdrone.race;

import com.gluecode.fpvdrone.Main;
import com.jme3.math.Vector3f;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SerialRaceGate {
  public String dimension;
  public BlockPos origin; // The bottom-left corner of BB (more negative)
  public BlockPos farthest; // // The top-right corner of BB (most positive)
  public int[] rowMin; // left most block (solid) for a given row (relative to some up direction).
  public int[] rowMax; // right most block (solid) for a given row (relative to some up direction).
  public Vector3f right;
  public Vector3f up;
  
  public SerialRaceGate(
    String dimension,
    int originX,
    int originY,
    int originZ,
    int farthestX,
    int farthestY,
    int farthestZ,
    int[] rowMin,
    int[] rowMax,
    int rightX,
    int rightY,
    int rightZ,
    int upX,
    int upY,
    int upZ
  ) {
    this.dimension = dimension;
    this.origin = new BlockPos(originX, originY, originZ);
    this.farthest = new BlockPos(farthestX, farthestY, farthestZ);
    this.rowMin = rowMin;
    this.rowMax = rowMax;
    this.right = new Vector3f(rightX, rightY, rightZ);
    this.up = new Vector3f(upX, upY, upZ);
  }
  
  public static void encode(SerialRaceGate gate, FriendlyByteBuf buffer) {
    buffer.writeUtf(gate.dimension);
    buffer.writeInt(gate.origin.getX());
    buffer.writeInt(gate.origin.getY());
    buffer.writeInt(gate.origin.getZ());
    buffer.writeInt(gate.farthest.getX());
    buffer.writeInt(gate.farthest.getY());
    buffer.writeInt(gate.farthest.getZ());
    buffer.writeVarIntArray(gate.rowMin);
    buffer.writeVarIntArray(gate.rowMax);
    buffer.writeInt((int) gate.right.x);
    buffer.writeInt((int) gate.right.y);
    buffer.writeInt((int) gate.right.z);
    buffer.writeInt((int) gate.up.x);
    buffer.writeInt((int) gate.up.y);
    buffer.writeInt((int) gate.up.z);
  }
  
  public static SerialRaceGate decode(FriendlyByteBuf buffer) {
    return new SerialRaceGate(
      buffer.readUtf(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readVarIntArray(),
      buffer.readVarIntArray(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt(),
      buffer.readInt()
    );
  }
  
  public static void handle(
    SerialRaceGate msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    contextSupplier.get().enqueueWork(() -> {
      DistExecutor.runWhenOn(
        Dist.CLIENT,
        () -> () -> SerialRaceGate.handleClient(msg, contextSupplier)
      );
      contextSupplier.get().setPacketHandled(true);
    });
  }
  
  @OnlyIn(Dist.CLIENT)
  private static void handleClient(
    SerialRaceGate msg,
    Supplier<NetworkEvent.Context> contextSupplier
  ) {
    // todo: check if in build mode
    if (RaceClient.isBuildMode) {
      RaceClient.addGate(msg);
    }
  }
  
  /*In world space*/
  public Vector3f getCenter() {
    Vector3f originCenter = new Vector3f(
      this.origin.getX() + 0.5f,
      this.origin.getY() + 0.5f,
      this.origin.getZ() + 0.5f
    );
    Vector3f farthestCenter = new Vector3f(
      this.farthest.getX() + 0.5f,
      this.farthest.getY() + 0.5f,
      this.farthest.getZ() + 0.5f
    );
    return originCenter.add(farthestCenter).mult(0.5f);
  }
  
  public VoxelShape getShape() {
    int nRows = this.rowMin.length;
    
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.player == null) {
      return Shapes.empty();
    }
    Level world = minecraft.player.getCommandSenderWorld();
    String currentPlayerDimension = Main.getDimension(world);
    if (currentPlayerDimension == null || !currentPlayerDimension.equals(
      this.dimension)) {
      // If the gate is not in the player's world, then there's no point in getting the voxelShape
      // since voxelShape is used for rendering ate outlines.
      // The player can't see the gate since it's in a different dimension....
      // ... Unless the player has the Immersive Portals mod... maybe..
      // Maybe that could be a future feature.
      return Shapes.empty();
    }
    
    int rightX = (int) this.right.getX();
    int rightY = (int) this.right.getY();
    int rightZ = (int) this.right.getZ();
    int upX = (int) this.up.getX();
    int upY = (int) this.up.getY();
    int upZ = (int) this.up.getZ();
    
    VoxelShape result = Shapes.empty();
    
    //    Main.LOGGER.info("start");
    BlockPos testPos;
    for (int row = 0; row < nRows; row++) {
      int colMin = this.rowMin[row];
      int colMax = this.rowMax[row];
      
      for (int col = colMin; col <= colMax; col++) {
        testPos = new BlockPos(
          this.origin.getX() + col * rightX + row * upX,
          this.origin.getY() + col * rightY + row * upY,
          this.origin.getZ() + col * rightZ + row * upZ
        );
        //        Main.LOGGER.info("testPos: " + testPos);
        BlockState state = world.getBlockState(testPos);
        VoxelShape gatePart = state.getShape(world, testPos);
        //        result = Shapes.or(result, gatePart);
        result = Shapes.joinUnoptimized(
          result,
          gatePart,
          BooleanOp.OR
        );
      }
    }
    //    Main.LOGGER.info("end");
    
    //    result = Shapes.combineAndSimplify(Shapes.fullCube(), Shapes.or(makeCuboidShape(0.0D, 0.0D, 4.0D, 16.0D, 3.0D, 12.0D), makeCuboidShape(4.0D, 0.0D, 0.0D, 12.0D, 3.0D, 16.0D), makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 3.0D, 14.0D), makeCuboidShape(2.0D, 4.0D, 2.0D, 14.0D, 16.0D, 14.0D)), BooleanOp.OR);
    
    //    result = result.simplify();
    
    return result;
  }
  
  public static VoxelShape makeCuboidShape(
    double x1,
    double y1,
    double z1,
    double x2,
    double y2,
    double z2
  ) {
    return Shapes.box(
      x1 / 16.0D,
      y1 / 16.0D,
      z1 / 16.0D,
      x2 / 16.0D,
      y2 / 16.0D,
      z2 / 16.0D
    );
  }
}
