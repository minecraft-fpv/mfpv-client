package com.gluecode.fpvdrone.gui.screen;

import com.gluecode.fpvdrone.gui.screen.addon.ScreenAddon;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public abstract class FpvScreen extends SettingsScreen {
  @Nullable
  public Screen previousScreen;
  @Nullable
  public ScreenAddon header;
  @Nullable
  public ScreenAddon footer;
  
  public FpvScreen(
    Screen previousScreen,
    @Nullable ScreenAddon header,
    @Nullable ScreenAddon footer
  ) {
    super(
      previousScreen,
      Minecraft.getInstance().options,
      new StringTextComponent("")
    );
    this.previousScreen = previousScreen;
    this.header = header;
    this.footer = footer;
  }
  
  @Override
  protected void init() {
    if (this.header != null) {
      this.header.init(this);
    }
    if (this.footer != null) {
      this.footer.init(this);
    }
  }
  
  @Override
  public <T extends Widget> T addButton(T button) { // Access widener
    return super.addButton(button);
  }
  
  abstract public void renderCustom(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  );
  
  @Override
  public void render(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.renderBackground(matrixStack);
    this.renderCustom(matrixStack, mouseX, mouseY, partialTicks);

    super.render(matrixStack, mouseX, mouseY, partialTicks);

    // These need to render after super.render because of text that might be drawn on top of "x" button.
    if (this.header != null) {
      this.header.render(this, matrixStack, mouseX, mouseY, partialTicks);
    }
    if (this.footer != null) {
      this.footer.render(this, matrixStack, mouseX, mouseY, partialTicks);
    }
  }
}
