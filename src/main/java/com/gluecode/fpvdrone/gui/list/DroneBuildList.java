package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.gui.entry.BooleanEntry;
import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.FloatEntry;
import com.gluecode.fpvdrone.gui.entry.IntEntry;
import com.gluecode.fpvdrone.gui.screen.DroneBuildScreen;
import com.gluecode.fpvdrone.physics.PhysicsConstants;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.gluecode.fpvdrone.util.Transforms;
import net.minecraft.client.resources.I18n;

public class DroneBuildList extends FPVList {
  public DroneBuildList(DroneBuildScreen parentScreen) {
    super(parentScreen);
    
    this.addEntry(new CategoryEntry(
      this,
      () -> I18n.get("fpvdrone.settings.build")
    ));
    
    if (SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)) {
      this.addEntry(new CategoryEntry(
        this, () -> I18n.get(
        "fpvdrone.build.error.default"))
      );
      this.addEntry(new CategoryEntry(this, () -> ""));
    }
    
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.mass"),
      () -> {
        return DroneBuild.getDroneMass() * 1000f;
      },
      (value) -> {
        DroneBuild.setDroneMass(value / 1000f);
      },
      () -> {
        return DroneBuild.getDefaultDroneMass() * 1000f;
      },
      DroneBuild::resetDroneMass,
      true,
      () -> {
        return DroneBuild.getMinDroneMass() * 1000f;
      },
      () -> {
        return DroneBuild.getMaxDroneMass() * 1000f;
      },
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.build.category.battery")));
    
    this.addEntry(new IntEntry(
      this,
      I18n.get("fpvdrone.build.cells"),
      DroneBuild::getBatteryCells,
      DroneBuild::setBatteryCells,
      DroneBuild::getDefaultBatteryCells,
      DroneBuild::resetBatteryCells
    ));
    this.addEntry(new IntEntry(
      this,
      I18n.get("fpvdrone.build.batteryMah"),
      DroneBuild::getBatteryMah,
      DroneBuild::setBatteryMah,
      DroneBuild::getDefaultBatteryMah,
      DroneBuild::resetBatteryMah
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.build.category.motors")));
    
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.kv"),
      () -> {
        return DroneBuild.getMotorKv() / PhysicsConstants.toSIKv;
      },
      (value) -> {
        DroneBuild.setMotorKv(value * PhysicsConstants.toSIKv);
      },
      () -> {
        return DroneBuild.getDefaultMotorKv() / PhysicsConstants.toSIKv;
      },
      DroneBuild::resetMotorKv,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.motorWidth"),
      Transforms.getMillimeter(DroneBuild::getMotorWidth),
      Transforms.setMillimeter(DroneBuild::setMotorWidth),
      Transforms.getMillimeter(DroneBuild::getDefaultMotorWidth),
      DroneBuild::resetMotorWidth,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.motorHeight"),
      Transforms.getMillimeter(DroneBuild::getMotorHeight),
      Transforms.setMillimeter(DroneBuild::setMotorHeight),
      Transforms.getMillimeter(DroneBuild::getDefaultMotorHeight),
      DroneBuild::resetMotorHeight,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.build.category.props")));
    
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.diameter"),
      Transforms.getInches(DroneBuild::getPropDiameter),
      Transforms.setInches(DroneBuild::setPropDiameter),
      Transforms.getInches(DroneBuild::getDefaultPropDiameter),
      DroneBuild::resetPropDiameter,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.pitch"),
      Transforms.getInches(DroneBuild::getPropPitch),
      Transforms.setInches(DroneBuild::setPropPitch),
      Transforms.getInches(DroneBuild::getDefaultPropPitch),
      DroneBuild::resetPropPitch,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.propWidth"),
      Transforms.getMillimeter(DroneBuild::getPropWidth),
      Transforms.setMillimeter(DroneBuild::setPropWidth),
      Transforms.getMillimeter(DroneBuild::getDefaultPropWidth),
      DroneBuild::resetPropWidth,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new IntEntry(
      this,
      I18n.get("fpvdrone.build.blades"),
      DroneBuild::getBlades,
      DroneBuild::setBlades,
      DroneBuild::getDefaultBlades,
      DroneBuild::resetBlades
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.build.category.frame")));
    
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.frameWidth"),
      Transforms.getMillimeter(DroneBuild::getFrameWidth),
      Transforms.setMillimeter(DroneBuild::setFrameWidth),
      Transforms.getMillimeter(DroneBuild::getDefaultFrameWidth),
      DroneBuild::resetFrameWidth,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.frameHeight"),
      Transforms.getMillimeter(DroneBuild::getFrameHeight),
      Transforms.setMillimeter(DroneBuild::setFrameHeight),
      Transforms.getMillimeter(DroneBuild::getDefaultFrameHeight),
      DroneBuild::resetFrameHeight,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.frameLength"),
      Transforms.getMillimeter(DroneBuild::getFrameLength),
      Transforms.setMillimeter(DroneBuild::setFrameLength),
      Transforms.getMillimeter(DroneBuild::getDefaultFrameLength),
      DroneBuild::resetFrameLength,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.armWidth"),
      Transforms.getMillimeter(DroneBuild::getArmWidth),
      Transforms.setMillimeter(DroneBuild::setArmWidth),
      Transforms.getMillimeter(DroneBuild::getDefaultArmWidth),
      DroneBuild::resetArmWidth,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.armThickness"),
      Transforms.getMillimeter(DroneBuild::getArmThickness),
      Transforms.setMillimeter(DroneBuild::setArmThickness),
      Transforms.getMillimeter(DroneBuild::getDefaultArmThickness),
      DroneBuild::resetArmThickness,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.build.category.accessories")));
    
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.antennaLength"),
      Transforms.getMillimeter(DroneBuild::getAntennaLength),
      Transforms.setMillimeter(DroneBuild::setAntennaLength),
      Transforms.getMillimeter(DroneBuild::getDefaultAntennaLength),
      DroneBuild::resetAntennaLength,
      false,
      () -> 0f,
      () -> 0f,
      () -> SettingsLoader.isDefaultPreset(SettingsLoader.currentModel)
    ));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.build.showProCam"),
      DroneBuild::getShowProCam,
      DroneBuild::setShowProCam,
      DroneBuild::getDefaultShowProCam,
      DroneBuild::resetShowProCam
    ));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.build.isHeroCam"),
      DroneBuild::getIsHeroCam,
      DroneBuild::setIsHeroCam,
      DroneBuild::getDefaultIsHeroCam,
      DroneBuild::resetIsHeroCam
    ));
    //    this.addEntry(new BooleanEntry(I18n.get("fpvdrone.build.isToothpick"), DroneBuild::getIsToothpick,
    //        DroneBuild::setIsToothpick, DroneBuild::getDefaultIsToothpick,
    //        DroneBuild::resetIsToothpick));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.build.category.colors")));
    
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.red"),
      DroneBuild::getRed,
      DroneBuild::setRed,
      DroneBuild::getDefaultRed,
      DroneBuild::resetRed,
      true,
      () -> 0f,
      () -> 1f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.green"),
      DroneBuild::getGreen,
      DroneBuild::setGreen,
      DroneBuild::getDefaultGreen,
      DroneBuild::resetGreen,
      true,
      () -> 0f,
      () -> 1f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.build.blue"),
      DroneBuild::getBlue,
      DroneBuild::setBlue,
      DroneBuild::getDefaultBlue,
      DroneBuild::resetBlue,
      true,
      () -> 0f,
      () -> 1f,
      () -> false
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
  }
}
