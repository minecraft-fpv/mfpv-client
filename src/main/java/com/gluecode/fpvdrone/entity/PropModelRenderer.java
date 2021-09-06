package com.gluecode.fpvdrone.entity;

import com.gluecode.fpvdrone.Main;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import org.lwjgl.opengl.GL11;

public class PropModelRenderer extends ModelRenderer {
  public int motorNumber;
  public DroneBuild build;
  public float alpha = 1; // Must be set by DroneModel.render
  
  public PropModelRenderer(
    Model model,
    DroneBuild build,
    int motorNumber
  ) {
    super(model);
    this.motorNumber = motorNumber;
    this.build = build;
    //        this.motorWidth = motorWidth;
  }
  
  @Override
  public void compile(
    MatrixStack.Entry matrixEntryIn,
    IVertexBuilder bufferIn,
    int packedLightIn,
    int packedOverlayIn,
    float red,
    float green,
    float blue,
    float ignoredAlpha
  ) {
      //            super.doRender(matrixEntryIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
      Matrix4f matrix4f = matrixEntryIn.pose();
      Matrix3f matrix3f = matrixEntryIn.normal();
      buildBlade(
        matrix4f,
        matrix3f,
        false,
        packedLightIn,
        packedOverlayIn,
        red,
        green,
        blue
      );
      buildBlade(
        matrix4f,
        matrix3f,
        true,
        packedLightIn,
        packedOverlayIn,
        red,
        green,
        blue
      );
  }
  
  /*
   * All physical parameters are in SI units.
   * */
  public void buildBlade(
    Matrix4f matrix4f,
    Matrix3f matrix3f,
    boolean flip,
    int packedLightIn,
    int packedOverlayIn,
    float red,
    float green,
    float blue
  ) {
    if (alpha == 0) return;
    
    float bladeLength = build.bladeLength;
    float bladeWidth = build.bladeWidth;
    float motorWidth = build.motorWidth;
    
    // The blade is built in object space align to the Z.
    // Starts at Z = 0;
    // The blade is built as a triangle strip with vertices on each side interleaved.
    
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    
    buffer.begin(GL11.GL_TRIANGLE_STRIP, DefaultVertexFormats.NEW_ENTITY);
    
    float scale = 32f;
    
    int n = 11;
    for (int i = -2; i < n; i++) {
      float z;
      if (i == -2) {
        z = 0;
      } else {
        z = (i + 1f) / ((float) n) * bladeLength;
      }

      float pitch = FastMath.atan2(
        bladeLength * 2f,
        2f * FastMath.PI * z
      );
      float x = bladeWidth / 2f * FastMath.cos(pitch);
      float y = bladeWidth / 2f * FastMath.sin(pitch);
      
      if (motorNumber % 2 == 0) {
        x *= -1;

        if (!flip && i == -2) continue;

        if (i == -2 || i % 2 == 0) {
          // alternate between left and right side of blade
          // to form triangle strip
          x *= -1;
          y *= -1;
        }
      } else {
        if (flip && i == -2) continue;

        if (i == -2 || i % 2 == 0) {
          // alternate between left and right side of blade
          // to form triangle strip
          x *= -1;
          y *= -1;
        }
      }
      
//      if (!flip) {
//        // Render the underside
//        x *= -1;
//        y *= -1;
//      }s
      x *= -1;
      y *= -1;
      
      
      // Between motorWidth and 0 radius, y value interpolates to 0.
      // Note that motorWidth is twice as big as motorRadius
      if (0 <= z && z < motorWidth) {
        float yscale = z / (motorWidth);
        y *= yscale;
      }
      
      y -= build.motorHeight; // motorHeight. matrix4f already includes bladeThickness.
      
//      if (flip) {
//        y -= 0.001f; // bias so that top and bottom do not overlap.
//      }
      
      Vector4f p = new Vector4f(
        x * scale / 16f,
        y * scale / 16f,
        z / 16f * scale,
        1.0F
      );
      p.transform(matrix4f);

//      float bpitch = i < 2 ? FastMath.atan2(
//          bladeLength * 2f,
//          2f * FastMath.PI * (z + ((float) n) * bladeLength / 2f)
//      ) : pitch;
      float nx = -FastMath.sin(pitch);
      float ny = FastMath.cos(pitch);
      if (motorNumber % 2 == 0) {
        nx *= -1;
      }
      if (flip) {
        nx *= -1;
        ny *= -1;
      }
      Vector3f vector3f = new Vector3f(nx, ny, 0);
      vector3f.transform(matrix3f);
      float n0 = vector3f.x();
      float n1 = vector3f.y();
      float n2 = vector3f.z();
      
      buffer.vertex(
        p.x(),
        p.y(),
        p.z(),
        red,
        green,
        blue,
        alpha,
        0,
        0,
        packedOverlayIn,
        packedLightIn,
        n0,
        n1,
        n2
      );
    }
  
    boolean originalDepthTest = GL11.glGetBoolean(GL11.GL_DEPTH_TEST);
    boolean originalCull = GL11.glGetBoolean(GL11.GL_CULL_FACE);
//    boolean originalAlphaTest = GL11.glGetBoolean(GL11.GL_ALPHA_TEST);
//    boolean originalBlendMode = GL11.glGetBoolean(GL11.GL_BLEND);
    
    
    RenderSystem.enableDepthTest();
    RenderSystem.enableCull();
    RenderSystem.enableBlend();
    RenderSystem.blendFuncSeparate(
      GlStateManager.SourceFactor.SRC_ALPHA,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
      GlStateManager.SourceFactor.ONE,
      GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
    );
    RenderHelper.turnBackOn();
    Minecraft.getInstance().gameRenderer.lightTexture().turnOnLightLayer();
    Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor();
    
    tessellator.end();
    
    if (!originalDepthTest) {
      RenderSystem.disableDepthTest();
    }
    if (!originalCull) {
      RenderSystem.disableCull();
    }
    RenderSystem.disableBlend();
    RenderSystem.defaultBlendFunc();
    RenderHelper.turnOff();
    Minecraft.getInstance().gameRenderer.lightTexture()
      .turnOffLightLayer();
    Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor();
  }
}
