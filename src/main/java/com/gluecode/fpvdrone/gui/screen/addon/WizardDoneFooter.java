package com.gluecode.fpvdrone.gui.screen.addon;

import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.screen.FpvScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.WizardConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class WizardDoneFooter extends ScreenAddon {
  private Runnable onDone;
  
  public void overrideDone(Runnable onDone) {
    this.onDone = onDone;
  }
  
  public void handleDone(FpvScreen screen) {
    if (this.onDone != null) {
      this.onDone.run();
      return;
    }
    if (screen.getMinecraft() != null) {
      screen.getMinecraft().setScreen(screen.previousScreen);
    }
  }
  
  public void handleOpenWizard(FpvScreen screen) {
    GuiEvents.openWelcomeScreen(screen);
  }
  
  @Override
  public void init(FpvScreen screen) {
    screen.addButton(new Button(
      WizardConfig.left,
      screen.height - 20 - WizardConfig.footerBottom,
      WizardConfig.wideButtonWidth * 2,
      20,
      new StringTextComponent(I18n.get("fpvdrone.wizard")),
      (Button button) -> this.handleOpenWizard(screen)
    ));
    
    screen.addButton(new Button(
      screen.width - WizardConfig.wideButtonWidth - WizardConfig.right,
      screen.height - 20 - WizardConfig.footerBottom,
      WizardConfig.wideButtonWidth,
      20,
      new StringTextComponent(I18n.get("gui.done")),
      (Button button) -> this.handleDone(screen)
    ));
  }
  
  @Override
  public void render(
    FpvScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
  ) {
  
  }
}
