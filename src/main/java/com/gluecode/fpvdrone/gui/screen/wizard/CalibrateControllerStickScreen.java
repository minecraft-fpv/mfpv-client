package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.screen.EmptyListScreen;
import com.gluecode.fpvdrone.gui.screen.addon.BackProceedFooter;
import com.gluecode.fpvdrone.gui.screen.addon.WizardHeader;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.render.StickOverlayRenderer;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.StringTextComponent;
import org.json.simple.JSONArray;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;

public class CalibrateControllerStickScreen extends EmptyListScreen {
  private enum Step {
    FIRST_CENTERING,
    LEFT_UP,
    LEFT_RIGHT,
    RIGHT_UP,
    RIGHT_RIGHT,
    RANGE,
    VERIFY
  }
  
  private Step step;
  private boolean showRecenter = false;
  private Button retryButton;
  private long lastProceedTime;
  
  public CalibrateControllerStickScreen(
    Screen previousScreen
  ) {
    super(previousScreen, new WizardHeader(I18n.get("fpvdrone.wizard.calibrateControllerStick.title"), true), new BackProceedFooter());
    
    BackProceedFooter footer = (BackProceedFooter) this.footer;
    if (footer != null) {
      footer.completeConstructor(this::onProceed, this::getProceedLabel, this::isProceedVisible);
      footer.overrideOnBack(this::onBack);
    }
    
    this.step = Step.FIRST_CENTERING;
    lastProceedTime = System.currentTimeMillis();
  }
  
  public void startListening() {
    ControllerReader.startInputListening((int channel) -> {
      switch(this.step) {
        case LEFT_UP: {
          // throttle
          ControllerConfig.setThrottleChannel(channel);
          float value = ControllerReader.getAxisDiffOnChannel(channel);
          ControllerConfig.setInvertThrottle(value < 0);
          break;
        }
        case LEFT_RIGHT: {
          // yaw
          ControllerConfig.setYawChannel(channel);
          float value = ControllerReader.getAxisDiffOnChannel(channel);
          ControllerConfig.setInvertYaw(value < 0);
          break;
        }
        case RIGHT_UP: {
          // pitch
          ControllerConfig.setPitchChannel(channel);
          float value = ControllerReader.getAxisDiffOnChannel(channel);
          ControllerConfig.setInvertPitch(value < 0);
          break;
        }
        case RIGHT_RIGHT: {
          // roll
          ControllerConfig.setRollChannel(channel);
          float value = ControllerReader.getAxisDiffOnChannel(channel);
          ControllerConfig.setInvertRoll(value < 0);
          break;
        }
        default:
          break;
      }
      
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      
      SettingsLoader.save();
      this.onProceed();
    });
  }
  
