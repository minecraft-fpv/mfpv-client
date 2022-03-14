package com.gluecode.fpvdrone.input;

import com.gluecode.fpvdrone.Main;
import net.minecraft.client.KeyMapping;

public class KeyMappingInterceptor extends KeyMapping {
  protected KeyMapping interceptedKeyMapping;
  private boolean interceptionActive;
  private int interceptedPressTime;
  
  /**
   * Create an Interceptor based on an existing binding.
   * The initial interception mode is OFF.
   * If existingKeyMapping is already a KeyMappingInterceptor, a reinitialised copy will be created but no further effect.
   *
   * @param existingKeyMapping - the binding that will be intercepted.
   */
  public KeyMappingInterceptor(KeyMapping existingKeyMapping) {
    super(
      existingKeyMapping.getName(),
      existingKeyMapping.getKey().getValue(),
      existingKeyMapping.getCategory()
    );
    
    this.interceptionActive = false;
    this.isDown = false;
    this.clickCount = 0;
    this.interceptedPressTime = 0;
    
    if (existingKeyMapping instanceof KeyMappingInterceptor) {
      interceptedKeyMapping = ((KeyMappingInterceptor) existingKeyMapping)
        .getOriginalKeyMapping();
    } else {
      interceptedKeyMapping = existingKeyMapping;
    }
    
    KeyMapping.resetMapping();
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
    //    Main.LOGGER.info("a: " + interceptedKeyMapping.isDown);
    //    copyKeyCodeToOriginal();
    //    return interceptedKeyMapping.isDown;
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
   * Otherwise, copies these from the intercepted KeyMapping.
   *
   * @return If interception is on, this will return false; Otherwise, it will pass on any clicks in the intercepted KeyMapping
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
  
  public KeyMapping getOriginalKeyMapping() {
    return interceptedKeyMapping;
  }
  
  protected void copyClickInfoFromOriginal() {
    this.clickCount += interceptedKeyMapping.clickCount;
    this.interceptedPressTime += interceptedKeyMapping.clickCount;
    interceptedKeyMapping.clickCount = 0;
    this.isDown = interceptedKeyMapping.isDown;
  }
  
  protected void copyKeyCodeToOriginal() {
    // only copy if necessary
    if (this.getKey() != interceptedKeyMapping.getKey()) {
      this.setKey(interceptedKeyMapping.getKey());
      resetMapping();
    }
  }
  
}
