package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.list.ControllerChoicesList;
import com.gluecode.fpvdrone.gui.list.FPVList;
import com.gluecode.fpvdrone.gui.screen.FpvScreen;
import com.gluecode.fpvdrone.gui.screen.addon.BackFooter;
import com.gluecode.fpvdrone.gui.screen.addon.WizardHeader;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class ChooseControllerScreen extends FpvScreen {
  private FPVList list;
  
  public ChooseControllerScreen(
    Screen previousScreen
  ) {
    super(
      previousScreen, new WizardHeader(I18n.get("fpvdrone.wizard.chooseController.title"), true), new BackFooter()
    );
    
    WizardHeader header = (WizardHeader) this.header;
    if (header != null) {
      header.addHelpQA(I18n.get("fpvdrone.wizard.chooseController.q1"), new StringTextComponent(I18n.get("fpvdrone.wizard.chooseController.a1")));
      
      StringTextComponent a2 = new StringTextComponent(I18n.get("fpvdrone.wizard.chooseController.a2") + " ");
      StringTextComponent a2url = new StringTextComponent(
        "How?");
      Style style = a2url.getStyle()
        .withClickEvent(new ClickEvent(
          ClickEvent.Action.OPEN_URL,
          "https://minecraftfpv.com/wiki/controllerVerify"
        ))
        .withColor(Color.fromLegacyFormat(TextFormatting.BLUE))
        .setUnderlined(true);
      a2url.setStyle(style);
      a2.append(a2url);
      header.addHelpQA(I18n.get("fpvdrone.wizard.chooseController.q2"), a2);
    }
  }
  
  public void handleIdSet(int id) {
    ControllerReader.setControllerId(id);
    GuiEvents.openCalibrateControllerStickScreen(this);
  }
  
  @Override
  protected void init() {
    super.init();
    this.list = new ControllerChoicesList(
      this,
      this::handleIdSet
    );
    this.children.add(this.list);
  }
  
  @Override
  public void renderCustom(
    PoseStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.list.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
