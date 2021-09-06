package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.Main;
import com.gluecode.fpvdrone.gui.GuiEvents;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class KeyManager {
  public static KeyBinding armKey = new KeyBinding(
    "key.fpvdrone.arm",
    GLFW.GLFW_KEY_I,
    "key.fpvdrone.category"
  );
  
  public static KeyBinding menuKey = new KeyBinding(
    "key.fpvdrone.menu",
    GLFW.GLFW_KEY_O,
    "key.fpvdrone.category"
  );
  
  public static void init() {
    ClientRegistry.registerKeyBinding(armKey);
    ClientRegistry.registerKeyBinding(menuKey);
  
    GameSettings gameSettings = Minecraft.getInstance().options;
  
    KeyBinding[] nextKeyBindings = new KeyBinding[gameSettings.keyMappings.length];
    for (int i = 0; i < gameSettings.keyMappings.length; i++) {
      if (gameSettings.keyMappings[i].equals(gameSettings.keyUp)) {
        nextKeyBindings[i] = new KeyBindingInterceptor(gameSettings.keyUp);
        gameSettings.keyUp = nextKeyBindings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyLeft)) {
        nextKeyBindings[i] = new KeyBindingInterceptor(gameSettings.keyLeft);
        gameSettings.keyLeft = nextKeyBindings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyDown)) {
        nextKeyBindings[i] = new KeyBindingInterceptor(gameSettings.keyDown);
        gameSettings.keyDown = nextKeyBindings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyRight)) {
        nextKeyBindings[i] = new KeyBindingInterceptor(gameSettings.keyRight);
        gameSettings.keyRight = nextKeyBindings[i];
      } else if (gameSettings.keyMappings[i].equals(gameSettings.keyJump)) {
        nextKeyBindings[i] = new KeyBindingInterceptor(gameSettings.keyJump);
        gameSettings.keyJump = nextKeyBindings[i];
      } else {
        nextKeyBindings[i] = gameSettings.keyMappings[i];
      }
    }
    gameSettings.keyMappings = nextKeyBindings;
  }
  
  public static void poll() {
    GameSettings gameSettings = Minecraft.getInstance().options;
    if (ControllerReader.arm &&
        ((KeyBindingInterceptor) gameSettings.keyLeft).isKeyReallyDown()) {
      ControllerReader.yaw += -0.5f;
    }
    if (ControllerReader.arm &&
        ((KeyBindingInterceptor) gameSettings.keyRight).isKeyReallyDown()) {
      ControllerReader.yaw += 0.5f;
    }
    if (ControllerReader.arm &&
        ((KeyBindingInterceptor) gameSettings.keyJump).isKeyReallyDown()) {
      ControllerReader.throttle = 1f;
    } else if (ControllerReader.arm &&
               ((KeyBindingInterceptor) gameSettings.keyUp).isKeyReallyDown()) {
      ControllerReader.throttle = 0.5f;
    }
    if (ControllerReader.arm &&
        ((KeyBindingInterceptor) gameSettings.keyDown).isKeyReallyDown()) {
      ControllerReader.throttle += -1f;
    }
  }
  
  @SubscribeEvent
  public static void onKeyboardEvent(InputEvent.KeyInputEvent event) {
    boolean isGuiOpen = Minecraft.getInstance().screen != null;
    if (isGuiOpen) return;
    
    int action = event.getAction();
    int code = event.getKey();
    
    if (code == armKey.key.getValue()) {
      ControllerReader.rawArm = action == GLFW.GLFW_PRESS;
      ControllerReader.handleArmToggle();
    } else if (code == menuKey.key.getValue()) {
      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.screen == null) {
        minecraft.pauseGame(false);
        GuiEvents.openFpvSettingsScreen(null);
      }
    }
  }
}
