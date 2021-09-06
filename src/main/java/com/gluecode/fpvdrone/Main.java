package com.gluecode.fpvdrone;

import com.gluecode.fpvdrone.entity.DroneBuild;
import com.gluecode.fpvdrone.entity.DroneRenderer;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.gluecode.fpvdrone.input.KeyManager;
import com.gluecode.fpvdrone.network.packet.PacketHandler;
import com.gluecode.fpvdrone.network.VersionNotifier;
import com.gluecode.fpvdrone.util.SettingsLoader;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.network.play.NetworkPlayerInfo;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntitySize;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("fpvdrone")
public class Main {
  public static final String MOD_ID = "fpvdrone";
  public static final Logger LOGGER = LogManager.getLogger(); //log4j logger.
  
  //  public static final Method setPose = ObfuscationReflectionHelper.findMethod(Entity.class, "func_213301_b", Pose.class);
  public static Map<UUID, Vector3d> entityPosition = Maps.newConcurrentMap();
  public static Map<UUID, Vector3d> entityVelocity = Maps.newConcurrentMap();
  public static Map<UUID, Boolean> entityArmStates = Maps.newConcurrentMap();
  public static Map<UUID, DroneBuild> droneBuilds = Maps.newConcurrentMap();
  public static Map<UUID, String> cachedPlayerNames = Maps.newConcurrentMap();
  public static Map<UUID, DroneRenderer> droneRenderers = Maps.newConcurrentMap();
  
