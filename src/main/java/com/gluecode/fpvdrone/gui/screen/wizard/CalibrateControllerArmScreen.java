package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.screen.EmptyListScreen;
import com.gluecode.fpvdrone.gui.screen.addon.BackProceedFooter;
import com.gluecode.fpvdrone.gui.screen.addon.WizardHeader;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;

public class CalibrateControllerArmScreen extends EmptyListScreen {
  private enum Step {
    BEGIN,
    PING,
    FLIP
  }
  
  private Step step;
  private Button retryButton;
  private Button flipButton;
  private long lastProceedTime;
  private long lastPingTime;
  
  public CalibrateControllerArmScreen(
    Screen previousScreen
  ) {
    super(previousScreen, new WizardHeader(I18n.get("fpvdrone.wizard.calibrateControllerStick.title"), true), new BackProceedFooter());
  
    BackProceedFooter footer = (BackProceedFooter) this.footer;
    if (footer != null) {
      footer.completeConstructor(this::onProceed, this::getProceedLabel, this::isProceedVisible);
      footer.overrideOnBack(this::onBack);
    }
  
    this.step = Step.BEGIN;
    lastProceedTime = System.currentTimeMillis();
    lastPingTime = System.currentTimeMillis();
    this.startAssignmentListening();
  }
  
  public void startAssignmentListening() {
    ControllerReader.startInputListening((int channel) -> {
      lastPingTime = System.currentTimeMillis();
      ControllerConfig.setArmChannel(channel - ControllerReader.getAxisLength());
      SettingsLoader.save();
  
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      
      this.onProceed();
    });
  }
  
  public void startPingListening() {
    ControllerReader.startInputListening((int channel) -> {
      if (channel == ControllerConfig.getArmChannel() + ControllerReader.getAxisLength()) {
        lastPingTime = System.currentTimeMillis();
        lastProceedTime = System.currentTimeMillis() + 500;
  
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }
      
      if (this.step == Step.PING || this.step == Step.FLIP) {
        this.startPingListening();
      }
    });
  }
  
  public void onBack() {
    lastProceedTime = System.currentTimeMillis();
  
    switch(this.step) {
      default:
      case BEGIN:
        this.getMinecraft().setScreen(this.previousScreen);
        break;
      case PING:
        this.step = Step.BEGIN;
        this.startAssignmentListening();
        break;
      case FLIP:
        this.step = Step.PING;
        this.startPingListening();
        break;
    }
  }
  
  public void onProceed() {
    lastProceedTime = System.currentTimeMillis();
    
    switch(this.step) {
      default:
      case BEGIN:
        this.step = Step.PING;
        this.startPingListening();
        break;
      case PING:
        this.step = Step.FLIP;
        break;
      case FLIP:
        GuiEvents.openCompleteSwitchScreen(this);
        break;
    }
  }
  
  public String getProceedLabel() {
    String proceed = I18n.get("gui.proceed");
    String yes = I18n.get("gui.yes");
    String no = I18n.get("gui.no");
    
    switch(this.step) {
      default:
      case BEGIN:
        return "";
      case PING:
        return yes + ", " + proceed;
      case FLIP:
        return no + ", " + proceed;
    }
  }
  
  public boolean isProceedVisible() {
    switch(this.step) {
      default:
      case BEGIN:
        return false;
      case PING:
        return true;
      case FLIP:
        return true;
    }
  }
  
  public String getContentTitle() {
    switch(this.step) {
      default:
      case BEGIN:
        return I18n.get("fpvdrone.wizard.calibrateControllerArm.begin");
      case PING:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.verify");
      case FLIP:
        return I18n.get("fpvdrone.wizard.calibrateControllerArm.flip");
    }
  }
  
  public void handleRetry(Button button) {
    this.onBack();
  }
  
  public void handleFlip(Button button) {
    ControllerConfig.setInvertArm(!ControllerConfig.getInvertArm());
  }
  
