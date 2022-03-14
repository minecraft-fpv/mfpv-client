package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.gui.list.FPVList;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.google.common.collect.ImmutableList;
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

public class BooleanEntry extends FPVEntry {
  private Supplier<Boolean> getValue;
  private Consumer<Boolean> setNewValue;
  private Runnable setDefaultValue;
  private Supplier<Boolean> getDefaultValue;
  private Button changeButton;
  private Button resetButton;
  
  public BooleanEntry(
    FPVList list,
    String name,
    Supplier<Boolean> getValue,
    Consumer<Boolean> setNewValue,
    Supplier<Boolean> getDefaultValue,
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
    return false;
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
    this.resetButton.active = this.getDefaultValue.get() !=
                              this.getValue.get();
    this.resetButton.render(matrixStack, mouseX, mouseY, partialTicks);
    this.changeButton.x = right - resetWidth - 1 - changeWidth;
    this.changeButton.y = rowTop;
    
    this.changeButton.setMessage(
      new StringTextComponent("" + this.getValue.get())
    );
    
    this.changeButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return ImmutableList.of(this.changeButton, this.resetButton);
  }
  
  public void handleChangePress(@Nullable Widget button) {
    super.handleChangePress(button);
    
    setNewValue.accept(!getValue.get());
    SettingsLoader.save();
  }
  
  public void handleResetPress(Button button) {
    this.setDefaultValue.run();
    SettingsLoader.save();
  }
}
