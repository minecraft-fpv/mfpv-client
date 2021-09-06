package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.render.CameraManager;
import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.gui.entry.BooleanEntry;
import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.FloatEntry;
import com.gluecode.fpvdrone.gui.screen.OtherSettingsScreen;
import com.gluecode.fpvdrone.util.SettingsLoader;
import net.minecraft.client.resources.I18n;

public class OtherSettingsList extends FPVList {
  public OtherSettingsList(OtherSettingsScreen parentScreen) {
    // field_230708_k_ = width
    // field_230709_l_ = height
    super(parentScreen);
    
    this.addEntry(new CategoryEntry(this, () -> I18n.get("fpvdrone.settings.other")));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.other.crosshair"),
      CameraManager::getShowCrosshairs,
      CameraManager::setShowCrosshairs,
      CameraManager::getDefaultShowCrosshairs,
      CameraManager::resetShowCrosshairs
    ));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.other.showBlockOutline"),
      CameraManager::getShowBlockOutline,
      CameraManager::setShowBlockOutline,
      CameraManager::getDefaultShowBlockOutline,
      CameraManager::resetShowBlockOutline
    ));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.other.showStickOverlay"),
      CameraManager::getShowStickOverlay,
      CameraManager::setShowStickOverlay,
      CameraManager::getDefaultShowStickOverlay,
      CameraManager::resetShowStickOverlay
    ));
    this.addEntry(new BooleanEntry(
        this,
        I18n.get("fpvdrone.other.flightMode3d"),
        DroneBuild::getFlightMode3d,
        DroneBuild::setFlightMode3d,
        DroneBuild::getDefaultFlightMode3d,
        DroneBuild::resetFlightMode3d
    ));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.other.angle"),
      DroneBuild::getSwitchlessAngle,
      DroneBuild::setSwitchlessAngle,
      DroneBuild::getDefaultSwitchlessAngle,
      DroneBuild::resetSwitchlessAngle,
      false,
      () -> 0f,
      () -> 0f,
      () -> false
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
    this.addEntry(new CategoryEntry(this, () -> I18n.get("fpvdrone.settings.other")));
    this.addEntry(new FloatEntry(
      this,
      I18n.get("fpvdrone.other.fov"),
      () -> SettingsLoader.currentFov,
      (fov) -> {
        SettingsLoader.currentFov = fov;
      },
      () -> SettingsLoader.defaultFov,
      () -> {
        SettingsLoader.currentFov = SettingsLoader.defaultFov;
      },
      true,
      () -> 30f,
      () -> 150f,
      () -> false
    ));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.other.fisheye"),
      () -> SettingsLoader.currentUseFisheye,
      (useFisheye) -> {
        SettingsLoader.currentUseFisheye = useFisheye;
      },
      () -> SettingsLoader.defaultUseFisheye,
      () -> {
        SettingsLoader.currentUseFisheye = SettingsLoader.defaultUseFisheye;
      }
    ));
//    this.addEntry(new BooleanEntry(
//      this,
//      I18n.get("fpvdrone.other.realtime"),
//      () -> SettingsLoader.currentUseRealtimePhysics,
//      (useRealtimePhysics) -> {
//        SettingsLoader.currentUseRealtimePhysics = useRealtimePhysics;
//      },
//      () -> SettingsLoader.defaultUseRealtimePhysics,
//      () -> {
//        SettingsLoader.currentUseRealtimePhysics = SettingsLoader.defaultUseRealtimePhysics;
//      }
//    ));
    this.addEntry(new BooleanEntry(
      this,
      I18n.get("fpvdrone.other.showWizard"),
      () -> SettingsLoader.firstTimeSetup,
      (value) -> {
        SettingsLoader.firstTimeSetup = value;
      },
      () -> true,
      () -> {
        SettingsLoader.firstTimeSetup = true;
      }
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
  }
}
