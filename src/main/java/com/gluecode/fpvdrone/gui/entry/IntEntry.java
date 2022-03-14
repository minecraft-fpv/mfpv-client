package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.list.FPVList;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.google.common.collect.ImmutableList;
import com.jme3.math.FastMath;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class IntEntry extends FPVEntry {
  private Supplier<Integer> getValue;
  private Consumer<Integer> setNewValue;
  private Runnable setDefaultValue;
  private Supplier<Integer> getDefaultValue;
  private Button changeButton;
  private Button resetButton;
  
  public IntEntry(
    FPVList list,
    String name,
    Supplier<Integer> getValue,
    Consumer<Integer> setNewValue,
    Supplier<Integer> getDefaultValue,
    Runnable setDefaultValue
  ) {
    super(list, name);
    this.name = name;
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
        return true;
      default:
        return false;
    }
  }
  
  @Override
  public void betterRender(
    PoseStack matrixStack,
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
    fontRenderer.draw(
      matrixStack,
      this.name,
      rowLeft,
      (float) (rowTop + 6),
      16777215
    );
  
    int right = rowLeft + rowWidth;
    int resetWidth = this.resetButton.getWidth();
    int changeWidth = this.changeButton.getWidth();
  
    this.resetButton.x = right - resetWidth;
    this.resetButton.y = rowTop;
    this.resetButton.active = FastMath.abs(this.getDefaultValue.get() -
                                           this.getValue.get()) >
                              0.001f; // todo
    this.changeButton.x = right - resetWidth - 1 - changeWidth;
    this.changeButton.y = rowTop;
  
    if (this.editMode) {
      this.changeButton.setMessage(new StringTextComponent("> " +
                                                           editValue +
                                                           "_ <"));
    } else {
      int value = this.getValue.get();
      this.changeButton.setMessage(new StringTextComponent("" +
                                                           value));
    }
  
    if (SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)) {
      this.resetButton.active = false;
      this.changeButton.active = false;
    }
    this.resetButton.render(matrixStack, mouseX, mouseY, partialTicks);
    this.changeButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return ImmutableList.of(this.changeButton, this.resetButton);
  }
  
  public void handleChangePress(@Nullable Widget button) {
    super.handleChangePress(button);
    
    this.editMode = !this.editMode;
    if (editMode) {
      this.editValue = "";
    } else {
      if (!this.editValue.equals("")) {
        try {
          int value = Integer.parseInt(this.editValue);
          setNewValue.accept(value);
          SettingsLoader.save();
        } catch (Exception e) {
          Main.LOGGER.info(e);
        }
      }
    }
  }
  
  public void handleResetPress(Button button) {
    this.editMode = false;
    this.setDefaultValue.run();
  }
}
