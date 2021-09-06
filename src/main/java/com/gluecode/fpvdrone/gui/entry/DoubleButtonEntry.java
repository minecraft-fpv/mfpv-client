package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.gui.list.FPVList;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class DoubleButtonEntry extends FPVEntry {
  private Supplier<String> getName;
  private Button changeButton;
  private Button configureButton;
  private Supplier<Boolean> disabled;
  
  public DoubleButtonEntry(
    FPVList list,
    Supplier<String> getName,
    String leftLabel,
    Runnable onLeft,
    String rightLabel,
    Runnable onRight,
    Supplier<Boolean> disabled
  ) {
    super(list, "");
    this.disabled = disabled;
    this.getName = getName;
    this.configureButton = new Button(
      0,
      0,
      80,
      20,
      new StringTextComponent(leftLabel),
      (@Nullable Button button) -> {
        onLeft.run();
      }
    );
    this.changeButton = new Button(
      0,
      0,
      80,
      20,
      new StringTextComponent(rightLabel),
      (@Nullable Button button) -> {
        onRight.run();
      }
    );
  }
  
  @Override
  public boolean isLetterAcceptable(String letter) {
    return false;
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
    fontRenderer.draw(
      matrixStack,
      this.getName.get(),
      rowLeft,
      (float) (rowTop + 6),
      16777215
    );
  
    int right = rowLeft + rowWidth;
    int configureWidth = this.configureButton.getWidth();
    int changeWidth = this.changeButton.getWidth();
    
    this.configureButton.x = right - changeWidth - 1 - configureWidth;
    this.configureButton.y = rowTop;
    this.configureButton.active = !this.disabled.get();
    this.configureButton.render(
      matrixStack,
      mouseX,
      mouseY,
      partialTicks
    );
    
    this.changeButton.x = right - changeWidth;
    this.changeButton.y = rowTop;
    this.changeButton.active = !this.disabled.get();
    this.changeButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return ImmutableList.of(this.changeButton, this.configureButton);
  }
}
