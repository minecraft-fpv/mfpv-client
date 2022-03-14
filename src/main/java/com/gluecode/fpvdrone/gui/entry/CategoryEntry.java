package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.gui.list.FPVList;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class CategoryEntry extends FPVEntry {
  private Supplier<String> title;
  private int color = 16777215;
  
  public CategoryEntry(FPVList list, Supplier<String> title) {
    super(list, "");
    this.title = title;
  }
  
  public CategoryEntry(FPVList list, Supplier<String> title, int color) {
    super(list, "");
    this.title = title;
    this.color = color;
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
    String label;
    String name = this.title.get();
    if (name.startsWith("Axis Mapping")) {
      label = "Axis Mapping (CH 1 - " +
              ControllerReader.getAxisLength() +
              ")";
    } else if (name.startsWith("Switch Mapping")) {
      label =
        "Switch Mapping (CH " +
        (ControllerReader.getAxisLength() + 1) +
        " - " +
        (ControllerReader.getAxisLength() +
         ControllerReader.getButtonLength()) +
        ")";
    } else {
      label = name;
    }
  
    int labelWidth = fontRenderer.width(label);
    
    fontRenderer.draw(
      matrixStack,
      label,
      (float) (rowLeft + rowWidth / 2 - labelWidth / 2),
      (float) (rowTop + 6),
      this.color
    );
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return Collections.emptyList();
  }
}
