package com.gluecode.fpvdrone.render;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.Resource;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.render.shader.ShaderObject;
import com.gluecode.fpvdrone.render.shader.ShaderProgram;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class BarrelRenderer {
  private static final int GL_FRONT_AND_BACK = 1032;
  private static final int GL_LINE = 6913;
  private static final int GL_FILL = 6914;
  private static final int GL_LINES = 1;
  public static int pausedShader = 0;
  public static ShaderProgram barrelShader = null;
  public static ShaderProgram copyShader = null;
  
  public static Framebuffer targetFramebuffer = null;
  public static int framebufferTexture;
  
  public static void loadCopyShader() {
    if (copyShader == null) {
      ShaderObject vertexShader = ShaderObject.createShader(
        GL20.GL_VERTEX_SHADER,
        Resource.shaders_barrel_vertex
      );
      ShaderObject fragmentShader = ShaderObject.createShader(
        GL20.GL_FRAGMENT_SHADER,
        Resource.shaders_copy_fragment
      );
      copyShader = new ShaderProgram(vertexShader, fragmentShader);
    }
    // Store the current shader program
    int[] out = new int[1];
    GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, out);
    pausedShader = out[0];
    // Switch to custom shader program
    //    Main.LOGGER.debug("using custom shader");
    GL20.glUseProgram(copyShader.program);
  }
  
  public static void loadCustomShader() {
    if (barrelShader == null) {
      ShaderObject vertexShader = ShaderObject.createShader(
        GL20.GL_VERTEX_SHADER,
        Resource.shaders_barrel_vertex
      );
      ShaderObject fragmentShader = ShaderObject.createShader(
        GL20.GL_FRAGMENT_SHADER,
        Resource.shaders_barrel_fragment
      );
      barrelShader = new ShaderProgram(vertexShader, fragmentShader);
    }
    // Store the current shader program
    int[] out = new int[1];
    GL11.glGetIntegerv(GL20.GL_CURRENT_PROGRAM, out);
    pausedShader = out[0];
    // Switch to custom shader program
    //    Main.LOGGER.debug("using custom shader");
    GL20.glUseProgram(barrelShader.program);
  }
  
  public static void unloadCustomShader() {
    GL20.glUseProgram(pausedShader);
    pausedShader = 0;
  }
  
  public static void createAndBindTexture() {
    Framebuffer defaultFramebuffer = Minecraft.getInstance()
      .getMainRenderTarget();
    int width = defaultFramebuffer.width;
    int height = defaultFramebuffer.height;
    
    boolean widthChanged = targetFramebuffer != null &&
                           (targetFramebuffer.width !=
                            width);
    boolean heightChanged = targetFramebuffer != null &&
                            (targetFramebuffer.height !=
                             height);
    boolean resize = widthChanged || heightChanged;
    if (targetFramebuffer == null || resize) {
      
      Main.LOGGER.debug("Creating custom framebuffer. Resize: " + resize);
      
      if (resize) {
        targetFramebuffer.destroyBuffers();
        GL11.glDeleteTextures(framebufferTexture);
      }
      
      // Create a texture:
      framebufferTexture = GL11.glGenTextures();
      
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferTexture);
      GL11.glTexImage2D(
        GL11.GL_TEXTURE_2D,
        0,
        GL11.GL_RGBA8,
        width,
        height,
        0,
        GL11.GL_RGBA,
        GL11.GL_UNSIGNED_BYTE,
        (ByteBuffer) null
      );
      GL11.glTexParameteri(
        GL11.GL_TEXTURE_2D,
        GL11.GL_TEXTURE_MIN_FILTER,
        GL11.GL_NEAREST
      );
      GL11.glTexParameteri(
        GL11.GL_TEXTURE_2D,
        GL11.GL_TEXTURE_MAG_FILTER,
        GL11.GL_NEAREST
      );
      GL11.glTexParameteri(
        GL11.GL_TEXTURE_2D,
        GL11.GL_TEXTURE_WRAP_S,
        GL11.GL_CLAMP
      );
      GL11.glTexParameteri(
        GL11.GL_TEXTURE_2D,
        GL11.GL_TEXTURE_WRAP_T,
        GL11.GL_CLAMP
      );
      GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
      
      targetFramebuffer = new Framebuffer(width, height, false, false);
      targetFramebuffer.bindWrite(false);
      GL11.glViewport(
        0,
        0,
        targetFramebuffer.width,
        targetFramebuffer.height
      );
      GL30.glFramebufferTexture2D(
        GL30.GL_FRAMEBUFFER,
        GL30.GL_COLOR_ATTACHMENT0,
        GL11.GL_TEXTURE_2D,
        framebufferTexture,
        0
      );
    } else {
      targetFramebuffer.bindWrite(false);
      GL11.glViewport(
        0,
        0,
        targetFramebuffer.width,
        targetFramebuffer.height
      );
    }
  }
  
  /*
  Takes whatever has currently been rendered in vanilla
  and saves it in our custom framebufferTexture
  * */
  public static void saveRenderPass() {
    Framebuffer defaultFramebuffer = Minecraft.getInstance()
      .getMainRenderTarget();
    int defaultFramebufferTexture = defaultFramebuffer.getColorTextureId();
    
    int[] currentFrameBuffer = new int[1];
    GL11.glGetIntegerv(GL30.GL_FRAMEBUFFER_BINDING, currentFrameBuffer);
    
    int[] currentFramebufferTexture = new int[1];
    GL30.glGetFramebufferAttachmentParameteriv(
      GL30.GL_FRAMEBUFFER,
      GL30.GL_COLOR_ATTACHMENT0,
      GL30.GL_FRAMEBUFFER_ATTACHMENT_OBJECT_NAME,
      currentFramebufferTexture
    );
    
    createAndBindTexture();
    
    boolean originalAlphaTest = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
    boolean originalBlendMode = GL11.glGetBoolean(GL11.GL_BLEND);
    RenderSystem.disableAlphaTest();
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(
      GlStateManager.SourceFactor.ONE,
      GlStateManager.DestFactor.ZERO,
      GlStateManager.SourceFactor.SRC_ALPHA,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
    );
    
    // Setup GL:
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    GL11.glOrtho(-1, 1, -1, 1, -1, 1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    
    loadCopyShader();
    
    // Draw to the target framebuffer using the defaultFramebufferTexture as input.
    //    FogRenderer.updateFogColor(Minecraft.getInstance().gameRenderer.getMainCamera(), 0, Minecraft.getInstance().level, Minecraft.getInstance().gameSettings.renderDistanceChunks, Minecraft.getInstance().gameRenderer.getBossColorModifier(0));
    //    GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentFramebufferTexture[0]);
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, defaultFramebufferTexture);
    GL11.glBegin(GL11.GL_QUADS);
    {
      GL11.glTexCoord2f(0, 0);
      GL11.glVertex2f(-1, -1);
      GL11.glTexCoord2f(1, 0);
      GL11.glVertex2f(1, -1);
      GL11.glTexCoord2f(1, 1);
      GL11.glVertex2f(1, 1);
      GL11.glTexCoord2f(0, 1);
      GL11.glVertex2f(-1, 1);
    }
    GL11.glEnd();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    
    unloadCustomShader();
    
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPopMatrix();
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPopMatrix();
    
    //    MainWindow mainwindow = Minecraft.getInstance().getWindow();
    //    GL11.glMatrixMode(GL11.GL_PROJECTION);
    //    RenderSystem.loadIdentity();
    //    RenderSystem.ortho(0.0D, (double)mainwindow.getFramebufferWidth() / mainwindow.getGuiScaleFactor(), (double)mainwindow.getFramebufferHeight() / mainwindow.getGuiScaleFactor(), 0.0D, 1000.0D, 3000.0D);
    //    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    //    RenderSystem.loadIdentity();
    //    RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
    
    //    GlStateManager.bindFramebuffer(FramebufferConstants.GL_FRAMEBUFFER, currentFrameBuffer[0]);
    //    GlStateManager.viewport(0, 0, this.framebufferWidth, this.framebufferHeight);
    
    if (originalAlphaTest) {
      RenderSystem.enableAlphaTest();
    }
    if (!originalBlendMode) {
      RenderSystem.disableBlend();
      RenderSystem.defaultBlendFunc();
    }
    
    defaultFramebuffer.bindWrite(true); // restore original framebuffer
  }
  
  public static void runShader() {
    loadCustomShader();
    
    // Neeed to show crosshair:
    boolean originalDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
    RenderSystem.disableDepthTest();
    
    double fov = Minecraft.getInstance().options.fov;
    int fovUniform = GL20.glGetUniformLocation(barrelShader.program, "fov");
    GL20.glUniform1f(fovUniform, ((float) fov) * FastMath.PI / 180f);
    
    int aspectUniform = GL20.glGetUniformLocation(
      barrelShader.program,
      "aspect"
    );
    GL20.glUniform1f(
      aspectUniform,
      1.0f * targetFramebuffer.width /
      targetFramebuffer.height
    );
    
    int inTexUniform = GL20.glGetUniformLocation(
      barrelShader.program,
      "inTex"
    );
    GL20.glUniform1i(inTexUniform, 0);
    
    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    GL11.glOrtho(-1, 1, -1, 1, -1, 1);
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPushMatrix();
    GL11.glLoadIdentity();
    
    // we drew to this texture earlier. Now are using it to draw back to the original framebuffer, but we are using a custom shader to do so.
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, framebufferTexture);
    GL11.glBegin(GL11.GL_QUADS);
    {
      GL11.glTexCoord2f(0, 0);
      GL11.glVertex2f(-1, -1);
      GL11.glTexCoord2f(1, 0);
      GL11.glVertex2f(1, -1);
      GL11.glTexCoord2f(1, 1);
      GL11.glVertex2f(1, 1);
      GL11.glTexCoord2f(0, 1);
      GL11.glVertex2f(-1, 1);
    }
    GL11.glEnd();
    GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    
    GL11.glMatrixMode(GL11.GL_PROJECTION);
    GL11.glPopMatrix();
    GL11.glMatrixMode(GL11.GL_MODELVIEW);
    GL11.glPopMatrix();
    
    // Neeed to show crosshair:
    if (originalDepthTest) {
      RenderSystem.enableDepthTest();
    }
    
    unloadCustomShader();
  }
  
  //  public void drawOverlay(float partialTicks) {
  //    if (getFullscreenGui()) {
  //      mc.gameSettings.hideGUI = hideGui;
  //      mc.screen = currentScreen;
  //
  //      if (!hideGui) {
  //        if (mc.entityRenderer.renderHand) {
  //          GlStateManager.enableDepth();
  //          mc.entityRenderer.renderHand(partialTicks, 2);
  //        }
  //        final ScaledResolution scaledresolution = new ScaledResolution(this.mc);
  //        int i1 = scaledresolution.getGuiScaledWidth();
  //        int j1 = scaledresolution.getGuiScaledHeight();
  //        GlStateManager.alphaFunc(516, 0.1F);
  //        mc.entityRenderer.setupOverlayRendering();
  //        mc.entityRenderer.renderItemActivation(i1, j1, partialTicks);
  //        this.mc.ingameGUI.renderGameOverlay(partialTicks);
  //
  //        if (mc.screen != null) {
  //          GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
  //
  //          final int k1 = Mouse.getX() * i1 / mc.displayWidth;
  //          final int l1 = j1 - Mouse.getY() * j1 / mc.displayHeight - 1;
  //          net.minecraftforge.client.ForgeHooksClient.drawScreen(mc.screen, k1, l1, mc.getTickLength());
  //        }
  //      }
  //    }
  //  }
  
  //  @SubscribeEvent(priority = EventPriority.LOWEST)
  //  public static void test(RenderBlockOverlayEvent event) {
  //    if (event.getOverlayType() == RenderBlockOverlayEvent.OverlayType.WATER) {
  //      Main.LOGGER.info("block overlay");
  //      Main.LOGGER.info("GL11.glGetBoolean(GL11.GL_DEPTH_TEST): " + GL11.glGetBoolean(GL11.GL_DEPTH_TEST));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL11.GL_ALPHA_TEST): " + GL11.glGetBoolean(GL11.GL_ALPHA_TEST));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL11.GL_BLEND): " + GL11.glGetBoolean(GL11.GL_BLEND));
  //      float[] blendColor = new float[4];
  //      GL11.glGetFloatv(ARBImaging.GL_BLEND_COLOR, blendColor);
  //      Main.LOGGER.info("GL11.glGetBoolean(GL11.GL_BLEND_DST): " + GL11.glGetInteger(GL11.GL_BLEND_DST));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL11.GL_BLEND_SRC): " + GL11.glGetInteger(GL11.GL_BLEND_SRC));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL20.GL_BLEND_DST_ALPHA): " + GL11.glGetInteger(GL20.GL_BLEND_DST_ALPHA));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL20.GL_BLEND_SRC_ALPHA): " + GL11.glGetInteger(GL20.GL_BLEND_SRC_ALPHA));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL20.GL_BLEND_DST_RGB): " + GL11.glGetInteger(GL20.GL_BLEND_DST_RGB));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL20.GL_BLEND_SRC_RGB): " + GL11.glGetInteger(GL20.GL_BLEND_SRC_RGB));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL30.GL_BLEND_EQUATION_RGB): " + GL11.glGetInteger(GL30.GL_BLEND_EQUATION_RGB));
  //      Main.LOGGER.info("GL11.glGetBoolean(GL30.GL_BLEND_EQUATION_ALPHA): " + GL11.glGetInteger(GL30.GL_BLEND_EQUATION_ALPHA));
  //    }
  //  }
  
  @SubscribeEvent(priority = EventPriority.HIGHEST)
  public static void handleGateRendering(RenderGameOverlayEvent.Pre event) {
    if (!ControllerReader.getArm()) return;
    if (!SettingsLoader.currentUseFisheye) return;
    
    if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
    
    //    Minecraft.getInstance().levelRenderer.updateCameraAndRender(event.getMatrixStack(), event.getPartialTicks(), event.getFinishTimeNano(), true, Minecraft.getInstance().gameRenderer.getMainCamera(), Minecraft.getInstance().gameRenderer, Minecraft.getInstance().gameRenderer.lightTexture(), event.getProjectionMatrix());
    
    saveRenderPass();
    runShader();
    
    
    //    loadCustomShader();
    //    unloadCustomShader();
    //    LapRender.loadCustomShader();
    //    LapRender.unloadCustomShader();
  }
  
  private static void renderBoundingBox(
    Matrix4f matrix4f,
    IVertexBuilder buffer,
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
    MatrixStack stack,
    IVertexBuilder buffer,
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
