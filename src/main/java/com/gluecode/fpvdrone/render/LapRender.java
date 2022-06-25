package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.Resource;
import com.gluecode.fpvdrone.race.*;
import com.gluecode.fpvdrone.render.shader.ShaderObject;
import com.gluecode.fpvdrone.render.shader.ShaderProgram;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.util.math.vector.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

import java.util.*;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class LapRender {
  public static int pausedShader = 0;
  public static ShaderProgram gateShader = null;
  
  @SubscribeEvent
  public static void handleOverlayRender(RenderGameOverlayEvent.Text event) {
    Minecraft minecraft = Minecraft.getInstance();
    LocalPlayer player = minecraft.player;
    UUID currentUserId = player.getUUID();
    if (!RaceClient.checkRacingMode(currentUserId)) {
      return;
    }
    
    ArrayList<String> right = event.getRight();
    UUID trackId = RaceClient.userToTrack.get(currentUserId);
    SerialRaceTrack serialRaceTrack = RaceClient.loadedTracks.get(trackId);
    HashMap<UUID, Boolean> allCurrentRacers = RaceClient.trackToUsers.get(
      trackId);
    ArrayList<LapTime> bestTimes = RaceClient.trackBestTimes.get(trackId);
    
    if (right == null ||
        trackId == null ||
        serialRaceTrack == null ||
        allCurrentRacers == null) {
      return;
    }
    
    right.add("Track: " + serialRaceTrack.name);
    right.add("");
    right.add("Leaderboard:");
    
    boolean found = false;
    
    // render best times for the track:
    if (bestTimes != null) {
      for (LapTime lapTime : bestTimes) {
        int bestTimeMillis = lapTime.millis;
        String fTime = RaceClient.formatTime(bestTimeMillis);
        String playerName = Main.getPlayerNameFromUuid(lapTime.userId);
        if (playerName == null) continue;
        right.add(playerName + ": " + fTime);
        
        if (lapTime.userId.equals(currentUserId)) {
          found = true;
        }
      }
    }
    
    right.add("");
    
    // Render the current player's best time if they aren't in the top list:
    if (!found) {
      right.add("Your time:");
      Integer bestTimeMillis = RaceClient.userBestTime.get(currentUserId);
      if (bestTimeMillis != null) {
        String playerName = Main.getPlayerNameFromUuid(currentUserId);
        if (playerName != null) {
          String fTime = RaceClient.formatTime(bestTimeMillis);
          right.add(playerName + ": " + fTime);
        }
      }
    }
    
    // Render the current players ticking timer:
    Long currentLapMillis = RaceClient.userStartTime.get(currentUserId);
    if (currentLapMillis != null) {
      long elapsedMillis = System.currentTimeMillis() - currentLapMillis;
      String fTime = RaceClient.formatTime((int) elapsedMillis);
      right.add(fTime);
    }
    
    // Render the gate index:
    //    Integer gateIndex = RaceClient.userGateIndex.get(currentUserId);
    //    if (gateIndex != null) {
    //      right.add((gateIndex + 1) + " of " + serialRaceTrack.gates.size());
    //    }
  }
  
  //  @SubscribeEvent
  //  public static void handleOverlayRender(RenderGameOverlayEvent.Pre event) {
  //    Minecraft minecraft = Minecraft.getInstance();
  //    LocalPlayer player = minecraft.player;
  //    if (!InputHandler.arm) {
  //      return;
  //    }
  //
  //    PoseStack matrix = event.getPoseStack();
  //    int colorWhite = Main.getColorIntFromHex("#ffffff");
  //    matrix.pushPose();
  //
  //    AbstractGui.fill(matrix, -2, 0, 1, 1, colorWhite);
  //    matrix.popPose();
  //  }
  
  @SubscribeEvent
  public static void handleLogOut(ClientPlayerNetworkEvent.LoggedOutEvent event) {
    try {
      Entity entity = event.getPlayer();
      UUID userId = entity.getUUID();
      UUID trackId = RaceClient.userToTrack.get(userId);
      RaceClient.handleStopRacing(trackId, userId);
    } catch (Exception e) {
      Main.LOGGER.debug(e.getMessage());
    }
  }
  
  @SubscribeEvent
  public static void onLoggedIn(WorldEvent.Load event) {
    
  }
  
  public static void loadCustomShader() {
    if (gateShader == null) {
      ShaderObject vertexShader = ShaderObject.createShader(
        GL20.GL_VERTEX_SHADER,
        Resource.shaders_build_gate_vertex
      );
      ShaderObject fragmentShader = ShaderObject.createShader(
        GL20.GL_FRAGMENT_SHADER,
        Resource.shaders_build_gate_fragment
      );
      gateShader = new ShaderProgram(vertexShader, fragmentShader);
    }
    // Store the current shader program
    int[] out = new int[1];
    GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, out);
    pausedShader = out[0];
    // Switch to custom shader program
    //    Main.LOGGER.debug("using custom shader");
    GL20.glUseProgram(gateShader.program);
  }
  
  public static void unloadCustomShader() {
    GL20.glUseProgram(pausedShader);
    pausedShader = 0;
  }
  
  //  @SubscribeEvent
  //  public static void h(RenderWorldLastEvent event) {
  //    Entity player = Minecraft.getInstance().getEntity();
  //    if (player == null) return;
  //
  //    if (gateShader == null) {
  //      ShaderObject vertexShader = ShaderObject.createShader(GL20.GL_VERTEX_SHADER, Resource.shaders_build_gate_vertex);
  //      ShaderObject fragmentShader = ShaderObject.createShader(GL20.GL_FRAGMENT_SHADER, Resource.shaders_build_gate_fragment);
  //      gateShader = new ShaderProgram(vertexShader, fragmentShader);
  //    }
  //    // Store the current shader program
  //    int[] out = new int[1];
  //    GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, out);
  //    // Switch to custom shader program
  //    GL20.glUseProgram(gateShader.program);
  //
  //    Vec3 eyePosMC = player.getEyePosition(event.getPartialTicks());
  //    com.jme3.math.Vector3f eyePos = new com.jme3.math.Vector3f((float) eyePosMC.x, (float) eyePosMC.y, (float) eyePosMC.z);
  //
  //    Tessellator tessellator = Tessellator.getInstance();
  //    BufferBuilder buffer = tessellator.getBuilder();
  ////    BuildModeRenderer.applyLineMode();
  ////    Minecraft.getInstance().getFramebuffer().bindFramebuffer(false);
  //
  //    PoseStack stack = event.getPoseStack();
  //    stack.pushPose();
  //
  ////    buffer.begin( GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR );
  ////
  ////
  //////    BlockPos pos = new BlockPos(0, 0, -10);
  //////    stack.translate(pos.getX(), pos.getY(), pos.getZ());
  //////    stack.translate(-eyePos.x, -eyePos.y, -eyePos.z);
  //    Matrix4f matrix = stack.last().pose();
  ////    renderBlockLines(matrix, buffer, new BlockPos(0, 0, 0));
  ////    tessellator.end();
  //
  //
  //    float red = 1.0f;
  //    float green = 0f;
  //    float blue = 0f;
  //    float z = -5;
  //
  ////    GL11.glBegin(GL11.GL_QUADS);
  ////    GL11.glColor3f(red, green, blue);
  ////    GL11.glVertex3f(-1, -1, z);
  ////    GL11.glColor3f(red, green, blue);
  ////    GL11.glVertex3f(1, -1, z);
  ////    GL11.glColor3f(red, green, blue);
  ////    GL11.glVertex3f(1, 1, z);
  ////    GL11.glColor3f(red, green, blue);
  ////    GL11.glVertex3f(-1, 1, z);
  ////    GL11.glEnd();
  //
  //        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
  //    buffer.vertex(matrix, -1, -1, z).color(red, green, blue, 1.0f).endVertex();
  //    buffer.vertex(matrix, 1, -1, z).color(red, green, blue, 1.0f).endVertex();
  //    buffer.vertex(matrix, 1, 1, z).color(red, green, blue, 1.0f).endVertex();
  //    buffer.vertex(matrix, -1, 1, z).color(red, green, 1, 1.0f).endVertex();
  //
  ////    buffer.vertex(matrix, -1, 1, z).color(red, green, blue, 1.0f);
  ////    buffer.vertex(matrix, 1, 1, z).color(red, green, blue, 1.0f);
  ////    buffer.vertex(matrix, 1, -1, z).color(red, green, blue, 1.0f);
  ////    buffer.vertex(matrix, -1, -1, z).color(red, green, blue, 1.0f);
  //
  //
  //
  //
  //    tessellator.end();
  //
  //    // Restore previous shader program
  //    GL20.glUseProgram(out[0]);
  //
  //    stack.popPose();
  ////    BuildModeRenderer.cleanLineMode();
  //  }
  
  @SubscribeEvent
  public static void handleGateRendering(RenderGameOverlayEvent.Pre event) {
    PlayerEntity self = Minecraft.getInstance().player;
    if (self == null) return;
    UUID selfUserId = self.getUUID();
    UUID currentTrackId = RaceClient.currentTrackId.get(selfUserId);
    if (currentTrackId == null) return;
    SerialRaceTrack track = RaceClient.loadedTracks.get(currentTrackId);
    if (track == null) return;
    Integer gateIndex = RaceClient.userGateIndex.get(selfUserId);
    if (gateIndex == null) return;
    SerialRaceGate gate = null;
    if (gateIndex < track.gates.size()) {
      gate = track.gates.get(gateIndex);
    }
    if (gate == null) return;
    Entity player = Minecraft.getInstance().getCameraEntity();
    if (player == null) return;
    
    MainWindow mainWindow = Minecraft.getInstance().getWindow();
    int scaledWidth = mainWindow.getGuiScaledWidth();
    int scaledHeight = mainWindow.getGuiScaledHeight();
    
    
    Vec3 eyePosMC = player.getEyePosition(event.getPartialTicks());
    com.jme3.math.Vector3f eyePos = new com.jme3.math.Vector3f(
      (float) eyePosMC.x,
      (float) eyePosMC.y,
      (float) eyePosMC.z
    );
    
    PoseStack stack = event.getPoseStack();
    
    
    stack.pushPose();
    
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    applyQuadMode();
    
    
    stack.pushPose();
    buffer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_COLOR);
    
    com.jme3.math.Vector3f center = gate.getCenter();
    //    com.jme3.math.Vector3f posf = new com.jme3.math.Vector3f(center.getX(), center.getY(), center.getZ());
    //    com.jme3.math.Vector3f diff = posf.subtract(eyePos);
    //    Vector3f diffMC = new Vector3f(diff.x, diff.y, diff.z);
    ////    Vector3f diffMC = new Vector3f(0, 0, -1);
    //
    //    // diff is the position of the gate relative to the eye position.
    //    // It must be rotated the same amount as the eye rotation
    //    ActiveRenderInfo activerenderinfo = Minecraft.getInstance().gameRenderer.getMainCamera();
    ////    Quaternion cameraRot = activerenderinfo.getRotation();
    ////    diffMC.transform(cameraRot);
    //    diffMC.transform(Vector3f.YP.rotationDegrees(activerenderinfo.getYRot() + 180.0F));
    //    diffMC.transform(Vector3f.XP.rotationDegrees(activerenderinfo.getXRot()));
    //    diffMC.transform(Vector3f.ZP.rotationDegrees(InputHandler.arm ? InputHandler.cameraRoll : 0));
    //    // Now diff is a vector pointing from the camera to the gate in view space.
    //
    //    boolean backwards = diffMC.getZ() > 0;
    //
    ////    RenderSystem.ortho(
    ////        0.0D,
    ////        (double)mainwindow.getFramebufferWidth() / mainwindow.getGuiScaleFactor(),
    ////        (double)mainwindow.getFramebufferHeight() / mainwindow.getGuiScaleFactor(),
    ////        0.0D,
    ////        1000.0D,
    ////        3000.0D);
    //
    //    Matrix4f projM = Minecraft.getInstance().gameRenderer.getProjectionMatrix(activerenderinfo, event.getPartialTicks(), true);
    //    Vector4f d4 = new Vector4f(diffMC.getX(), diffMC.getY(), diffMC.getZ(), 1.0f);
    //    d4.transform(projM);
    //    // Now diff is in clip space.
    //
    //    d4.perspectiveDivide();
    //    // now diff is NDC.
    //
    //
    //
    //    d4.mul(new Vector3f(scaledWidth / 2, scaledHeight / 2, 1));
    
    //    diffMC.mul(50);
    
    
    //    Main.LOGGER.info("diff: " + diff);
    
    //    com.jme3.math.Vector2f ssGatePos = new com.jme3.math.Vector2f(d4.getX(), d4.getY());
    //
    //    if (backwards) {
    //      // multLocal(1000) ensures the gatePos appear off screen;
    //      ssGatePos.multLocal(-1).multLocal(1000);
    //    }
    
    com.jme3.math.Vector2f ssGatePos = RenderingHelper.getScreenSpacePos(
      center,
      player,
      event.getPartialTicks()
    );
    
    float averageSize = (mainWindow.getGuiScaledWidth() +
                         mainWindow.getGuiScaledHeight()) / 2f;
    float size = 0.025f * averageSize;
    //    float padding = size;
    //    float maxX = (scaledWidth / 2) - padding;
    //    float minX = -(scaledWidth / 2) + padding;
    //    float maxY = (scaledHeight / 2) - padding;
    //    float minY = -(scaledHeight / 2) + padding;
    //    if (ssGatePos.x > maxX) {
    //      float length = new com.jme3.math.Vector2f(ssGatePos.x, 0).length();
    //      float ratio = FastMath.abs(maxX) / length;
    //      ssGatePos.multLocal(ratio);
    //    }
    //    if (ssGatePos.x < minX) {
    //      float length = new com.jme3.math.Vector2f(ssGatePos.x, 0).length();
    //      float ratio = FastMath.abs(minX) / length;
    //      ssGatePos.multLocal(ratio);
    //    }
    //    if (ssGatePos.y > maxY) {
    //      float length = new com.jme3.math.Vector2f(0, ssGatePos.y).length();
    //      float ratio = FastMath.abs(maxY) / length;
    //      ssGatePos.multLocal(ratio);
    //    }
    //    if (ssGatePos.y < minY) {
    //      float length = new com.jme3.math.Vector2f(0, ssGatePos.y).length();
    //      float ratio = FastMath.abs(minY) / length;
    //      ssGatePos.multLocal(ratio);
    //    }
    
    stack.translate(
      (float) (scaledWidth / 2),
      (float) (scaledHeight / 2),
      0
    );
    stack.scale(1, -1, 1);
    stack.translate(ssGatePos.getX(), ssGatePos.getY(), 0);
    //    stack.scale((float) mainWindow.getGuiScaleFactor(), (float) mainWindow.getGuiScaleFactor(), 1);
    
    Matrix4f matrix = stack.last().pose();
    
    //    buffer.vertex(matrix, 0, 0.2f, -10).color(1f, 0, 0, 1f).endVertex();
    //    buffer.vertex(matrix, 0, 0.2f, 10).color(1f, 0f, 0f, 1f).endVertex();
    //    buffer.vertex(matrix,0.0f, 0.0f, 0.0f).color(1f, 0, 0, 1).endVertex();
    //    buffer.vertex(matrix,0.0f, 10.0f, 0.0f).color(1f, 0, 0, 1).endVertex();
    //    buffer.vertex(matrix,d4.getX(), d4.getY(), d4.getZ()).color(1f, 0, 0, 1).endVertex();
    
    
    int n = 4;
    com.jme3.math.Vector3f radial = new com.jme3.math.Vector3f(
      1,
      1,
      0
    ).normalizeLocal().multLocal(size);
    com.jme3.math.Vector3f normal = new com.jme3.math.Vector3f(
      0,
      0,
      (float) -1
    ).normalizeLocal();
    com.jme3.math.Quaternion quaternion = (new com.jme3.math.Quaternion()).fromAngleNormalAxis(
      -FastMath.TWO_PI / (n * 1f),
      normal
    );
    for (int i = 0; i < n + 1; i++) {
      buffer.vertex(
        matrix,
        radial.x * 0.45f,
        radial.y * 0.45f,
        radial.z * 0.45f
      ).color(255 / 255f, 209 / 255f, 102 / 255f, 1f).endVertex();
      buffer.vertex(matrix, radial.x, radial.y, radial.z).color(
        23 / 255f,
        32 / 255f,
        40 / 255f,
        1f
      ).endVertex();
      radial = quaternion.multLocal(radial);
    }
    
    //    VoxelShape shape = gate.getShape();
    //    shape.forAllEdges((x1, y1, z1, x2, y2, z2) -> {
    //      float a1 = (float) x1;
    //      float b1 = (float) y1;
    //      float c1 = (float) z1;
    //      float a2 = (float) x2;
    //      float b2 = (float) y2;
    //      float c2 = (float) z2;
    //      buffer.vertex(matrix, a1, b1, c1).color(red, green, blue, opacity).endVertex();
    //      buffer.vertex(matrix, a2, b2, c2).color(red, green, blue, opacity).endVertex();
    ////        buffer.vertex(x1, y1, z1).color(red, green, blue, opacity).endVertex();
    ////        buffer.vertex(x2, y2, z2).color(red, green, blue, opacity).endVertex();
    //    });
    
    tessellator.end();
    
    stack.popPose();
    
    cleanQuadMode();
    
    stack.popPose();
  }
  
  
  public static void drawShape(
    PoseStack matrixStackIn,
    VertexConsumer bufferIn,
    VoxelShape shapeIn,
    double xIn,
    double yIn,
    double zIn,
    float red,
    float green,
    float blue,
    float alpha
  ) {
    Matrix4f matrix4f = matrixStackIn.last().pose();
    shapeIn.forAllEdges((p_230013_12_, p_230013_14_, p_230013_16_, p_230013_18_, p_230013_20_, p_230013_22_) -> {
      bufferIn.vertex(
        matrix4f,
        (float) (p_230013_12_ + xIn),
        (float) (p_230013_14_ + yIn),
        (float) (p_230013_16_ + zIn)
      ).uv(0, 0).endVertex();
      bufferIn.vertex(
        matrix4f,
        (float) (p_230013_18_ + xIn),
        (float) (p_230013_20_ + yIn),
        (float) (p_230013_22_ + zIn)
      ).uv(0, 0).endVertex();
    });
  }
  
  private static void renderBlockLines(
    Matrix4f matrix4f,
    VertexConsumer buffer,
    BlockPos originToFarthest
  ) {
    //    final float red = (proDroneBuild.getColor() >> 16 & 0xff) / 255f;
    //    final float green = (proDroneBuild.getColor() >> 8 & 0xff) / 255f;
    //    final float blue = (proDroneBuild.getColor() & 0xff) / 255f;
    final float red = 1f;
    final float green = 1;
    final float blue = 0.333f;
    final float opacity = 1;
    
    float x = originToFarthest.getX() + 1;
    float y = originToFarthest.getY() + 1;
    float z = originToFarthest.getZ() + 1;
    
    // TOP
    buffer.vertex(matrix4f, 0, y, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, y, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, y, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, y, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, y, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, y, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, y, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, y, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    
    // BOTTOM
    buffer.vertex(matrix4f, x, 0, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, 0, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, 0, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, 0, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, 0, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, 0, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, 0, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, 0, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    
    // Edge 1
    buffer.vertex(matrix4f, x, 0, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, y, z)
      .color(red, green, blue, opacity)
      .endVertex();
    
    // Edge 2
    buffer.vertex(matrix4f, x, 0, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, x, y, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    
    // Edge 3
    buffer.vertex(matrix4f, 0, 0, z)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, y, z)
      .color(red, green, blue, opacity)
      .endVertex();
    
    // Edge 4
    buffer.vertex(matrix4f, 0, 0, 0)
      .color(red, green, blue, opacity)
      .endVertex();
    buffer.vertex(matrix4f, 0, y, 0)
      .color(red, green, blue, opacity)
      .endVertex();
  }
  
  private static void drawBlock(
    final BufferBuilder bufferbuilder,
    final double x,
    final double y,
    final double z,
    final float minU,
    final float maxU,
    final float minV,
    final float maxV,
    final double x_size,
    final double y_size,
    final double z_size
  ) {
    // UP
    bufferbuilder.vertex(-x_size + x, y_size + y, -z_size + z)
      .uv(maxU, maxV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, y_size + y, z_size + z)
      .uv(maxU, minV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, y_size + y, z_size + z)
      .uv(minU, minV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, y_size + y, -z_size + z)
      .uv(minU, maxV)
      .endVertex();
    
    // DOWN
    bufferbuilder.vertex(-x_size + x, -y_size + y, z_size + z)
      .uv(minU, minV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, -y_size + y, -z_size + z)
      .uv(minU, maxV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, -y_size + y, -z_size + z)
      .uv(maxU, maxV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, -y_size + y, z_size + z)
      .uv(maxU, minV)
      .endVertex();
    
    // LEFT
    bufferbuilder.vertex(x_size + x, -y_size + y, z_size + z)
      .uv(maxU, minV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, -y_size + y, -z_size + z)
      .uv(maxU, maxV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, y_size + y, -z_size + z)
      .uv(minU, maxV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, y_size + y, z_size + z)
      .uv(minU, minV)
      .endVertex();
    
    // RIGHT
    bufferbuilder.vertex(-x_size + x, -y_size + y, -z_size + z)
      .uv(minU, maxV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, -y_size + y, z_size + z)
      .uv(minU, minV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, y_size + y, z_size + z)
      .uv(maxU, minV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, y_size + y, -z_size + z)
      .uv(maxU, maxV)
      .endVertex();
    
    // BACK
    bufferbuilder.vertex(-x_size + x, -y_size + y, -z_size + z)
      .uv(minU, maxV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, y_size + y, -z_size + z)
      .uv(minU, minV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, y_size + y, -z_size + z)
      .uv(maxU, minV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, -y_size + y, -z_size + z)
      .uv(maxU, maxV)
      .endVertex();
    
    // FRONT
    bufferbuilder.vertex(x_size + x, -y_size + y, z_size + z)
      .uv(maxU, minV)
      .endVertex();
    bufferbuilder.vertex(x_size + x, y_size + y, z_size + z)
      .uv(maxU, maxV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, y_size + y, z_size + z)
      .uv(minU, maxV)
      .endVertex();
    bufferbuilder.vertex(-x_size + x, -y_size + y, z_size + z)
      .uv(minU, minV)
      .endVertex();
  }
  
  //  private static final int GL_FRONT_AND_BACK = 1032;
  //  private static final int GL_LINE = 6913;
  //  private static final int GL_FILL = 6914;
  //  private static final int GL_LINES = 1;
  //  private enum Profile
  //  {
  //    BLOCKS {
  //      @Override
  //      public void apply()
  //      {
  ////        GlStateManager.disableTexture();
  ////        GlStateManager.depthMask(false);
  ////        GL11.glLineWidth(4.0F);
  //
  //
  //
  //        RenderSystem.disableTexture();
  //        RenderSystem.disableDepthTest();
  //        RenderSystem.depthMask( false );
  //        RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_LINES );
  //        RenderSystem.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
  //        RenderSystem.enableBlend();
  //        RenderSystem.lineWidth( 1 );
  //      }
  //
  //      @Override
  //      public void clean()
  //      {
  ////        GL11.glLineWidth(1.0F);
  ////        GlStateManager.depthMask(true);
  ////        GlStateManager.enableTexture();
  //
  //
  //
  //        RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_FILL );
  //        RenderSystem.disableBlend();
  //        RenderSystem.enableDepthTest();
  //        RenderSystem.depthMask( true );
  //        RenderSystem.enableTexture();
  //      }
  //    },
  //    // TODO:
  //    ENTITIES {
  //      @Override
  //      public void apply()
  //      {}
  //
  //      @Override
  //      public void clean()
  //      {}
  //    };
  //
  //    private Profile() {}
  //    public abstract void apply();
  //    public abstract void clean();
  //  }
  
  public static void applyQuadMode() {
    RenderSystem.shadeModel(GL11.GL_SMOOTH);
  }
  
  public static void cleanQuadMode() {
    RenderSystem.shadeModel(GL11.GL_FLAT);
  }
}
