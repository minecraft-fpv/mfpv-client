package com.gluecode.fpvdrone.gui.screen.wizard;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.screen.EmptyListScreen;
import com.gluecode.fpvdrone.gui.screen.addon.BackProceedFooter;
import com.gluecode.fpvdrone.gui.screen.addon.WizardHeader;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.input.KeyManager;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.KeyMappingList;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class CalibrateKeyboardScreen extends EmptyListScreen {
  private Button keyButton;
  private boolean listening;
  
  public CalibrateKeyboardScreen(
    Screen previousScreen
  ) {
    super(previousScreen, new WizardHeader(null, true), new BackProceedFooter());
  
    BackProceedFooter footer = (BackProceedFooter) this.footer;
    if (footer != null) {
      footer.completeConstructor(this::onProceed, null, null);
    }
  }
  
  public void onProceed() {
    GuiEvents.openCompleteKeyScreen(this);
  }
  
  public void handleKeyButtonClick(Button button) {
    if (!this.listening) {
      this.listening = true;
    } else {
      this.listening = false;
      
    }
  }
  
  @Override
  public boolean keyPressed(
    int keyCode,
    int scanCode,
    int modifiers
  ) {
    // copied from ControlsScreen.keyPressed:
    if (this.listening) {
      if (keyCode == 256) {
        KeyManager.armKey.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.UNKNOWN);
        this.options.setKey(KeyManager.armKey, InputMappings.UNKNOWN);
      } else {
        KeyManager.armKey.setKeyModifierAndCode(net.minecraftforge.client.settings.KeyModifier.getActiveModifier(), InputMappings.getKey(keyCode, scanCode));
        this.options.setKey(KeyManager.armKey, InputMappings.getKey(keyCode, scanCode));
      }

      if (!net.minecraftforge.client.settings.KeyModifier.isKeyCodeModifier(KeyManager.armKey.getKey())) {
        this.listening = false;
      }
      KeyMapping.resetMapping();
      return true;
    } else {
      return super.keyPressed(keyCode, scanCode, modifiers);
    }
  }
  
  @Override
  protected void init() {
    super.init();
    
//    this.addWidget(new KeyMappingList.KeyEntry(InputHandler.armKey, new StringTextComponent("huh?")));
    Minecraft minecraft = Minecraft.getInstance();
    String armLabel = I18n.get("key.fpvdrone.arm");
    int labelWidth = minecraft.font.width(armLabel);
    this.keyButton = new Button(
      this.width / 2 - (WizardConfig.shortButtonWidth + 6 + labelWidth) / 2,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 20 + WizardConfig.doubleButtonSpacing + 5,
      WizardConfig.shortButtonWidth,
      20,
      KeyManager.armKey.getTranslatedKeyMessage(),
      this::handleKeyButtonClick
    );
  
    this.addButton(this.keyButton);
  }
  
  @Override
  public void renderCustom(
    PoseStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    super.renderCustom(matrixStack, mouseX, mouseY, partialTicks);
  
    Minecraft minecraft = Minecraft.getInstance();
    
    String top = I18n.get("fpvdrone.wizard.calibrateControllerKey.top");
    String bottom = I18n.get("fpvdrone.wizard.calibrateControllerKey.bottom");
    
    int topWidth = minecraft.font.width(top);
    int bottomWidth = minecraft.font.width(bottom);
  
    minecraft.font.draw(
      matrixStack,
      top,
      this.width / 2f - topWidth / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop,
      0xFFFFFF
    );
  
    minecraft.font.draw(
      matrixStack,
      bottom,
      this.width / 2f - bottomWidth / 2f,
      WizardConfig.headerHeight + WizardConfig.contentTop + 2 * minecraft.font.lineHeight * WizardConfig.lineHeight,
      0xFFFFFF
    );
    
    
    this.keyButton.setMessage(KeyManager.armKey.getTranslatedKeyMessage());
    
    if (listening) {
      this.keyButton.setMessage((new StringTextComponent("> ")).append(this.keyButton.getMessage().copy().withStyle(
        TextFormatting.YELLOW)).append(" <").withStyle(TextFormatting.YELLOW));
    } else {
      boolean[] conflicts = this.getConflicts();
      if (conflicts[0]) {
        boolean keyCodeModifierConflict = conflicts[1];
        this.keyButton.setMessage(this.keyButton.getMessage().copy().withStyle(keyCodeModifierConflict ? TextFormatting.GOLD : TextFormatting.RED));
      }
    }
  
    String armLabel = I18n.get("key.fpvdrone.arm");
    int labelWidth = minecraft.font.width(armLabel);
    minecraft.font.draw(
      matrixStack,
      armLabel,
      this.width / 2f - (WizardConfig.shortButtonWidth + 6 + labelWidth) / 2f + WizardConfig.shortButtonWidth + 6,
      WizardConfig.headerHeight + WizardConfig.contentTop + WizardConfig.titleSpacing + 20 + WizardConfig.doubleButtonSpacing + 5 + minecraft.font.lineHeight / 2f + 1,
      0xFFFFFF
    );
  }
  
  // This code is copied from KeyMappingList.KeyEntry.render:
  public boolean[] getConflicts() {
    boolean[] results = new boolean[2];
    
    results[0] = false;
    results[1] = true; // less severe form of conflict, like SHIFT conflicting with SHIFT+G
    if (!KeyManager.armKey.isUnbound()) {
      Minecraft minecraft = Minecraft.getInstance();
      for(KeyMapping keybinding : minecraft.options.keyMappings) {
        if (keybinding != KeyManager.armKey && KeyManager.armKey.same(keybinding)) {
          results[0] = true;
          results[1] &= KeyManager.armKey.hasKeyCodeModifierConflict(keybinding);
        }
      }
    }
    
    return results;
  }
}
