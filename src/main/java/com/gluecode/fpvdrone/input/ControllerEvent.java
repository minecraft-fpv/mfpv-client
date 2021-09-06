package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.Main;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Main.MOD_ID)
public class ControllerEvent {
  @SubscribeEvent
  public static void handleGuiEvent(GuiScreenEvent.InitGuiEvent.Pre event) {
    if (ControllerReader.controllerId != -1) {
      Main.LOGGER.debug("REFRESH BUFFERS");
      ControllerReader.axisBuffer = GLFW.glfwGetJoystickAxes(ControllerReader.controllerId);
      ControllerReader.buttonBuffer = GLFW.glfwGetJoystickButtons(
        ControllerReader.controllerId);
    }
  }
  
  @SubscribeEvent
  public static void handleTickEvent(TickEvent.RenderTickEvent event) {
    if (event.phase == TickEvent.Phase.END) {
      ControllerReader.poll();
      KeyManager.poll();
      MouseManager.poll();
    }
  }
}
