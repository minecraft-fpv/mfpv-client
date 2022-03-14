package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.gui.list.FPVList;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.List;

public class DoubleNavEntry extends FPVEntry {
  private Button leftButton;
  private Button rightButton;
  
  public DoubleNavEntry(
    FPVList list,
    String leftName,
    Runnable onLeftSelect,
    String rightName,
    Runnable onRightSelect
  ) {
    super(list, "");
    this.leftButton = new Button(
      0,
      0,
      150,
      20,
      new StringTextComponent(leftName),
      (Button button) -> {
        onLeftSelect.run();
      }
    );
    this.rightButton = new Button(
      0,
      0,
      150,
      20,
      new StringTextComponent(rightName),
      (Button button) -> {
        onRightSelect.run();
      }
    );
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
    this.leftButton.x = rowLeft + rowWidth / 2 - 150 - 5;
    this.leftButton.y = rowTop;
    this.leftButton.render(matrixStack, mouseX, mouseY, partialTicks);
    
    this.rightButton.x = rowLeft + rowWidth / 2 + 5;
    this.rightButton.y = rowTop;
    this.rightButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return ImmutableList.of(this.leftButton, this.rightButton);
  }
}
