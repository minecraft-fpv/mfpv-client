package com.gluecode.fpvdrone.gui.screen;

import com.gluecode.fpvdrone.gui.list.OtherSettingsList;
import com.gluecode.fpvdrone.gui.screen.addon.DoneFooter;
import com.gluecode.fpvdrone.gui.screen.addon.ServerTitleWikiHeader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;

public class OtherSettingsScreen extends FpvScreen {
  private OtherSettingsList otherSettingsList;

  public OtherSettingsScreen(
    Screen previousScreen
  ) {
    super(
      previousScreen,
      new ServerTitleWikiHeader(I18n.get("fpvdrone.settings.fpvsettings")),
      new DoneFooter()
    );
  }

  @Override
  protected void init() {
    super.init();
    
    this.otherSettingsList = new OtherSettingsList(this);
    this.children.add(this.otherSettingsList);
  }

  // func_230430_a_ = render
  @Override
  public void renderCustom(
    PoseStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.otherSettingsList.render(
      matrixStack,
      mouseX,
      mouseY,
      partialTicks
    );
  }
}
