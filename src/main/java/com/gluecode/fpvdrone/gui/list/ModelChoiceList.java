package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.DoubleButtonEntry;
import com.gluecode.fpvdrone.gui.screen.ModelChoicesScreen;
import com.gluecode.fpvdrone.util.SettingsLoader;
import net.minecraft.client.resources.I18n;

import java.util.*;

public class ModelChoiceList extends FPVList {
  public ModelChoiceList(
    ModelChoicesScreen parentScreen
  ) {
    // field_230708_k_ = width
    super(parentScreen);

    this.addEntry(new CategoryEntry(this, () -> I18n.get("fpvdrone.model.choose")));

    this.addEntry(new DoubleButtonEntry(
      this,
      () -> SettingsLoader.defaultModelName,
      I18n.get("fpvdrone.settings.select"),
      () -> {
        SettingsLoader.loadModel(SettingsLoader.defaultModelName);
        SettingsLoader.save();
      },
      I18n.get("fpvdrone.settings.delete"),
      () -> {
        SettingsLoader.models.remove(SettingsLoader.defaultModelName);
        if (SettingsLoader.currentModel.equals(SettingsLoader.defaultModelName)) {
          SettingsLoader.loadModel(SettingsLoader.defaultModelName);
        }
        SettingsLoader.save();
      },
      () -> SettingsLoader.currentModel.equals(SettingsLoader.defaultModelName)
    ));
    this.addEntry(new DoubleButtonEntry(
      this,
      () -> SettingsLoader.defaultWhoop,
      I18n.get("fpvdrone.settings.select"),
      () -> {
        SettingsLoader.loadModel(SettingsLoader.defaultWhoop);
        SettingsLoader.save();
      },
      I18n.get("fpvdrone.settings.delete"),
      () -> {
        SettingsLoader.models.remove(SettingsLoader.defaultWhoop);
        if (SettingsLoader.currentModel.equals(SettingsLoader.defaultWhoop)) {
          SettingsLoader.loadModel(SettingsLoader.defaultWhoop);
        }
        SettingsLoader.save();
      },
      () -> SettingsLoader.currentModel.equals(SettingsLoader.defaultWhoop)
    ));

    List keys = new ArrayList(SettingsLoader.models.keySet());
    Collections.sort(keys);
    for (Object key : keys) {
      String presetName = (String) key;

      if (presetName.equals(SettingsLoader.defaultModelName)) continue;
      if (presetName.equals(SettingsLoader.defaultWhoop)) continue;

      this.addEntry(new DoubleButtonEntry(
        this,
        () -> presetName,
        I18n.get("fpvdrone.settings.select"),
        () -> {
          SettingsLoader.loadModel(presetName);
          SettingsLoader.save();
        },
        I18n.get("fpvdrone.settings.delete"),
        () -> {
          SettingsLoader.models.remove(presetName);
          if (SettingsLoader.currentModel.equals(presetName)) {
            SettingsLoader.loadModel(presetName);
          }
          SettingsLoader.save();
        },
        () -> SettingsLoader.currentModel.equals(presetName)
      ));
    }
  }
}
