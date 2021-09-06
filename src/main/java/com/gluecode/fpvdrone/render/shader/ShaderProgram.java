package com.gluecode.fpvdrone.render.shader;

import com.gluecode.fpvdrone.Main;
import org.lwjgl.opengl.GL20;

public class ShaderProgram {
  public int program;
  public ShaderObject vertexShader;
  public ShaderObject fragmentShader;
  
  public ShaderProgram(
    ShaderObject vertexShader,
    ShaderObject fragmentShader
  ) {
    this.vertexShader = vertexShader;
    this.fragmentShader = fragmentShader;
    this.program = GL20.glCreateProgram();
    GL20.glAttachShader(this.program, vertexShader.shader);
    GL20.glAttachShader(this.program, fragmentShader.shader);
    GL20.glLinkProgram(this.program);
    int[] res = new int[1];
    GL20.glGetProgramiv(this.program, GL20.GL_LINK_STATUS, res);
    if (res[0] == GL20.GL_FALSE) {
      Main.LOGGER.error("Failed to link shader program.");
    }
    Main.LOGGER.info("Shader program linked.");
  }
}
