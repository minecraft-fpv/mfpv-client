package com.gluecode.fpvdrone.gui.screen.addon;

import com.gluecode.fpvdrone.gui.screen.FpvScreen;
import com.gluecode.fpvdrone.gui.screen.wizard.WizardConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;

public class ServerTitleWikiHeader extends ScreenAddon {
  String title;
  
  public ServerTitleWikiHeader(String title) {
    this.title = title;
  }
  
  @Override
  public void init(FpvScreen screen) {
    screen.addButton(new Button(
      WizardConfig.left,
      WizardConfig.headerTop,
      WizardConfig.shortButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.settings.server")),
      (p_244738_1_) -> {
        screen.getMinecraft().setScreen(new ConfirmOpenLinkScreen((p_244739_1_) -> {
          if (p_244739_1_) {
            Util.getPlatform().openUri(
              "https://minecraftfpv.com/wiki/joiningServer");
          }
          screen.getMinecraft().setScreen(screen);
        }, "https://minecraftfpv.com/wiki/joiningServer", true));
      }
    ));
  
    screen.addButton(new Button(
      screen.width - WizardConfig.right - WizardConfig.shortButtonWidth,
      WizardConfig.headerTop,
      WizardConfig.shortButtonWidth,
      20,
      new StringTextComponent(I18n.get("fpvdrone.settings.wiki")),
      (p_244738_1_) -> {
        screen.getMinecraft().setScreen(new ConfirmOpenLinkScreen((p_244739_1_) -> {
          if (p_244739_1_) {
            Util.getPlatform()
              .openUri("https://minecraftfpv.com/wiki");
          }
  
          screen.getMinecraft().setScreen(screen);
        }, "https://minecraftfpv.com/wiki", true));
      }
    ));
  }
  
  @Override
  public void render(
    FpvScreen screen, MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks
  ) {
    AbstractGui.drawCenteredString(matrixStack, screen.getMinecraft().font, this.title,
      screen.width / 2,
      WizardConfig.headerTop + 20 / 2 - screen.getMinecraft().font.lineHeight / 2,
      0xFFFFFF
    );
//    AbstractGui.drawCenteredString(
//      matrixStack,
//      screen.getMinecraft().font,
//      InputHandler.getControllerName(InputHandler.getControllerId())
//        .equalsIgnoreCase("") ? I18n.get(
//        "fpvdrone.settings.nocontroller") :
//        InputHandler.getControllerName(InputHandler.getControllerId()),
//      screen.width / 2,
//      26,
//      0xFFFFFF
//    );
  }
}
