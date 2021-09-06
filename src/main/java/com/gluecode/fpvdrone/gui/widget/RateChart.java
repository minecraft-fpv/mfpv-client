package com.gluecode.fpvdrone.gui.widget;

import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.render.StickOverlayRenderer;
import com.gluecode.fpvdrone.util.Transforms;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;

public class RateChart extends Widget {
  private boolean showYaw;
  private boolean showPitch;
  private boolean showRoll;
  
  public RateChart(
    int x,
    int y,
    int width,
    int height
  ) {
    super(x, y, width, height, new StringTextComponent("Rates"));
    this.showYaw = true;
    this.showPitch = true;
    this.showRoll = true;
    this.active = false;
  }
  
  public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

    matrixStack.pushPose();
    matrixStack.translate(this.x, this.y + this.height, 0);
    matrixStack.scale(this.width, -this.height, 1);
    
    Matrix4f matrix = matrixStack.last().pose();
    
    // Render curve:
    float yawMax = 0;
    float pitchMax = 0;
    float rollMax = 0;
    if (this.showYaw) {
      float crate = ControllerConfig.getYawRate();
      float csuper = ControllerConfig.getYawSuper();
      float cexpo = ControllerConfig.getYawExpo();
      yawMax = Transforms.bfRate(
        1,
        crate,
        csuper,
        cexpo
      );
    }
    if (this.showPitch) {
      float crate = ControllerConfig.getPitchRate();
      float csuper = ControllerConfig.getPitchSuper();
      float cexpo = ControllerConfig.getPitchExpo();
      pitchMax = Transforms.bfRate(
        1,
        crate,
        csuper,
        cexpo
      );
    }
    if (this.showRoll) {
      float crate = ControllerConfig.getRollRate();
      float csuper = ControllerConfig.getRollSuper();
      float cexpo = ControllerConfig.getRollExpo();
      rollMax = Transforms.bfRate(
        1,
        crate,
        csuper,
        cexpo
      );
    }
    float max = Math.max(yawMax, Math.max(pitchMax, rollMax));
    
    if (this.showYaw) {
      float setpoint = ControllerReader.getYaw();
      float crate = ControllerConfig.getYawRate();
      float csuper = ControllerConfig.getYawSuper();
      float cexpo = ControllerConfig.getYawExpo();
      this.renderCurve(matrix, buffer, setpoint, crate, csuper, cexpo, max, 0xFF8040);
    }
    if (this.showPitch) {
      float setpoint = ControllerReader.getPitch();
      float crate = ControllerConfig.getPitchRate();
      float csuper = ControllerConfig.getPitchSuper();
      float cexpo = ControllerConfig.getPitchExpo();
      this.renderCurve(matrix, buffer, setpoint, crate, csuper, cexpo, max, 0x40FF80);
    }
    if (this.showRoll) {
      float setpoint = ControllerReader.getRoll();
      float crate = ControllerConfig.getRollRate();
      float csuper = ControllerConfig.getRollSuper();
      float cexpo = ControllerConfig.getRollExpo();
      this.renderCurve(matrix, buffer, setpoint, crate, csuper, cexpo, max, 0x4080FF);
    }
  
    // Render border
    buffer.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 0, 1, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 1, 0, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 1, 0, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 1, 1, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 0, 1, 0).color(1f, 1f, 1f, 1f).endVertex();
    buffer.vertex(matrix, 1, 1, 0).color(1f, 1f, 1f, 1f).endVertex();
    
    matrixStack.popPose();

    StickOverlayRenderer.applyLineMode();
    tessellator.end();
    StickOverlayRenderer.cleanLineMode();
    
//    super.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  protected void renderBg(MatrixStack matrixStack, Minecraft minecraft, int mouseX, int mouseY) {
  }
  
  private void renderCurve(Matrix4f matrix, BufferBuilder buffer, float setpoint, float crate, float csuper, float cexpo, float max, int color) {
    int red = color >> 16;
    int green = (color & 0x00FF00) >> 8;
    int blue = color & 0x0000FF;
    
    float r = red / 255f;
    float g = green / 255f;
    float b = blue / 255f;
  
    // Render setpoint:
    float sx = Math.max(Math.abs(setpoint), 0);
    float sy = Transforms.bfRate(sx, crate, csuper, cexpo) / max;
    buffer.vertex(matrix, sx, 0, 0).color(r, g, b, 1f).endVertex();
    buffer.vertex(matrix, sx, sy, 0).color(r, g, b, 1f).endVertex();
    buffer.vertex(matrix, sx, sy, 0).color(r, g, b, 1f).endVertex();
    buffer.vertex(matrix, 1, sy, 0).color(r, g, b, 1f).endVertex();
    
    int n = 100;
    for (int i = 1; i < n; i++) {
      float pxprev = (i - 1f) / (n - 1f);
      float px = 1f * i / (n - 1f);
      
      float pyprev = Transforms.bfRate(pxprev, crate, csuper, cexpo) / max;
      float py = Transforms.bfRate(px, crate, csuper, cexpo) / max;
    
      buffer.vertex(matrix, pxprev, pyprev, 0).color(r, g, b, 1f).endVertex();
      buffer.vertex(matrix, px, py, 0).color(r, g, b, 1f).endVertex();
    }
  }
}
