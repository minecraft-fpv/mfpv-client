package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.widget.SnappySlider;
import com.gluecode.fpvdrone.gui.list.FPVList;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.google.common.collect.ImmutableList;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class FloatEntry extends FPVEntry {
  private Supplier<Float> getValue;
  private Consumer<Float> setNewValue;
  private Runnable setDefaultValue;
  private Supplier<Float> getDefaultValue;
  private Button changeButton;
  private Button resetButton;
  
  private SnappySlider slider;
  private Supplier<Float> getMin;
  private Supplier<Float> getMax;
  private Supplier<Boolean> disabled;
  
  public FloatEntry(
    FPVList list,
    String name,
    Supplier<Float> getValue,
    Consumer<Float> setNewValue,
    Supplier<Float> getDefaultValue,
    Runnable setDefaultValue,
    boolean isLimited,
    Supplier<Float> getMin,
    Supplier<Float> getMax,
    Supplier<Boolean> disabled
  ) {
    super(list, name);
    this.getValue = getValue;
    this.setNewValue = setNewValue;
    this.getDefaultValue = getDefaultValue;
    this.setDefaultValue = setDefaultValue;
    this.changeButton = new Button(
      0,
      0,
      70,
      20,
      new StringTextComponent(name),
      this::handleChangePress
    );
    this.resetButton = new Button(
      0,
      0,
      50,
      20,
      new StringTextComponent(I18n.get("controls.reset")),
      this::handleResetPress
    );
    this.editMode = false;
    
    if (isLimited) {
      this.slider = new SnappySlider(
        0,
        0,
        70,
        20,
        0.5f,
        getMin,
        getMax,
        this::handleSliderValue,
        this::handleChangePress
      );
      this.getMin = getMin;
      this.getMax = getMax;
    }
  
    this.disabled = disabled;
  }
  
  @Override
  public void handleChangePress(@Nullable Widget button) {
    super.handleChangePress(button);
    
    if (this.slider != null) {
      // When using a slider, editMode is not used.
      return;
    }
    
    this.editMode = !this.editMode;
    if (editMode) {
      this.editValue = "";
    } else {
      if (!this.editValue.equals("")) {
        try {
          float value = Float.parseFloat(this.editValue);
          setNewValue.accept(value);
          SettingsLoader.save();
        } catch (Exception e) {
          Main.LOGGER.info(e);
        }
      }
    }
  }
  
  @Override
  public boolean isLetterAcceptable(String letter) {
    switch (letter) {
      case "0":
      case "1":
      case "2":
      case "3":
      case "4":
      case "5":
      case "6":
      case "7":
      case "8":
      case "9":
      case "-":
      case ".":
        return true;
      default:
        return false;
    }
  }
  
  public void handleResetPress(Button button) {
    this.editMode = false;
    this.setDefaultValue.run();
  }
  
  public void handleSliderValue(float value) {
    setNewValue.accept(value);
    SettingsLoader.save();
  }
  
  @Override
  public void betterRender(
    MatrixStack matrixStack,
    FontRenderer fontRenderer,
    int rowIndex,
    int rowTop,
    int rowLeft,
    int rowWidth,
    int rowHeight,
    int mouseX,
    int mouseY,
    boolean isMouseOver,
    float partialTicks
  ) {
    int right = rowLeft + rowWidth;
    int resetWidth = this.resetButton.getWidth();
    int changeWidth = this.changeButton.getWidth();
    
    fontRenderer.draw(
      matrixStack,
      this.name,
      rowLeft,
      (float) (rowTop + 6),
      16777215
    );
  
    this.resetButton.x = right - resetWidth;
    this.resetButton.y = rowTop;
    this.resetButton.active = FastMath.abs(this.getDefaultValue.get() -
                                           this.getValue.get()) >
                              0.001f; // todo
    
    if (this.slider != null) {
      this.slider.x = right - resetWidth - 1 - changeWidth;
      this.slider.y = rowTop;
    } else {
      this.changeButton.x = right - resetWidth - 1 - changeWidth;
      this.changeButton.y = rowTop;
    }
    
    if (this.slider != null) {
      float value = this.getValue.get();
      this.slider.setValue(value);
      //                this.slider.updateSlider();
    } else {
      if (this.editMode) {
        this.changeButton.setMessage(new StringTextComponent("> " +
                                                             editValue +
                                                             "_ <"));
      } else {
        float value = this.getValue.get();
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        String str = df.format(value);
        this.changeButton.setMessage(new StringTextComponent("" +
                                                             str));
      }
    }
  
    if (this.slider != null) {
      if (disabled.get()) {
        this.resetButton.active = false;
        this.slider.active = false;
      }
      this.slider.render(matrixStack, mouseX, mouseY, partialTicks);
    } else {
      if (disabled.get()) {
        this.resetButton.active = false;
        this.changeButton.active = false;
      }
      this.changeButton.render(
        matrixStack,
        mouseX,
        mouseY,
        partialTicks
      );
    }
    this.resetButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    if (this.slider != null) {
      return ImmutableList.of(
        this.slider,
        this.changeButton,
        this.resetButton
      );
    } else {
      return ImmutableList.of(this.changeButton, this.resetButton);
    }
  }
}
