package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.Main;
import net.minecraft.client.settings.KeyBinding;

public class KeyBindingInterceptor extends KeyBinding {
  protected KeyBinding interceptedKeyBinding;
  private boolean interceptionActive;
  private int interceptedPressTime;
  
  /**
   * Create an Interceptor based on an existing binding.
   * The initial interception mode is OFF.
   * If existingKeyBinding is already a KeyBindingInterceptor, a reinitialised copy will be created but no further effect.
   *
   * @param existingKeyBinding - the binding that will be intercepted.
   */
  public KeyBindingInterceptor(KeyBinding existingKeyBinding) {
    super(
      existingKeyBinding.getName(),
      existingKeyBinding.getKey().getValue(),
      existingKeyBinding.getCategory()
    );
    
    this.interceptionActive = false;
    this.isDown = false;
    this.clickCount = 0;
    this.interceptedPressTime = 0;
    
    if (existingKeyBinding instanceof KeyBindingInterceptor) {
      interceptedKeyBinding = ((KeyBindingInterceptor) existingKeyBinding)
        .getOriginalKeyBinding();
    } else {
      interceptedKeyBinding = existingKeyBinding;
    }
    
    KeyBinding.resetMapping();
  }
  
  public void setInterceptionActive(boolean newMode) {
    if (newMode && !interceptionActive) {
      this.interceptedPressTime = 0;
    }
    interceptionActive = newMode;
  }
  
  public boolean isDown() {
    if (interceptionActive) return false;
    return super.isDown();
    //    Main.LOGGER.info("a: " + interceptedKeyBinding.isDown);
    //    copyKeyCodeToOriginal();
    //    return interceptedKeyBinding.isDown;
  }
  
  public boolean isKeyReallyDown() {
    return super.isDown();
  }
  
  /**
   * @return returns false if interception isn't active.  Otherwise, retrieves one of the clicks (true) or false if no clicks left
   */
  public boolean retrieveClick() {
    copyKeyCodeToOriginal();
    if (interceptionActive) {
      copyClickInfoFromOriginal();
      
      if (this.interceptedPressTime == 0) {
        return false;
      } else {
        --this.interceptedPressTime;
        return true;
      }
    } else {
      return false;
    }
  }
  
  /**
   * A better name for this method would be retrieveClick.
   * If interception is on, resets .isDown and .clickCount to zero.
   * Otherwise, copies these from the intercepted KeyBinding.
   *
   * @return If interception is on, this will return false; Otherwise, it will pass on any clicks in the intercepted KeyBinding
   */
  @Override
  public boolean consumeClick() {
    copyKeyCodeToOriginal();
    copyClickInfoFromOriginal();
    
    Main.LOGGER.info("interceptionActive: " + interceptionActive);
    
    if (interceptionActive) {
      this.clickCount = 0;
      this.isDown = false;
      return false;
    } else {
      if (this.clickCount == 0) {
        return false;
      } else {
        --this.clickCount;
        return true;
      }
    }
  }
  
  public KeyBinding getOriginalKeyBinding() {
    return interceptedKeyBinding;
  }
  
  protected void copyClickInfoFromOriginal() {
    this.clickCount += interceptedKeyBinding.clickCount;
    this.interceptedPressTime += interceptedKeyBinding.clickCount;
    interceptedKeyBinding.clickCount = 0;
    this.isDown = interceptedKeyBinding.isDown;
  }
  
  protected void copyKeyCodeToOriginal() {
    // only copy if necessary
    if (this.key != interceptedKeyBinding.key) {
      this.key = interceptedKeyBinding.key;
      resetMapping();
    }
  }
  
}
