package com.gluecode.fpvdrone.gui.widget;

import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class FpvSettingsButton extends Button {
  public FpvSettingsButton(int x, int y, IPressable onPress) {
    super(
      x,
      y,
      30,
      20,
      new StringTextComponent(I18n.get("fpvdrone.settings.fpv")),
      onPress
    );
  }
}