  @Override
  protected void init() {
    super.init();
  
    this.retryButton = new Button(
      this.width / 2 - WizardConfig.wideButtonWidth / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 40 + WizardConfig.doubleButtonSpacing + 2,
      WizardConfig.wideButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.wizard.calibrateControllerStick.retry")),
      this::handleRetry
    );
  
    this.flipButton = new Button(
      this.width / 2 - WizardConfig.wideButtonWidth / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 40 + WizardConfig.doubleButtonSpacing + 2,
      WizardConfig.wideButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.wizard.calibrateControllerArm.flipButton")),
      this::handleFlip
    );
  
    this.addButton(this.retryButton);
    this.addButton(this.flipButton);
  }
  
  @Override
  public void renderCustom(
    PoseStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    super.renderCustom(matrixStack, mouseX, mouseY, partialTicks);
  
    float extra = -6;
//    if (this.step == CalibrateControllerStickScreen.Step.RANGE) {
//      extra = -9;
//    }
  
    Minecraft minecraft = Minecraft.getInstance();
    String title = this.getContentTitle();
    int width = minecraft.font.width(title);
    minecraft.font.draw(
      matrixStack,
      title,
      this.width / 2f - width / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop + extra,
      0xFFFFFF
    );
    
    if (this.step == Step.BEGIN) {
      String label = I18n.get("fpvdrone.wizard.calibrateControllerArm.begin2");
      int labelWidth = minecraft.font.width(label);
      minecraft.font.draw(
        matrixStack,
        label,
        this.width / 2f - labelWidth / 2f,
        WizardConfig.headerHeight + WizardConfig.contentTop + extra + minecraft.font.lineHeight * WizardConfig.lineHeight,
        0xFFFFFF
      );
    }
    
    this.renderListening(matrixStack);
  
    if (this.step == Step.PING) {
      this.retryButton.visible = true;
    } else {
      this.retryButton.visible = false;
    }
  
    if (this.step == Step.FLIP) {
      this.flipButton.visible = true;
    } else {
      this.flipButton.visible = false;
    }
  }
  
  public float getT() {
    int loopLengthMillis = 3250;
    int millis = (int) (System.currentTimeMillis() - lastProceedTime) % loopLengthMillis;
    return 1f * millis / loopLengthMillis;
  }
  
  public String getDots() {
    float t = this.getT() * 2.9999f;
    int nDots = (int) FastMath.floor(t);
    if (nDots == 0) return "";
    if (nDots == 1) return ".";
    if (nDots == 2) return "..";
    return "";
  }
  
  public void renderListening(PoseStack matrixStack) {
    Minecraft minecraft = Minecraft.getInstance();
    
    if (this.step == Step.BEGIN) {
      String listening = I18n.get("fpvdrone.wizard.calibrateControllerArm.listening") + ".";
      int listeningWidth = minecraft.font.width(listening);
      listening += this.getDots();
      minecraft.font.draw(
        matrixStack,
        listening,
        this.width / 2f - listeningWidth / 2f,
        WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 8,
        0xFFFFFF
      );
    } else if (this.step == Step.PING) {
      String listening = I18n.get("fpvdrone.wizard.calibrateControllerArm.listening") + ".";
      String detected = I18n.get("fpvdrone.wizard.calibrateControllerArm.detected") + "!";
      int listeningWidth = minecraft.font.width(listening);
      listening += this.getDots();
      int detectedWidth = minecraft.font.width(detected);
      
      if (System.currentTimeMillis() - this.lastPingTime < 500) {
        minecraft.font.draw(
          matrixStack,
          detected,
          this.width / 2f - detectedWidth / 2f,
          WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 8,
          0xFFFFFF
        );
      } else {
        minecraft.font.draw(
          matrixStack,
          listening,
          this.width / 2f - listeningWidth / 2f,
          WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 8,
          0xFFFFFF
        );
      }
    } else if (this.step == Step.FLIP) {
      String on = I18n.get("options.on");
      String off = I18n.get("options.off");
      int offWidth = minecraft.font.width(off);
      
      minecraft.font.draw(
        matrixStack,
        off,
        this.width / 2f - offWidth - 3,
        WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 8,
        !ControllerReader.rawArm ? 0xFFFFFF : 0x6F6F6F
      );
  
      minecraft.font.draw(
        matrixStack,
        on,
        this.width / 2f + 3,
        WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 8,
        ControllerReader.rawArm ? 0xFFFFFF : 0x6F6F6F
      );
    }
  }
}
