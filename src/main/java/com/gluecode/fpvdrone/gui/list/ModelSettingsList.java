package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.gui.GuiEvents;
import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.DoubleNavEntry;
import com.gluecode.fpvdrone.gui.entry.SingleButtonEntry;
import com.gluecode.fpvdrone.gui.screen.ModelSettingsScreen;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.SettingsLoader;
import net.minecraft.client.resources.I18n;

public class ModelSettingsList extends FPVList {
  public ModelSettingsList(ModelSettingsScreen parentScreen) {
    // field_230708_k_ = width
    // field_230709_l_ = height
    super(parentScreen);
  
    this.addEntry(new CategoryEntry(
      this,
      () -> I18n.get("fpvdrone.settings.model")
    ));
    this.addEntry(new SingleButtonEntry(
      this,
      () -> SettingsLoader.currentModel,
      () -> false,
      () -> {
        GuiEvents.openModelChoicesScreen(this.parentScreen);
      }
    ));
    this.addEntry(new CategoryEntry(this, () -> ""));
  
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
      GuiEvents.openControllerChoicesScreen(this.parentScreen);
    }));
    this.addEntry(new CategoryEntry(this, () -> ""));
    
    this.addEntry(new DoubleNavEntry(
      this,
      I18n.get("fpvdrone.settings.channel"),
      () -> {
        GuiEvents.openChannelMappingScreen(parentScreen);
      },
      I18n.get("fpvdrone.settings.rates"),
      () -> {
        GuiEvents.openRatesScreen(parentScreen);
      }
    ));
    this.addEntry(new DoubleNavEntry(
      this,
      I18n.get("fpvdrone.settings.build"),
      () -> {
        GuiEvents.openDroneBuildScreen(parentScreen);
      },
      I18n.get("fpvdrone.settings.other"),
      () -> {
        GuiEvents.openMiscSettingsScreen(parentScreen);
      }
    ));
    this.addEntry(new CategoryEntry(this, () -> ""));
  }
}
