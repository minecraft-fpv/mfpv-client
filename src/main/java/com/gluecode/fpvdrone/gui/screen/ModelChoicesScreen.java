package com.gluecode.fpvdrone.gui.screen;

import com.gluecode.fpvdrone.gui.list.ModelChoiceList;
import com.gluecode.fpvdrone.gui.screen.addon.DoneFooter;
import com.gluecode.fpvdrone.gui.screen.addon.ServerTitleWikiHeader;
import com.gluecode.fpvdrone.gui.screen.wizard.WizardConfig;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;

public class ModelChoicesScreen extends FpvScreen {
  private ModelChoiceList list = null;
  private String initName = I18n.get("fpvdrone.model.new");
  private String editValue = initName;
  private boolean editMode = false;
  //  private Button createButton;
  private Button nameButton;
  
  public ModelChoicesScreen(
    Screen previousScreen
  ) {
    super(
      previousScreen,
      new ServerTitleWikiHeader(I18n.get("fpvdrone.model.active")),
      new DoneFooter()
    );
    MinecraftForge.EVENT_BUS.register(this);
  }
  
  @SubscribeEvent
  public void charTyped(GuiScreenEvent.KeyboardCharTypedEvent.Pre event) {
    if (editMode) {
      if (this.editValue.length() < 24) {
        this.editValue = this.editValue + event.getCodePoint();
      }
    }
  }
  
  @Override
  protected void init() {
    super.init();
    this.generateList();
    
    this.nameButton = this.addButton(new Button(
      WizardConfig.left,
      this.height - 20 - WizardConfig.footerBottom,
      WizardConfig.wideButtonWidth * 2,
      20,
      new StringTextComponent(this.editValue),
      this::handleNewPress
    ));
  }
  
  public void generateList() {
    if (this.list != null) {
      this.children.remove(this.list);
    }
    
    this.list = new ModelChoiceList(this);
    this.children.add(this.list);
  }
  
  public boolean keyPressed(
    int p_keyPressed_1_,
    int p_keyPressed_2_,
    int p_keyPressed_3_
  ) {
    InputMappings.Input input = InputMappings.getKey(
      p_keyPressed_1_,
      p_keyPressed_2_
    );
    if (editMode) {
      if (input.toString().equals("key.keyboard.backspace")) {
        if (this.editValue.length() > 0) {
          this.editValue = this.editValue.substring(
            0,
            this.editValue.length() - 1
          );
        }
        return true;
      } else if (input.toString().equals("key.keyboard.space")) {
        return true;
      }
    }
    return super.keyPressed(
      p_keyPressed_1_,
      p_keyPressed_2_,
      p_keyPressed_3_
    );
  }
  
  public void handleNewPress(@Nullable Widget button) {
    this.editMode = !this.editMode;
    if (editMode) {
      this.editValue = "";
    } else {
      String attemptName = this.editValue.trim();
      if (!attemptName.equals("")) {
        Object existing = SettingsLoader.models.get(attemptName);
        if (existing == null) {
          SettingsLoader.loadModel(SettingsLoader.defaultModelName);
          SettingsLoader.currentModel = attemptName;
          SettingsLoader.save();
          this.generateList();
        }
      }
      this.editValue = initName;
    }
  }
  
  @Override
  public void renderCustom(
    MatrixStack matrixStack,
    int mouseX,
    int mouseY,
    float partialTicks
  ) {
    this.list.render(matrixStack, mouseX, mouseY, partialTicks);
    this.nameButton.setMessage(
      new StringTextComponent(this.editMode ? "> " +
                                              this.editValue +
                                              "_ <" : this.editValue));
    this.nameButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
}
