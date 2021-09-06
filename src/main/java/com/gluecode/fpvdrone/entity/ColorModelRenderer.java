package com.gluecode.fpvdrone.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.Model;
import net.minecraft.client.renderer.model.ModelRenderer;

public class ColorModelRenderer extends ModelRenderer {
  public float red;
  public float green;
  public float blue;
  public float alpha;
  
  public ColorModelRenderer(
    Model model,
    float red,
    float green,
    float blue,
    float alpha
  ) {
    super(model);
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.alpha = alpha;
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
    float alpha
  ) {
    super.compile(
      matrixEntryIn,
      bufferIn,
      packedLightIn,
      packedOverlayIn,
      this.red,
      this.green,
      this.blue,
      this.alpha
    );
  }
}
