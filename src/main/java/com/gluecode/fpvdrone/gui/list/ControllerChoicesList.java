package com.gluecode.fpvdrone.gui.list;

import com.gluecode.fpvdrone.gui.entry.SingleButtonEntry;
import com.gluecode.fpvdrone.gui.screen.ControllerChoicesScreen;
import com.gluecode.fpvdrone.input.ControllerReader;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.AbstractOptionList;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ControllerChoicesList extends FPVList {
  public ControllerChoicesList(
    Screen parentScreen,
    Consumer<Integer> setId
  ) {
    super(parentScreen);
    
    for (int id = 0; id < 16; id++) {
      String name = ControllerReader.getControllerName(id);
      final int innerId = id;
      this.addEntry(
        new SingleButtonEntry(this, () -> {
          return ControllerReader.getControllerName(innerId);
        }, () -> {
          return !GLFW.glfwJoystickPresent(innerId);
        }, () -> {
          setId.accept(innerId);
        })
      );
    }
  }
}
