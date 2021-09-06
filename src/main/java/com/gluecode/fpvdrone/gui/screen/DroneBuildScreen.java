package com.gluecode.fpvdrone.gui.screen;

import com.gluecode.fpvdrone.gui.list.DroneBuildList;
import com.gluecode.fpvdrone.gui.screen.addon.DoneFooter;
import com.gluecode.fpvdrone.gui.screen.addon.ServerTitleWikiHeader;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.*;

public class DroneBuildScreen extends FpvScreen {
  private DroneBuildList droneBuildList;

  public DroneBuildScreen(
    Screen previousScreen
  ) {
    super(
      previousScreen,
      new ServerTitleWikiHeader(I18n.get("fpvdrone.settings.fpvsettings")),
      new DoneFooter()
    );
  }

  // func_231160_c_ = init
  @Override
  protected void init() {
    super.init();
    this.droneBuildList = new DroneBuildList(this);
    this.children.add(this.droneBuildList);
  }

  // func_230430_a_ = render
  @Override
  public void renderCustom(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.droneBuildList.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
