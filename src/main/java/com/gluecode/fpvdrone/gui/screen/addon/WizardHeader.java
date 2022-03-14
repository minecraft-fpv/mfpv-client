package com.gluecode.fpvdrone.gui.screen.addon;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.screen.FpvScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.HelpQAScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.HelpScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.WelcomeScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.WizardConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.*;

public class WizardHeader extends ScreenAddon {
  @Nullable
  public String title;
  public boolean showHelpButton;
  @Nullable
  public Runnable overrideOnDone;
  
  public LinkedHashMap<String, ITextComponent> qa = null;
  
  public WizardHeader(@Nullable String title, boolean showHelpButton) {
    this.title = title;
    this.showHelpButton = showHelpButton;
  }
  
  public void handleDone() {
    if (this.overrideOnDone != null) {
      this.overrideOnDone.run();
      return;
    }
    if (WelcomeScreen.currentWelcomeScreen != null) {
      WelcomeScreen.currentWelcomeScreen.getMinecraft()
        .setScreen(WelcomeScreen.currentWelcomeScreen);
    }
  }
  
  public void setOverrideOnDone(@Nullable Runnable overrideOnDone) {
    this.overrideOnDone = overrideOnDone;
  }
  
  public void addHelpQA(String question, ITextComponent answer) {
    if (qa == null) {
      qa = new LinkedHashMap<>();
    }
    qa.put(question, answer);
  }
  
  @Override
  public void init(FpvScreen screen) {
    screen.addButton(new Button(
      WizardConfig.left,
      WizardConfig.headerTop,
      20,
      20,
      new StringTextComponent(""),
      (Button button) -> this.handleDone()
    ));
    
    if (this.showHelpButton) {
      screen.addButton(new Button(
        screen.width - WizardConfig.right - WizardConfig.shortButtonWidth,
        WizardConfig.headerTop,
        WizardConfig.shortButtonWidth,
        20,
        new StringTextComponent(I18n.get("fpvdrone.wizard.header.help")),
        (Button button) -> {
          if (qa == null) {
            screen.getMinecraft().setScreen(new HelpScreen(screen));
          } else {
            screen.getMinecraft().setScreen(new HelpQAScreen(screen, qa));
          }
        }
      ));
    }
  }
  
  @Override
  public void render(
    FpvScreen screen,
    PoseStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    if (this.title != null) {
      AbstractGui.drawCenteredString(matrixStack,
        screen.getMinecraft().font,
        this.title,
        screen.width / 2,
        WizardConfig.headerTop + 20 / 2 - screen.getMinecraft().font.lineHeight / 2,
        0xFFFFFF
      );
    }

    Minecraft minecraft = Minecraft.getInstance();
    int width = minecraft.font.width("x");

    minecraft.font.draw(
        matrixStack,
        "x",
        WizardConfig.left + 10 - width / 2f + 1,
        WizardConfig.headerTop + 10 - minecraft.font.lineHeight / 2f - 1 + 1,
        0x464646
    );

    minecraft.font.draw(
        matrixStack,
        "x",
        WizardConfig.left + 10 - width / 2f,
        WizardConfig.headerTop + 10 - minecraft.font.lineHeight / 2f - 1,
        0xFFFFFF
    );
  }
}
