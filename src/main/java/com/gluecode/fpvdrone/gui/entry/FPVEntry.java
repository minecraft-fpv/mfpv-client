package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.list.FPVList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class FPVEntry extends AbstractOptionList.Entry<FPVEntry> {
  public FPVList list;
  public String name;
  public String editValue = "";
  public boolean editMode = false;
  
  public FPVEntry(FPVList list, String name) {
    this.list = list;
    this.name = name;
  }
  
  public void handleChangePress(@Nullable Widget button) {
    if (this.list.activeEntry != null &&
        this.list.activeEntry != this &&
        this.list.activeEntry.editMode) {
      // There is already an activeEntry.
      // simulate clicking the other button first to deactivate it.
      this.list.activeEntry.handleChangePress(button);
    }
    this.list.activeEntry = this;
  }
  
  public int maxValueLength() {
    return 7;
  }
  
  abstract public boolean isLetterAcceptable(String letter);
  
  public boolean keyPressed(
    int keyCode,
    int scanCode,
    int modifiers
  ) {
    InputMappings.Input input = InputMappings.getKey(
      keyCode,
      scanCode
    );
    if (editMode) {
      if (input.toString().equals("key.keyboard.backspace") && this.editValue.length() > 0) {
        this.editValue = this.editValue.substring(
          0,
          this.editValue.length() - 1
        );
      } else if (this.editValue.length() < this.maxValueLength()) {
        String letter = GLFW.glfwGetKeyName(input.getValue(), -1);
        if (letter == null) {
          return super.keyPressed(
            keyCode,
            scanCode,
            modifiers
          );
        }
        if (isLetterAcceptable(letter)) {
          this.editValue = this.editValue + letter;
          return true;
        }
      }
      return super.keyPressed(
        keyCode,
        scanCode,
        modifiers
      );
    } else {
      return super.keyPressed(
        keyCode,
        scanCode,
        modifiers
      );
    }
  }
  
  abstract public void betterRender(
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
  );
  
  public void render(
    MatrixStack matrixStack,
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
    Minecraft minecraft = Minecraft.getInstance();
    Screen currentScreen = minecraft.screen;
    if (currentScreen != null) {
      rowLeft = this.list.getLeftPadding();
      rowWidth = currentScreen.width - this.list.getLeftPadding() - this.list.getRightPadding();
      boolean showScrollBar = this.list.getMaxScroll() > 0;
      if (showScrollBar) {
        rowWidth -= 6;
      }
    }
  
    FontRenderer fontRenderer = minecraft.font;
    
    betterRender(matrixStack, fontRenderer, rowIndex, rowTop, rowLeft, rowWidth, rowHeight, mouseX, mouseY, isMouseOver, partialTicks);
  }
}
