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

public class SingleButtonEntry extends FPVEntry {
  private Supplier<String> getName;
  private Supplier<Boolean> getDisabled;
  private Button selectButton;
  
  public SingleButtonEntry(
    FPVList list,
    Supplier<String> getName,
    Supplier<Boolean> getDisabled,
    Runnable onSelect
  ) {
    super(list, "");
    this.getName = getName;
    this.getDisabled = getDisabled;
    this.selectButton = new Button(
      0,
      0,
      80,
      20,
      new StringTextComponent(I18n.get("fpvdrone.settings.choose")),
      (@Nullable Button button) -> {
        onSelect.run();
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
    int selectWidth = this.selectButton.getWidth();
    
    this.selectButton.x = right - selectWidth;
    this.selectButton.y = rowTop;
    this.selectButton.active = !this.getDisabled.get();
    this.selectButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return ImmutableList.of(this.selectButton);
  }
}
