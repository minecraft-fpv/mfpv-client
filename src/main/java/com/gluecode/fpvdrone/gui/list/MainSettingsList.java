package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.DoubleButtonEntry;
import com.gluecode.fpvdrone.gui.entry.SingleButtonEntry;
import com.gluecode.fpvdrone.gui.screen.MainSettingsScreen;
import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.SettingsLoader;
import net.minecraft.client.resources.I18n;

public class MainSettingsList extends FPVList {
  private int maxListLabelWidth;
  
  public MainSettingsList(MainSettingsScreen parentScreen) {
    // field_230708_k_ = width
    // field_230709_l_ = height
    super(parentScreen);
    
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.settings.controller")));
    this.addEntry(new SingleButtonEntry(this, () -> {
      String name = ControllerReader.getControllerName(ControllerReader.getControllerId());
      if (name.equalsIgnoreCase("")) {
        return I18n.get("fpvdrone.settings.nocontroller");
      } else {
        return name;
      }
    }, () -> false, () -> {
      GuiEvents.openControllerChoicesScreen(parentScreen);
    }));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(
      this,
      () -> I18n.get("fpvdrone.settings.model")
    ));
    this.addEntry(new DoubleButtonEntry(
      this,
      () -> {
        String name = SettingsLoader.currentModel;
        return name;
      },
      I18n.get("fpvdrone.settings.config"),
      () -> {
        GuiEvents.openModelSettingsScreen(parentScreen);
      },
      I18n.get("fpvdrone.settings.choose"),
      () -> {
        GuiEvents.openModelChoicesScreen(parentScreen);
      },
      () -> false
    ));
  }
}
