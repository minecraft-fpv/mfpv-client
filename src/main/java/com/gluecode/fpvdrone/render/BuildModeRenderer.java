package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.race.RaceClient;
import com.gluecode.fpvdrone.race.SerialRaceGate;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class BuildModeRenderer {
  private static final int GL_FRONT_AND_BACK = 1032;
  private static final int GL_LINE = 6913;
  private static final int GL_FILL = 6914;
  private static final int GL_LINES = 1;

  @SubscribeEvent
  public static void handleGateRendering(RenderWorldLastEvent event) {
    if (!RaceClient.isBuildMode) return;

    LapRender.loadCustomShader();

    Entity player = Minecraft.getInstance().getCameraEntity();

    PoseStack stack = event.getPoseStack();

    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    applyLineMode();

    Vec3 eyePosMC = player.getEyePosition(event.getPartialTicks());
    com.jme3.math.Vector3f eyePos = new com.jme3.math.Vector3f(
      (float) eyePosMC.x,
      (float) eyePosMC.y,
      (float) eyePosMC.z
    );

    stack.pushPose();
    stack.translate(-eyePos.x, -eyePos.y, -eyePos.z); // go to center
    for (SerialRaceGate gate : RaceClient.builtGates) {
      stack.pushPose();

      buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
      stack.translate(
        gate.origin.getX(),
        gate.origin.getY(),
        gate.origin.getZ()
      );
      Matrix4f matrix = stack.last().pose();
      renderBoundingBox(
        matrix,
        buffer,
        player,
        event.getPartialTicks(),
        gate.origin,
        gate.farthest.subtract(gate.origin)
      );
      tessellator.end();

      stack.popPose();
    }
    stack.popPose();

    LapRender.unloadCustomShader();

    cleanLineMode();
  }

  public static void renderBoundingBox(
    Matrix4f matrix4f,
    VertexConsumer buffer,
    Entity renderViewEntity,
    float partialTicks,
    BlockPos origin,
    BlockPos originToFarthest
  ) {
    //    final float red = (proDroneBuild.getColor() >> 16 & 0xff) / 255f;
    //    final float green = (proDroneBuild.getColor() >> 8 & 0xff) / 255f;
    //    final float blue = (proDroneBuild.getColor() & 0xff) / 255f;
    final float red = 0.333f;
    final float green = 1;
    final float blue = 0.333f;
    final float opacity = 1;

    float x = originToFarthest.getX() + 1;
    float y = originToFarthest.getY() + 1;
    float z = originToFarthest.getZ() + 1;

    Vector3f originF = new Vector3f(
      origin.getX(),
      origin.getY(),
      origin.getZ()
    );
    Vector3f offsetX = new Vector3f(x, 0, 0);
    Vector3f offsetY = new Vector3f(0, y, 0);
    Vector3f offsetZ = new Vector3f(0, 0, z);

    // TOP
    //    addWorldPos(originF.add(offsetY), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetX).add(offsetY), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetX).add(offsetY), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetX).add(offsetY).add(offsetZ), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetX).add(offsetY).add(offsetZ), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetY).add(offsetZ), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetY).add(offsetZ), stack, buffer, renderViewEntity, partialTicks);
    //    addWorldPos(originF.add(offsetY), stack, buffer, renderViewEntity, partialTicks);

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

  /*
  Assumes the stack is already at the center of the screen.
  * */
  public static void addWorldPos(
    Vector3f worldPos,
    PoseStack stack,
    VertexConsumer buffer,
    Entity renderViewEntity,
    float partialTicks
  ) {
    stack.pushPose();
    Vector2f ssPos = RenderingHelper.getScreenSpacePos(
      worldPos,
      renderViewEntity,
      partialTicks
    );
    stack.translate(ssPos.getX(), ssPos.getY(), 0);
    Matrix4f matrix = stack.last().pose();

    final float red = 0.333f;
    final float green = 1;
    final float blue = 0.333f;
    final float opacity = 1;

    long time = System.currentTimeMillis();
    //    int mt = (int) ((time / 1000) % 1000);
    int mt = 0;
    buffer.vertex(matrix, 0, 0, -mt)
      .color(red, green, blue, opacity)
      .endVertex();

    stack.popPose();
  }

  public static void applyLineMode() {
    //    LapRender.applyQuadMode();

    //    RenderHelper.setupGui3DDiffuseLighting();
    //    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
    //    RenderSystem.enableBlend();
    //    RenderSystem.enableAlphaTest();
    RenderSystem.disableTexture();
    //    RenderSystem.disableDepthTest();
    //    RenderSystem.depthMask( false );
    ////    RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_LINES );
    ////    RenderSystem.blendFunc( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA );
    ////    RenderSystem.enableBlend();
    //    RenderSystem.lineWidth( 1 );
    GL11.glLineWidth(4.0F);
  }

  public static void cleanLineMode() {
    //    LapRender.cleanQuadMode();
    ////    RenderSystem.polygonMode( GL_FRONT_AND_BACK, GL_FILL );
    ////    RenderSystem.disableBlend();
    //    RenderSystem.enableDepthTest();
    GL11.glLineWidth(1.0F);
    //    RenderSystem.depthMask( true );
    RenderSystem.enableTexture();
  }
}
