package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.screen.EmptyListScreen;
import com.gluecode.fpvdrone.gui.screen.addon.DoneFooter;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class CompleteScreen extends EmptyListScreen {
  public enum Mode {
    SWITCH,
    KEY
  }
  
  private Mode mode;
  
  public CompleteScreen(
    Screen previousScreen,
    Mode mode
  ) {
    super(previousScreen, null, new DoneFooter());
    
    this.mode = mode;
    
    DoneFooter footer = (DoneFooter) this.footer;
    if (footer != null) {
      footer.overrideDone(this::handleDone);
    }
  }
  
  public void handleDone() {
    SettingsLoader.firstTimeSetup = false;
    SettingsLoader.save();
    if (WelcomeScreen.currentWelcomeScreen != null) {
      WelcomeScreen.currentWelcomeScreen.getMinecraft()
        .setScreen(WelcomeScreen.currentWelcomeScreen.previousScreen);
    }
  }
  
  public void handleMore(Button button) {
    SettingsLoader.firstTimeSetup = false;
    SettingsLoader.save();
    if (WelcomeScreen.currentWelcomeScreen != null) {
      GuiEvents.openFpvSettingsScreen(WelcomeScreen.currentWelcomeScreen.previousScreen);
    }
  }
  
  @Override
  protected void init() {
    super.init();
    
    this.addButton(new Button(
      this.width / 2 - WizardConfig.wideButtonWidth / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 40 + WizardConfig.doubleButtonSpacing + 2,
      WizardConfig.wideButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.wizard.complete.more")),
      this::handleMore
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
    
    float lineHeight = WizardConfig.lineHeight;
    
    Minecraft minecraft = Minecraft.getInstance();
    String ready = I18n.get("fpvdrone.wizard.complete.ready");
    int readyWidth = minecraft.font.width(ready);
    
    String goBackSwitch = this.mode == Mode.SWITCH ? I18n.get("fpvdrone.wizard.complete.goBackSwitch") : I18n.get("fpvdrone.wizard.complete.goBackKey");
    String switchTop = this.safeSplitGet(goBackSwitch, "\n", 0);
    String switchBottom = this.safeSplitGet(goBackSwitch, "\n", 1);
    
    int switchTopWidth = minecraft.font.width(switchTop);
    int switchBottonWidth = minecraft.font.width(switchBottom);
    
    minecraft.font.draw(
      matrixStack,
      ready,
      this.width / 2f - readyWidth / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop,
      0xFFFFFF
    );
    
    minecraft.font.draw(
      matrixStack,
      switchTop,
      this.width / 2f - switchTopWidth / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop + 2 * minecraft.font.lineHeight * lineHeight,
      0xFFFFFF
    );
    
    minecraft.font.draw(
      matrixStack,
      switchBottom,
      this.width / 2f - switchBottonWidth / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop + 3 * minecraft.font.lineHeight * lineHeight,
      0xFFFFFF
    );
  }
  
  public String safeSplitGet(String str, String regex, int index) {
    if (index < 0) {
      return "";
    }
    
    String[] split = str.split(regex);
    
    if (index <= split.length) {
      return split[index];
    } else {
      return "";
    }
  }
}
