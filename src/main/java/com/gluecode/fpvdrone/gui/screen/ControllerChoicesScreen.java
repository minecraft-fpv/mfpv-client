package com.gluecode.fpvdrone.gui.screen;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.list.ControllerChoicesList;
import com.gluecode.fpvdrone.gui.screen.addon.DoneFooter;
import com.gluecode.fpvdrone.gui.screen.addon.ServerTitleWikiHeader;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.GameSettings;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SettingsScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;

public class ControllerChoicesScreen extends FpvScreen {
  private ControllerChoicesList keyBindingList;
  
  public ControllerChoicesScreen(
    Screen previousScreen
  ) {
    super(
      previousScreen,
      new ServerTitleWikiHeader(I18n.get("fpvdrone.device.choose")),
      new DoneFooter()
    );
  }
  
  public void handleIdSet(int id) {
    ControllerReader.setControllerId(id);
    if (this.footer != null) {
      DoneFooter footer = (DoneFooter) this.footer;
      footer.handleDone(this);
    }
  }
  
  @Override
  protected void init() {
    super.init();
    this.keyBindingList = new ControllerChoicesList(
      this,
      this::handleIdSet
    );
    this.children.add(this.keyBindingList);
  }
  
  @Override
  public void renderCustom(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.keyBindingList.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
