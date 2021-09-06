package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.gui.screen.RatesScreen;
import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.FloatEntry;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.util.Transforms;
import net.minecraft.client.resources.I18n;

public class RatesList extends FPVList {
  public RatesList(RatesScreen controls) {
    super(controls);
    
    this.addEntry(new CategoryEntry(this, () -> {
      float setpoint = Math.max(Math.abs(ControllerReader.getYaw()), 0);
      boolean isMax = setpoint < 0.05;
      float crate = ControllerConfig.getYawRate();
      float csuper = ControllerConfig.getYawSuper();
      float cexpo = ControllerConfig.getYawExpo();
      if (isMax) {
        float max = Transforms.bfRate(
          1,
          crate,
          csuper,
          cexpo
        );
        return I18n.get("fpvdrone.rates.yaw") + " (Max: " + Math.round(max) + " deg/s)";
      } else {
        float max = Transforms.bfRate(
          setpoint,
          crate,
          csuper,
          cexpo
        );
        return I18n.get("fpvdrone.rates.yaw") + " (" + Math.round(max) + " deg/s)";
      }
    }, 0xFF8040));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.rate"),
      ControllerConfig::getYawRate,
      ControllerConfig::setYawRate,
      ControllerConfig::getDefaultYawRate,
      ControllerConfig::resetYawRate,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.super"),
      ControllerConfig::getYawSuper,
      ControllerConfig::setYawSuper,
      ControllerConfig::getDefaultYawSuper,
      ControllerConfig::resetYawSuper,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.expo"),
      ControllerConfig::getYawExpo,
      ControllerConfig::setYawExpo,
      ControllerConfig::getDefaultYawExpo,
      ControllerConfig::resetYawExpo,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
  
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> {
      float setpoint = Math.max(Math.abs(ControllerReader.getPitch()), 0);
      boolean isMax = setpoint < 0.05;
      float crate = ControllerConfig.getPitchRate();
      float csuper = ControllerConfig.getPitchSuper();
      float cexpo = ControllerConfig.getPitchExpo();
      if (isMax) {
        float max = Transforms.bfRate(
          1,
          crate,
          csuper,
          cexpo
        );
        return I18n.get("fpvdrone.rates.pitch") + " (Max: " + Math.round(max) + " deg/s)";
      } else {
        float max = Transforms.bfRate(
          setpoint,
          crate,
          csuper,
          cexpo
        );
        return I18n.get("fpvdrone.rates.pitch") + " (" + Math.round(max) + " deg/s)";
      }
    }, 0x40FF80));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.rate"),
      ControllerConfig::getPitchRate,
      ControllerConfig::setPitchRate,
      ControllerConfig::getDefaultPitchRate,
      ControllerConfig::resetPitchRate,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.super"),
      ControllerConfig::getPitchSuper,
      ControllerConfig::setPitchSuper,
      ControllerConfig::getDefaultPitchSuper,
      ControllerConfig::resetPitchSuper,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.expo"),
      ControllerConfig::getPitchExpo,
      ControllerConfig::setPitchExpo,
      ControllerConfig::getDefaultPitchExpo,
      ControllerConfig::resetPitchExpo,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
  
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> {
      float setpoint = Math.max(Math.abs(ControllerReader.getRoll()), 0);
      boolean isMax = setpoint < 0.05;
      float crate = ControllerConfig.getRollRate();
      float csuper = ControllerConfig.getRollSuper();
      float cexpo = ControllerConfig.getRollExpo();
      if (isMax) {
        float max = Transforms.bfRate(
          1,
          crate,
          csuper,
          cexpo
        );
        return I18n.get("fpvdrone.rates.roll") + " (Max: " + Math.round(max) + " deg/s)";
      } else {
        float max = Transforms.bfRate(
          setpoint,
          crate,
          csuper,
          cexpo
        );
        return I18n.get("fpvdrone.rates.roll") + " (" + Math.round(max) + " deg/s)";
      }
    }, 0x4080FF));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.rate"),
      ControllerConfig::getRollRate,
      ControllerConfig::setRollRate,
      ControllerConfig::getDefaultRollRate,
      ControllerConfig::resetRollRate,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.super"),
      ControllerConfig::getRollSuper,
      ControllerConfig::setRollSuper,
      ControllerConfig::getDefaultRollSuper,
      ControllerConfig::resetRollSuper,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.rates.expo"),
      ControllerConfig::getRollExpo,
      ControllerConfig::setRollExpo,
      ControllerConfig::getDefaultRollExpo,
      ControllerConfig::resetRollExpo,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
  }
  
  @Override
  public int getLeftPadding() {
    int chartSize = this.parentScreen.height - 32 - 43 - 12 * 2;
    return 30 + chartSize + 30 + 30;
  }
  
  @Override
  public int getRightPadding() {
    return 30;
  }
}