  public Main() {
    DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
      KeyManager.init();
      ControllerReader.init();
      SettingsLoader.load();
      VersionNotifier.init();
    });
    PacketHandler.register();
    
    // Register ourselves for server and other game events we are interested in
    MinecraftForge.EVENT_BUS.register(this);
    MinecraftForge.EVENT_BUS.register(Main.class);
  }
  
  public static boolean isClientSide() {
    ClientWorld world = Minecraft.getInstance().level;
    if (world != null) {
      return world.isClientSide;
    } else {
      return false;
    }
  }
  
  @SubscribeEvent
  public static void setup(FMLCommonSetupEvent event) {
    DeferredWorkQueue.runLater(() -> {
      //      GlobalEntityTypeAttributes.put(ModEntityTypes.DRONE.get(), DroneEntity.setCustomAttributes().create());
    });
  }
  
  @SubscribeEvent
  public static void handlePlayerSize(EntityEvent.Size event) {
    Entity entity = event.getEntity();
    if (!entity.isAddedToWorld()) {
      return;
    }
    
    if (entityArmStates.getOrDefault(entity.getUUID(), false)) {
      float height = 0.1f;
      event.setNewSize(EntitySize.scalable(0.2F, height));
      event.setNewEyeHeight(height / 2f);
    }
  }
  
  @SubscribeEvent
  public static void handlePlayerTick(TickEvent.PlayerTickEvent event) {
    // This runs on both server and client.
    // Note: There is an elytra flying flag, but setting it will mess up drone physics.
    
    // Size is determined by the current pose.
    if (event.phase == TickEvent.Phase.END) {
      PlayerEntity player = event.player;
      if (entityArmStates.getOrDefault(player.getUUID(), false)) {
        //        player.setOnGround(false); // setOnGround(false)
        player.abilities.flying = true;
        //        player.setNoGravity(true);
        //        player.isAirBorne = true;
        player.onUpdateAbilities();
        
        // Trigger Size event by chaing pose:
        player.setPose(Pose.CROUCHING);
        player.setPose(Pose.STANDING);
        
        //        try {
        //          Main.setPose.invoke(event.player, Pose.SLEEPING);
        //        } catch (IllegalAccessException | InvocationTargetException e) {
        //          String displayName = player.getDisplayName().toString();
        //          Main.LOGGER.error("Error setting player prone: " + displayName, e);
        //        }
      } else {
        if (!(player.isCreative() || player.isSpectator())) {
          player.abilities.flying = false;
          player.onUpdateAbilities();
        }
      }
      
      //      if (!player.level.isClientSide) {
      //        boolean isSurvival = !(player.isCreative() || player.isSpectator());
      //        boolean isArmed = entityArmStates.getOrDefault(player.getUUID(), false);
      //        if (isArmed && isSurvival) {
      //          // check for drone mode collisions
      //          boolean collided = player.collidedHorizontally || player.collidedVertically;
      //          if (collided) {
      //            // Extrapolate the lastVelocity to find the collision results.
      //            Vector3d lastVelocity = entityVelocity.get(player.getUUID());
      //            if (lastVelocity != null) {
      //              // note this is not a perfect solution. A proper solution should use the previous position, not the current position, to get the clipped displacement.
      //              // todo: make getAllowedMovement work on previous position.
      //              // Maybe we can move the player back, run getAllowedMovement, then move the player forward?
      //              Vector3d clipped = player.collide(lastVelocity);
      //
      //              Vector3d diff = lastVelocity.subtract(clipped);
      //              Main.LOGGER.info("diff.length(): " + diff.length());
      //
      //              Vector3f lastVelocityF = new Vector3f((float) lastVelocity.x, (float) lastVelocity.y, (float) lastVelocity.z);
      //              CollisionResults results = Physics.getCollisionResults(lastVelocityF, clipped, lastVelocityF);
      //
      //              // Note that collisionsResults velocity is in meters per tick.
      //              float preSpeed = lastVelocityF.length() * 20f; // * 20 to convert to meters per second.
      //              float postSpeed = results.velocity.length() * 20f;
      //              float mass = 1; // todo: variable mass.
      //              float preEnergy = 0.5f * mass * preSpeed * preSpeed;
      //              float postEnergy = 0.5f * mass * postSpeed * postSpeed;
      //              float diffEnergy = preEnergy - postEnergy;
      //
      //              float damage = diffEnergy / 30f;
      //              if (damage >= 1) {
      //                player.attackEntityFrom(DamageSource.FLY_INTO_WALL, damage);
      //              }
      //            }
      //          }
      //        }
      //
      //        Vector3d lastPosition = entityPosition.get(player.getUUID());
      //        if (lastPosition != null) {
      //          Vector3d velocity = player.position().subtract(lastPosition);
      //          entityVelocity.put(player.getUUID(), velocity);
      //        }
      //        entityPosition.put(player.getUUID(), player.position());
      //      }
    }
  }
  
  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void onRender(RenderPlayerEvent.Pre event) {
    // Render the drone model, excluding the proDroneBuild.
    
    AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) event.getPlayer();
    if (Main.entityArmStates.getOrDefault(player.getUUID(), false)) {
      event.setCanceled(true);
      
      DroneRenderer droneRenderer = droneRenderers.get(player.getUUID());
      if (droneRenderer == null) {
        UUID uuid = player.getUUID();
        DroneBuild build = Main.droneBuilds.getOrDefault(
          uuid,
          DroneBuild.getSelf()
        );
        droneRenderer = new DroneRenderer(
          uuid,
          event.getRenderer().getDispatcher(),
          build
        );
        droneRenderers.put(player.getUUID(), droneRenderer);
      }
      
      float partialTicks = event.getPartialRenderTick();
//      droneRenderer.renderProps = false;
      droneRenderer.render(
        player,
        player.getViewYRot(partialTicks),
        partialTicks,
        event.getMatrixStack(),
        event.getBuffers(),
        event.getLight()
      );
    }
  }
  
  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void onRenderDrone(RenderWorldLastEvent event) {
    // Render the drone props only for all players.
    ClientPlayerEntity self = Minecraft.getInstance().player;
    if (self == null) return;
    UUID selfId = self.getUUID();
    for (UUID uuid : droneRenderers.keySet()) {
      if (uuid.equals(selfId)) {
        if (Minecraft.getInstance().options.getCameraType() ==
            PointOfView.FIRST_PERSON) {
          // Do not render self props because they will get in the way.
          continue;
        }
      }
      
      ClientWorld world = Minecraft.getInstance().level;
      if (world == null) continue;
      AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) world
        .getPlayerByUUID(uuid);
      if (player == null) continue;
      DroneRenderer droneRenderer = droneRenderers.get(uuid);
      if (droneRenderer == null) continue;
      
      float partialTicks = event.getPartialTicks();
      
//      droneRenderer.renderProps = true;
      droneRenderer.renderBlurryProps(
        player,
        player.getViewYRot(partialTicks),
        partialTicks,
        event.getMatrixStack()
      );
    }
  }
  
  @OnlyIn(Dist.CLIENT)
  @SubscribeEvent
  public static void onRenderOverlay(RenderBlockOverlayEvent event) {
    if (ControllerReader.getArm() &&
        event.getOverlayType() ==
        RenderBlockOverlayEvent.OverlayType.BLOCK) {
      event.setCanceled(true);
    }
  }
  
  public static @Nullable
  String getPlayerNameFromUuid(UUID userId) {
    String name = cachedPlayerNames.get(userId);
    
    if (name == null) {
      Minecraft minecraft = Minecraft.getInstance();
      ClientPlayNetHandler clientPlayNetHandler = minecraft.getConnection();
      if (clientPlayNetHandler != null) {
        NetworkPlayerInfo playerInfo = clientPlayNetHandler.getPlayerInfo(
          userId);
        if (playerInfo != null) {
          name = playerInfo.getProfile().getName();
        }
      }
    }
    
    if (name == null) {
      // An HTTP call will be made async.
      // While the call is loading, null will be returned.
      // This means that getPlayerNameFromUuid needs to be called again.
      CompletableFuture.runAsync(() -> {
        try {
          URL url = new URL("https://api.mojang.com/user/profiles/" +
                            userId.toString().replaceAll("-", "") +
                            "/names");
          InputStream stream = url.openStream();
          try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(stream, writer, StandardCharsets.UTF_8);
            String response = writer.toString();
            JSONArray nameList = (JSONArray) JSONValue.parse(
              response);
            JSONObject nameEntry = (JSONObject) nameList.get(
              nameList.size() - 1);
            String newName = (String) nameEntry.get("name");
            if (newName != null) {
              cachedPlayerNames.put(userId, newName);
            }
          } finally {
            stream.close();
          }
        } catch (Exception e) {
          Main.LOGGER.error(e);
        }
      });
    }
    
    if (name == null) {
      return null;
    } else {
      return name;
    }
  }
  
  public static int getColorIntFromHex(String hex) {
    return Integer.parseInt(hex.substring(1), 16);
  }
  
  public static @Nullable
  String getDimension(@Nullable World world) {
    if (world == null) return null;
    RegistryKey<World> dimension = world.dimension();
    return dimension.location().toString();
  }
}
