package com.gluecode.fpvdrone.gui.entry;

import com.gluecode.fpvdrone.gui.list.FPVList;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ChannelEntry extends FPVEntry {
  private IntSupplier getChannel;
  private IntConsumer setNewValue;
  private BooleanSupplier getInverted;
  private BooleanConsumer setInverted;
  private Runnable setDefaultValue;
  private IntSupplier getDefaultValue;
  private Button changeButton;
  private Button invertButton;
  private Button resetButton;
  
  public ChannelEntry(
    FPVList list,
    String name,
    IntSupplier getChannel,
    IntConsumer setNewValue,
    BooleanSupplier getInverted,
    BooleanConsumer setInverted,
    IntSupplier getDefaultValue,
    Runnable setDefaultValue
  ) {
    super(list, name);
    this.name = name;
    this.getChannel = getChannel;
    this.setNewValue = setNewValue;
    this.getInverted = getInverted;
    this.setInverted = setInverted;
    this.getDefaultValue = getDefaultValue;
    this.setDefaultValue = setDefaultValue;
    this.changeButton = new Button(
      0,
      0,
      50,
      20,
      new StringTextComponent(name),
      this::handleChangePress
    );
    this.invertButton = new Button(
      0,
      0,
      50,
      20,
      new StringTextComponent(I18n.get(
        "fpvdrone.channel.notinverted")),
      this::handleInvertPress
    );
    this.resetButton = new Button(
      0,
      0,
      50,
      20,
      new StringTextComponent(I18n.get("controls.reset")),
      this::handleResetPress
    );
    this.editMode = false;
  }

  @Override
  public int maxValueLength() {
    return 2;
  }
  
  @Override
  public boolean isLetterAcceptable(String letter) {
    switch (letter) {
      case "0":
      case "1":
      case "2":
      case "3":
      case "4":
      case "5":
      case "6":
      case "7":
      case "8":
      case "9":
        return true;
      default:
        return false;
    }
  }
  
  @Override
  public void betterRender(
    MatrixStack matrixStack,
    FontRenderer fontRenderer,
    int rowIndex,
    int rowTop,
    int rowLeft,
    int rowWidth,
    int rowHeight,
    int mouseX,
    int mouseY,
    boolean isMouseOver,
    float partialTicks
  ) {
    fontRenderer.draw(
      matrixStack,
      this.name,
      rowLeft,
      (float) (rowTop + 6),
      16777215
    );
  
    int right = rowLeft + rowWidth;
    int resetWidth = this.resetButton.getWidth();
    int invertWidth = this.invertButton.getWidth();
    int changeWidth = this.changeButton.getWidth();
    
    this.changeButton.x = right - resetWidth - 1 - invertWidth - 1 - changeWidth;
    this.changeButton.y = rowTop;
    this.invertButton.x = right - resetWidth - 1 - invertWidth;
    this.invertButton.y = rowTop;
    this.resetButton.x = right - resetWidth;
    this.resetButton.y = rowTop;
    
    // field_230693_o_ = active
    this.resetButton.active = this.getDefaultValue.getAsInt() !=
                              this.getChannel.getAsInt();
    
    if (this.getInverted.getAsBoolean()) {
      // func_238482_a_ = setMessage
      this.invertButton.setMessage(new StringTextComponent(I18n.get(
        "fpvdrone.channel.inverted")));
    } else {
      this.invertButton.setMessage(new StringTextComponent(I18n.get(
        "fpvdrone.channel.notinverted")));
    }
    
    if (this.editMode) {
      this.changeButton.setMessage(new StringTextComponent("> CH " +
                                                           editValue +
                                                           "_ <"));
    } else {
      int channel = this.getChannel.getAsInt();
      String channelName = "CH " + (channel + 1);
      this.changeButton.setMessage(new StringTextComponent(channelName));
    }
    
    this.changeButton.render(matrixStack, mouseX, mouseY, partialTicks);
    this.invertButton.render(matrixStack, mouseX, mouseY, partialTicks);
    this.resetButton.render(matrixStack, mouseX, mouseY, partialTicks);
  }
  
  @Override
  public List<? extends IGuiEventListener> children() {
    return ImmutableList.of(
      this.changeButton,
      this.invertButton,
      this.resetButton
    );
  }
  
  @Override
  public void handleChangePress(@Nullable Widget button) {
    super.handleChangePress(button);
    
    this.editMode = !this.editMode;
    if (editMode) {
      this.editValue = "";
      ControllerReader.startInputListening((int channel) -> {
        if (this.list.activeEntry != this) return;
        if (!this.editMode) return;
        setNewValue.accept(channel);
        this.editMode = false;
        SettingsLoader.save();
      });
    } else {
      if (!this.editValue.equals("")) {
        int channel = Integer.parseInt(this.editValue) - 1;
        setNewValue.accept(channel);
        SettingsLoader.save();
      }
    }
  }
  
  public void handleInvertPress(Button button) {
    this.setInverted.accept(!this.getInverted.getAsBoolean());
  }
  
  public void handleResetPress(Button button) {
    this.editMode = false;
    this.setDefaultValue.run();
    SettingsLoader.save();
  }
}
