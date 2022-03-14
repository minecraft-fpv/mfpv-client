package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.GuiEvents;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class KeyManager {
  public static KeyMapping armKey = new KeyMapping(
    "key.fpvdrone.arm",
    GLFW.GLFW_KEY_I,
    "key.fpvdrone.category"
  );
  
  public static KeyMapping menuKey = new KeyMapping(
    "key.fpvdrone.menu",
    GLFW.GLFW_KEY_O,
    "key.fpvdrone.category"
  );
  
  public static void init() {
    ClientRegistry.registerKeyBinding(armKey);
    ClientRegistry.registerKeyBinding(menuKey);
  
    Options gameSettings = Minecraft.getInstance().options;
  
    KeyMapping[] nextKeyMappings = new KeyMapping[gameSettings.keyMappings.length];
    for (int i = 0; i < gameSettings.keyMappings.length; i++) {
      if (gameSettings.keyMappings[i].equals(gameSettings.keyUp)) {
        nextKeyMappings[i] = new KeyMappingInterceptor(gameSettings.keyUp);
        gameSettings.keyUp = nextKeyMappings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyLeft)) {
        nextKeyMappings[i] = new KeyMappingInterceptor(gameSettings.keyLeft);
        gameSettings.keyLeft = nextKeyMappings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyDown)) {
        nextKeyMappings[i] = new KeyMappingInterceptor(gameSettings.keyDown);
        gameSettings.keyDown = nextKeyMappings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyRight)) {
        nextKeyMappings[i] = new KeyMappingInterceptor(gameSettings.keyRight);
        gameSettings.keyRight = nextKeyMappings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyJump)) {
        nextKeyMappings[i] = new KeyMappingInterceptor(gameSettings.keyJump);
        gameSettings.keyJump = nextKeyMappings[i];
      } else {
        nextKeyMappings[i] = gameSettings.keyMappings[i];
      }
    }
    gameSettings.keyMappings = nextKeyMappings;
  }
  
  public static void poll() {
    Options gameSettings = Minecraft.getInstance().options;
    if (ControllerReader.arm &&
        ((KeyMappingInterceptor) gameSettings.keyLeft).isKeyReallyDown()) {
      ControllerReader.yaw += -0.5f;
    }
    if (ControllerReader.arm &&
        ((KeyMappingInterceptor) gameSettings.keyRight).isKeyReallyDown()) {
      ControllerReader.yaw += 0.5f;
    }
    if (ControllerReader.arm &&
        ((KeyMappingInterceptor) gameSettings.keyJump).isKeyReallyDown()) {
      ControllerReader.throttle = 1f;
    } else if (ControllerReader.arm &&
               ((KeyMappingInterceptor) gameSettings.keyUp).isKeyReallyDown()) {
      ControllerReader.throttle = 0.5f;
    }
    if (ControllerReader.arm &&
        ((KeyMappingInterceptor) gameSettings.keyDown).isKeyReallyDown()) {
      ControllerReader.throttle += -1f;
    }
  }
  
  @SubscribeEvent
  public static void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    boolean isGuiOpen = Minecraft.getInstance().screen != null;
    if (isGuiOpen) return;
    
    int action = event.getAction();
    int code = event.getKey();
    
    if (code == armKey.getKey().getValue()) {
      ControllerReader.rawArm = action == GLFW.GLFW_PRESS;
      ControllerReader.handleArmToggle();
    } else if (code == menuKey.getKey().getValue()) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.screen == null) {
        minecraft.pauseGame(false);
        GuiEvents.openFpvSettingsScreen(null);
      }
    }
  }
}
