package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.gui.screen.EmptyListScreen;
import com.gluecode.fpvdrone.gui.screen.addon.BackHelpFooter;
import com.gluecode.fpvdrone.gui.screen.addon.WizardHeader;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.ConfirmOpenLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;

public class HelpQAScreen extends EmptyListScreen {
  private LinkedHashMap<String, ITextComponent> qa;
  
  public HelpQAScreen(
    Screen previousScreen,
    LinkedHashMap<String, ITextComponent> qa
  ) {
    super(previousScreen, null, new BackHelpFooter());
    this.qa = qa;
  }
  
  @Override
  protected void init() {
    super.init();
  }
  
  @Override
  public void renderCustom(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    super.renderCustom(matrixStack, mouseX, mouseY, partialTicks);
    
    Minecraft minecraft = Minecraft.getInstance();
    float lineMult = WizardConfig.lineHeight;
    int lineHeight = (int) (lineMult * minecraft.font.lineHeight);
    
    String[] questions = this.qa.keySet().toArray(new String[0]);
    for (int i = 0; i < questions.length; i++) {
      int y = 3 * lineHeight * i;
      String q = questions[i];
      ITextComponent a = this.qa.get(q);
      StringTextComponent answer = new StringTextComponent("A: ");
      answer.append(a);
      
      minecraft.font.draw(
        matrixStack,
        "Q: " + q,
        3 * WizardConfig.left,
        WizardConfig.headerHeight + WizardConfig.contentTop + y,
        0xFFFFFF
      );
      
      minecraft.font.draw(
        matrixStack,
        answer,
        4 * WizardConfig.left,
        WizardConfig.headerHeight + WizardConfig.contentTop + y + lineHeight,
        0xFFFFFF
      );
    }
  }
  
  public @Nullable
  Style getStyleAt(double mouseX, double mouseY) {
    // Find if any links were clicked.
    String[] questions = this.qa.keySet().toArray(new String[0]);
    
    Minecraft minecraft = Minecraft.getInstance();
    float lineMult = WizardConfig.lineHeight;
    int lineHeight = (int) (lineMult * minecraft.font.lineHeight);
    
    for (int i = 0; i < questions.length; i++) {
      int y = 3 * lineHeight * i;
      String q = questions[i];
      ITextComponent a = this.qa.get(q);
      StringTextComponent answer = new StringTextComponent("A: ");
      answer.append(a);
      
      int answerY = WizardConfig.headerHeight +
                    WizardConfig.contentTop +
                    y +
                    lineHeight;
      
      if (answerY <= mouseY && mouseY <= answerY + minecraft.font.lineHeight) {
        // This answer is on the same line as the mouseClick.
        // Do not worry about line wrapping. It will not be supported.
        
        int answerX = 4 * WizardConfig.left;
        Style style = minecraft.font.getSplitter().componentStyleAtWidth(
          answer,
          (int) (mouseX - answerX)
        );
        
        if (style != null) {
          return style;
        }
      }
    }
    return null;
  }
  
  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int p_231044_5_) {
    Style style = this.getStyleAt(mouseX, mouseY);
    
    if (
      style != null &&
      style.getClickEvent() != null &&
      style.getClickEvent().getAction().equals(ClickEvent.Action.OPEN_URL)
    ) {
      String url = style.getClickEvent().getValue();
      this.handleLinkClick(url);
      return true;
    }
    
    return super.mouseClicked(mouseX, mouseY, p_231044_5_);
  }
  
  public void handleLinkClick(String url) {
    this.getMinecraft().setScreen(new ConfirmOpenLinkScreen((p_244739_1_) -> {
      if (p_244739_1_) {
        Util.getPlatform().openUri(
          url);
      }
      this.getMinecraft().setScreen(this);
    }, url, true));
  }
}
