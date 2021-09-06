package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.gui.entry.CategoryEntry;
import com.gluecode.fpvdrone.gui.entry.ChannelEntry;
import com.gluecode.fpvdrone.gui.screen.ChannelMappingScreen;
import com.gluecode.fpvdrone.input.ControllerConfig;
import com.gluecode.fpvdrone.input.ControllerReader;
import net.minecraft.client.resources.I18n;

import java.util.function.*;

public class ChannelMappingList extends FPVList {
  public ChannelMappingList(ChannelMappingScreen parentScreen) {
    super(parentScreen);
    
    this.addEntry(new CategoryEntry(this, () -> I18n.get(
      "fpvdrone.channel.category.axis")));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.throttle"),
      ControllerConfig::getThrottleChannel,
      ControllerConfig::setThrottleChannel,
      ControllerConfig::getInvertThrottle,
      ControllerConfig::setInvertThrottle,
      ControllerConfig::getDefaultThrottleChannel,
      ControllerConfig::resetThrottleChannel
    ));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.roll"),
      ControllerConfig::getRollChannel,
      ControllerConfig::setRollChannel,
      ControllerConfig::getInvertRoll,
      ControllerConfig::setInvertRoll,
      ControllerConfig::getDefaultRollChannel,
      ControllerConfig::resetRollChannel
    ));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.pitch"),
      ControllerConfig::getPitchChannel,
      ControllerConfig::setPitchChannel,
      ControllerConfig::getInvertPitch,
      ControllerConfig::setInvertPitch,
      ControllerConfig::getDefaultPitchChannel,
      ControllerConfig::resetPitchChannel
    ));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.yaw"),
      ControllerConfig::getYawChannel,
      ControllerConfig::setYawChannel,
      ControllerConfig::getInvertYaw,
      ControllerConfig::setInvertYaw,
      ControllerConfig::getDefaultYawChannel,
      ControllerConfig::resetYawChannel
    ));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.anglepot"),
      ControllerConfig::getAngleChannel,
      ControllerConfig::setAngleChannel,
      ControllerConfig::getInvertAngle,
      ControllerConfig::setInvertAngle,
      ControllerConfig::getDefaultAngleChannel,
      ControllerConfig::resetAngleChannel
    ));
    
    this.addEntry(new CategoryEntry(
      this,
      () -> I18n.get("fpvdrone.channel.category.switch")));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.arm"),
      getButtonChannel(ControllerConfig::getArmChannel),
      setButtonChannel(ControllerConfig::setArmChannel),
      ControllerConfig::getInvertArm,
      ControllerConfig::setInvertArm,
      getButtonChannel(ControllerConfig::getDefaultArmChannel),
      ControllerConfig::resetArmChannel
    ));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.angleswitch"),
      getButtonChannel(ControllerConfig::getActivateAngleChannel),
      setButtonChannel(ControllerConfig::setActivateAngleChannel),
      ControllerConfig::getInvertActivateAngle,
      ControllerConfig::setInvertActivateAngle,
      getButtonChannel(ControllerConfig::getDefaultActivateAngleChannel),
      ControllerConfig::resetActivateAngleChannel
    ));
    this.addEntry(new ChannelEntry(
      this,
      I18n.get("fpvdrone.channel.rightclick"),
      getButtonChannel(ControllerConfig::getRightClickChannel),
      setButtonChannel(ControllerConfig::setRightClickChannel),
      ControllerConfig::getInvertRightClick,
      ControllerConfig::setInvertRightClick,
      getButtonChannel(ControllerConfig::getDefaultRightClickChannel),
      ControllerConfig::resetRightClickChannel
    ));
    
    this.addEntry(new CategoryEntry(this, () -> ""));
  }
  
  private IntSupplier getButtonChannel(IntSupplier supplier) {
    return () -> supplier.getAsInt() + ControllerReader.getAxisLength();
  }
  
  private IntConsumer setButtonChannel(IntConsumer consumer) {
    return (int channel) -> consumer.accept(
      channel - ControllerReader.getAxisLength()
    );
  }
}
