package com.gluecode.fpvdrone.gui.screen.addon;

import com.gluecode.fpvdrone.gui.screen.FpvScreen;
import com.mojang.blaze3d.matrix.MatrixStack;

public abstract class ScreenAddon {
  public abstract void init(FpvScreen screen);
  
  public abstract void render(
    FpvScreen screen,
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  );
}
