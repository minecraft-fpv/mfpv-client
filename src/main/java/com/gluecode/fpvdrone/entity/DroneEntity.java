package com.gluecode.fpvdrone.entity;

import com.gluecode.fpvdrone.Main;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.item.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DroneEntity extends AbstractClientPlayer {
  public DroneEntity(ClientWorld world, GameProfile profile) {
    super(world, profile);
  }
  //  public DroneEntity(EntityType<? extends CreatureEntity> type, World worldIn) {
  //    super(type, worldIn);
  //  }
  
  //  public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
  //    // func_233666_p_() = registerAttributes
  //    return MobEntity.func_233666_p_()
  //        .createMutableAttribute(Attributes.MAX_HEALTH, 20)
  //        .createMutableAttribute(Attributes.MOVEMENT_SPEED, 4.317);
  //  }
}
