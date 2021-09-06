package com.gluecode.fpvdrone.gui.screen;

import com.gluecode.fpvdrone.gui.list.ModelSettingsList;
import com.gluecode.fpvdrone.gui.screen.addon.DoneFooter;
import com.gluecode.fpvdrone.gui.screen.addon.ServerTitleWikiHeader;
import com.gluecode.fpvdrone.gui.screen.addon.WizardDoneFooter;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

public class ModelSettingsScreen extends FpvScreen {
  private ModelSettingsList list;

  public ModelSettingsScreen(
    Screen previousScreen
  ) {
    super(
      previousScreen,
      new ServerTitleWikiHeader(I18n.get("fpvdrone.settings.fpvsettings")),
      new WizardDoneFooter()
    );
  }

  // func_231160_c_ = init
  @Override
  protected void init() {
    super.init();
    this.list = new ModelSettingsList(this);
    this.children.add(this.list);
  }

  // func_230430_a_ = render
  @Override
  public void renderCustom(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.list.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
