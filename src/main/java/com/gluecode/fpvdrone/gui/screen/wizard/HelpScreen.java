package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.gui.screen.EmptyListScreen;
import com.gluecode.fpvdrone.gui.screen.addon.BackFooter;
import com.gluecode.fpvdrone.gui.screen.addon.WizardHeader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class HelpScreen extends EmptyListScreen {
  
  public HelpScreen(
    Screen previousScreen
  ) {
    super(previousScreen, null, new BackFooter());
  }
  
  public void handleDiscord(Screen screen) {
    screen.getMinecraft().setScreen(new ConfirmOpenLinkScreen((p_244739_1_) -> {
      if (p_244739_1_) {
        Util.getPlatform().openUri(
          "https://discord.gg/WJfhXuz");
      }
      screen.getMinecraft().setScreen(screen);
    }, "https://discord.gg/WJfhXuz", true));
  }
  
  public void handleWiki(Screen screen) {
    screen.getMinecraft().setScreen(new ConfirmOpenLinkScreen((p_244739_1_) -> {
      if (p_244739_1_) {
        Util.getPlatform().openUri(
          "https://minecraftfpv.com/wiki");
      }
      screen.getMinecraft().setScreen(screen);
    }, "https://minecraftfpv.com/wiki", true));
  }
  
  @Override
  protected void init() {
    super.init();
  
    this.addButton(new Button(
      this.width / 2 - WizardConfig.shortButtonWidth / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing,
      WizardConfig.shortButtonWidth,
      20,
      new StringTextComponent("Discord"),
      (button) -> this.handleDiscord(this)
    ));
  
    this.addButton(new Button(
      this.width / 2 - WizardConfig.shortButtonWidth / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 20 + WizardConfig.doubleButtonSpacing,
      WizardConfig.shortButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.settings.wiki")),
      (button) -> this.handleWiki(this)
    ));
  }
  
  @Override
  public void renderCustom(
    PoseStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    super.renderCustom(matrixStack, mouseX, mouseY, partialTicks);
  
    Minecraft minecraft = Minecraft.getInstance();
    String title = I18n.get("fpvdrone.wizard.help.title");
    int welcomeWidth = minecraft.font.width(title);
    minecraft.font.draw(
      matrixStack,
      title,
      this.width / 2f - welcomeWidth / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop,
      0xFFFFFF
    );
  }
}
