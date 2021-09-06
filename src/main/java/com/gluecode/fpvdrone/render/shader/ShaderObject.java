package com.gluecode.fpvdrone.render.shader;

import com.gluecode.fpvdrone.Main;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.GL20;

import java.nio.charset.StandardCharsets;

public class ShaderObject {
  public int shader;
  public int type;

  public ShaderObject(String glslCode, int type) {
    this.type = type;
    this.shader = GlStateManager.glCreateShader(type);
    GL20.glShaderSource(this.shader, glslCode);
    GL20.glCompileShader(this.shader);
    int[] res = new int[1];
    GL20.glGetShaderiv(this.shader, GL20.GL_COMPILE_STATUS, res);
    if (res[0] == GL20.GL_FALSE) {
      Main.LOGGER.error("Failed to compile shader.");
      String infoLog = GL20.glGetShaderInfoLog(
        this.shader,
        GL20.glGetShaderi(this.shader, GL20.GL_INFO_LOG_LENGTH)
      );
      Main.LOGGER.error(infoLog);
    } else {
      Main.LOGGER.info("Shader compiled successfully.");
    }
  }

  public static ShaderObject createShader(int type, String filename) {
    ResourceLocation resourceLocation = new ResourceLocation(
      Main.MOD_ID,
      filename
    );
    IResourceManager resourceManager = Minecraft.getInstance()
      .getResourceManager();
    String contents = null;
    try {
      IResource resource = resourceManager.getResource(resourceLocation);
      contents = IOUtils.toString(
        resource.getInputStream(),
        StandardCharsets.UTF_8
      );
    } catch (Exception e) {
      Main.LOGGER.error(e);
    }
    if (contents == null) return null;

    return new ShaderObject(contents, type);
  }
}
