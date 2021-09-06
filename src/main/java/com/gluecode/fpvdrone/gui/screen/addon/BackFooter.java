package com.gluecode.fpvdrone.gui.screen.addon;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.screen.FpvScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.WizardConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class BackFooter extends ScreenAddon {
  public void handleDone(FpvScreen screen) {
    screen.getMinecraft().setScreen(screen.previousScreen);
  }
  
  @Override
  public void init(FpvScreen screen) {
    screen.addButton(new Button(
      WizardConfig.left,
      screen.height - 20 - WizardConfig.footerBottom,
      WizardConfig.wideButtonWidth,
      20,
      new StringTextComponent(I18n.get("gui.back")),
      (Button button) -> this.handleDone(screen)
    ));
  }
  
  @Override
  public void render(
    FpvScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
  ) {
  
  }
}