  public void startRangeListening() {
    ControllerReader.startRangeListening((float[] range) -> {
      JSONArray array = new JSONArray();
      for (int i = 0; i < range.length; i++) {
        array.add(range[i]);
      }
      ControllerConfig.setRange(array);
      SettingsLoader.save();
  
      Minecraft minecraft = Minecraft.getInstance();
      minecraft.getSoundManager().play(SimpleSound.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      
      this.onProceed();
    });
  }
  
  public void onBack() {
    lastProceedTime = System.currentTimeMillis();
    
    switch(this.step) {
      default:
      case FIRST_CENTERING:
        this.getMinecraft().setScreen(this.previousScreen);
        break;
      case LEFT_UP:
        if (!this.showRecenter) {
          this.step = Step.FIRST_CENTERING;
        } else {
          this.showRecenter = false;
          this.startListening();
        }
        break;
      case LEFT_RIGHT:
        if (!this.showRecenter) {
          this.step = Step.LEFT_UP;
        } else {
          this.showRecenter = false;
        }
        this.startListening();
        break;
      case RIGHT_UP:
        if (!this.showRecenter) {
          this.step = Step.LEFT_RIGHT;
        } else {
          this.showRecenter = false;
        }
        this.startListening();
        this.showRecenter = false;
        break;
      case RIGHT_RIGHT:
        if (!this.showRecenter) {
          this.step = Step.RIGHT_UP;
        } else {
          this.showRecenter = false;
        }
        this.startListening();
        this.showRecenter = false;
        break;
      case RANGE:
        this.step = Step.RIGHT_RIGHT;
        this.showRecenter = true;
        break;
      case VERIFY:
        this.step = Step.RANGE;
        this.startRangeListening();
        break;
    }
  }
  
  public void onProceed() {
    lastProceedTime = System.currentTimeMillis();
    
    switch(this.step) {
      default:
      case FIRST_CENTERING:
        this.step = Step.LEFT_UP;
        this.startListening();
        break;
      case LEFT_UP:
        if (!this.showRecenter) {
          this.showRecenter = true;
        } else {
          this.showRecenter = false;
          this.step = Step.LEFT_RIGHT;
          this.startListening();
        }
        break;
      case LEFT_RIGHT:
        if (!this.showRecenter) {
          this.showRecenter = true;
        } else {
          this.showRecenter = false;
          this.step = Step.RIGHT_UP;
          this.startListening();
        }
        break;
      case RIGHT_UP:
        if (!this.showRecenter) {
          this.showRecenter = true;
        } else {
          this.showRecenter = false;
          this.step = Step.RIGHT_RIGHT;
          this.startListening();
        }
        break;
      case RIGHT_RIGHT:
        if (!this.showRecenter) {
          this.showRecenter = true;
        } else {
          this.showRecenter = false;
          this.step = Step.RANGE;
          this.startRangeListening();
        }
        break;
      case RANGE:
        this.step = Step.VERIFY;
        break;
      case VERIFY:
        GuiEvents.openCalibrateControllerArmScreen(this);
        break;
    }
  }
  
  public String getProceedLabel() {
    String proceed = I18n.get("gui.proceed");
    String yes = I18n.get("gui.yes");
    
    if (this.showRecenter) {
      return proceed;
    }
    
    switch(this.step) {
      default:
      case FIRST_CENTERING:
        return proceed;
      case LEFT_UP:
      case LEFT_RIGHT:
      case RIGHT_UP:
      case RIGHT_RIGHT:
      case RANGE:
        return "";
      case VERIFY:
        return yes + ", " + proceed;
    }
  }
  
  public boolean isProceedVisible() {
    if (this.showRecenter) {
      return true;
    }
    
    switch(this.step) {
      default:
      case FIRST_CENTERING:
      case VERIFY:
        return true;
      case LEFT_UP:
      case LEFT_RIGHT:
      case RIGHT_UP:
      case RIGHT_RIGHT:
      case RANGE:
        return false;
    }
  }
  
  public String getContentTitle() {
    if (this.showRecenter) {
      return I18n.get("fpvdrone.wizard.calibrateControllerStick.recenter");
    }
  
    switch(this.step) {
      default:
      case FIRST_CENTERING:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.begin");
      case VERIFY:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.verify");
      case LEFT_UP:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.leftUp");
      case LEFT_RIGHT:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.leftRight");
      case RIGHT_UP:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.rightUp");
      case RIGHT_RIGHT:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.rightRight");
      case RANGE:
        return I18n.get("fpvdrone.wizard.calibrateControllerStick.range");
    }
  }
  
  public void handleRetry(Button button) {
    this.showRecenter = false;
    this.step = Step.FIRST_CENTERING;
    lastProceedTime = System.currentTimeMillis();
  }
  
  @Override
  protected void init() {
    super.init();
    
    this.retryButton = new Button(
      this.width / 2 - WizardConfig.wideButtonWidth / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 40 + WizardConfig.doubleButtonSpacing + 8,
      WizardConfig.wideButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.wizard.calibrateControllerStick.retry")),
      this::handleRetry
    );
  
    this.addButton(this.retryButton);
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
    if (this.step == Step.RANGE) {
      extra = -9;
    }
  
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
    
    this.renderSticks(matrixStack);
    
    if (this.step == Step.VERIFY) {
      this.retryButton.visible = true;
    } else {
      this.retryButton.visible = false;
    }
  }
  
  public void renderSticks(PoseStack stack) {
    stack.pushPose();
    stack.translate(this.width / 2f, WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 15, 0);
    stack.scale(2.5f, -2.5f, 1);
  
    if (this.step == Step.RANGE) {
      this.renderRangeLabels(stack, -21f);
      this.renderRangeLabels(stack, 21f);
    }
    
    Tessellator tessellator = Tessellator.getInstance();
    BufferBuilder buffer = tessellator.getBuilder();
    buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
    
    stack.pushPose();
    stack.translate(-21f, 0, 0);
    StickOverlayRenderer.renderAxis(
      stack,
      buffer,
      this.getYawAnim(),
      this.getThrottleAnim()
    );
    
    stack.popPose();
  
    stack.pushPose();
    stack.translate(21f, 0, 0);
    StickOverlayRenderer.renderAxis(
      stack,
      buffer,
      this.getRollAnim(),
      this.getPitchAnim()
    );
    stack.popPose();
  
    stack.popPose();
  
    StickOverlayRenderer.applyLineMode();
    tessellator.end();
    StickOverlayRenderer.cleanLineMode();
  }
  
  public void renderRangeLabels(PoseStack stack, float gridX) {
    Minecraft minecraft = Minecraft.getInstance();
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
  
    String range;
    
    stack.pushPose();
    stack.translate(gridX, 0, 0);
    stack.scale(1f / 2.5f, 1f / -2.5f, 1);
    
    if (gridX < 0) {
      range = df.format(ControllerReader.safeReadRangeSnapshotMin(ControllerConfig.getThrottleChannel()));
    } else {
      range = df.format(ControllerReader.safeReadRangeSnapshotMin(ControllerConfig.getPitchChannel()));
    }
    
    int labelWidth = minecraft.font.width(range);
    minecraft.font.draw(
      stack,
      range,
      -labelWidth / 2f,
      20f + minecraft.font.lineHeight,
      0xFFFFFF
    );
    stack.popPose();
  
    stack.pushPose();
    stack.translate(gridX, 0, 0);
    stack.scale(1f / 2.5f, 1f / -2.5f, 1);
  
    if (gridX < 0) {
      range = df.format(ControllerReader.safeReadRangeSnapshotMax(ControllerConfig.getThrottleChannel()));
    } else {
      range = df.format(ControllerReader.safeReadRangeSnapshotMax(ControllerConfig.getPitchChannel()));
    }
    
    labelWidth = minecraft.font.width(range);
    minecraft.font.draw(
      stack,
      range,
      -labelWidth / 2f,
      -26f - minecraft.font.lineHeight,
      0xFFFFFF
    );
    stack.popPose();
  
    stack.pushPose();
    stack.translate(gridX, 0, 0);
    stack.scale(1f / 2.5f, 1f / -2.5f, 1);
  
    if (gridX < 0) {
      range = df.format(ControllerReader.safeReadRangeSnapshotMax(ControllerConfig.getYawChannel()));
    } else {
      range = df.format(ControllerReader.safeReadRangeSnapshotMax(ControllerConfig.getRollChannel()));
    }
    
    labelWidth = minecraft.font.width(range);
    minecraft.font.draw(
      stack,
      range,
      -26f - labelWidth,
      -minecraft.font.lineHeight / 2f,
      0xFFFFFF
    );
    stack.popPose();
  
    stack.pushPose();
    stack.translate(gridX, 0, 0);
    stack.scale(1f / 2.5f, 1f / -2.5f, 1);
  
    if (gridX < 0) {
      range = df.format(ControllerReader.safeReadRangeSnapshotMax(ControllerConfig.getYawChannel()));
    } else {
      range = df.format(ControllerReader.safeReadRangeSnapshotMax(ControllerConfig.getRollChannel()));
    }
    
    minecraft.font.draw(
      stack,
      range,
      26,
      -minecraft.font.lineHeight / 2f,
      0xFFFFFF
    );
    stack.popPose();
  }
  
  public float sigmoid(float a, float t) {
    return 1f / (1f + FastMath.exp(-a * t)) - 0.5f;
  }
  
  public float easeInOut(float t, float a) {
    return (float) (Math.pow(t, a) / (Math.pow(t, a) + Math.pow(1 - t, a)));
  }
  
  public float lerp(float a, float b, float u, float v, float t) {
    float outRange = b - a;
    float inRange = v - u;
    float p = (t - u) / inRange;
    p = this.easeInOut(p, 2);
    return p * outRange + a;
  }
  
  public float getT() {
    int loopLengthMillis = 1200;
    if (this.step == Step.FIRST_CENTERING || this.showRecenter) {
      loopLengthMillis = 2000;
    } else if (this.step == Step.RANGE) {
      loopLengthMillis = 2000;
    }
    
    int millis = (int) (System.currentTimeMillis() - lastProceedTime) % loopLengthMillis;
    return 1f * millis / loopLengthMillis;
  }
  
  public float getAnimLerp() {
    float t = this.getT();
    float a = 5;
    return this.easeInOut(t, a);
  }
  
  public float getSpiralX() {
    float t = this.easeInOut(this.getT(), 2) * 20f;
    float k = 0.25f;
    float raw = (float) (Math.exp((1 - t) * k) * Math.sin(1 - t));
    float norm = (float) (Math.exp(k) * Math.sin(1));
    return raw / norm;
  }
  
  public float getSpiralY() {
    float t = this.easeInOut(this.getT(), 2) * 20f;
    float k = 0.25f;
    float raw = (float) (Math.exp((1 - t) * k) * Math.cos(1 - t));
    float norm = (float) (Math.exp(k) * Math.sin(1));
    return raw / norm;
  }
  
  public float getBoxX() {
    float t = this.getT();
    
    if (0 <= t && t < 0.25f) {
      // top edge
      return this.lerp(-1, 1, 0, 0.25f, t);
    } else if (0.25f <= t && t < 0.5f) {
      return 1;
    } else if (0.5f <= t && t < 0.75f) {
      // bottom edge
      return this.lerp(1, -1, 0.5f, 0.75f, t);
    } else {
      return -1;
    }
  }
  
  public float getBoxY() {
    float t = this.getT();
  
    if (0 <= t && t < 0.25f) {
      return 1;
    } else if (0.25f <= t && t < 0.5f) {
      // right
      return this.lerp(1, -1, 0.25f, 0.5f, t);
    } else if (0.5f <= t && t < 0.75f) {
      return -1;
    } else {
      // left
      return this.lerp(-1, 1, 0.75f, 1, t);
    }
  }
  
  public float getThrottleAnim() {
    if (this.step == Step.FIRST_CENTERING || this.showRecenter) {
      return this.getSpiralY();
    }
    
    if (this.step == Step.RANGE) {
      return this.getBoxY();
    }
    
    if (this.step == Step.LEFT_UP) {
      return this.getAnimLerp();
    }
  
    if (this.step == Step.VERIFY) {
      return ControllerReader.getThrottle();
    }
  
    return 0;
  }
  
  public float getYawAnim() {
    if (this.step == Step.FIRST_CENTERING || this.showRecenter) {
      return -this.getSpiralX();
    }
  
    if (this.step == Step.RANGE) {
      return this.getBoxX();
    }
  
    if (this.step == Step.LEFT_RIGHT) {
      return this.getAnimLerp();
    }
  
    if (this.step == Step.VERIFY) {
      return ControllerReader.getYaw();
    }
  
    return 0;
  }
  
  public float getPitchAnim() {
    if (this.step == Step.FIRST_CENTERING || this.showRecenter) {
      return this.getSpiralY();
    }
  
    if (this.step == Step.RANGE) {
      return this.getBoxY();
    }
  
    if (this.step == Step.RIGHT_UP) {
      return this.getAnimLerp();
    }
  
    if (this.step == Step.VERIFY) {
      return ControllerReader.getPitch();
    }
    
    return 0;
  }
  
  public float getRollAnim() {
    if (this.step == Step.FIRST_CENTERING || this.showRecenter) {
      return this.getSpiralX();
    }
  
    if (this.step == Step.RANGE) {
      return -this.getBoxX();
    }
  
    if (this.step == Step.RIGHT_RIGHT) {
      return this.getAnimLerp();
    }
  
    if (this.step == Step.VERIFY) {
      return ControllerReader.getRoll();
    }
    
    return 0;
  }
}
