package com.gluecode.fpvdrone.gui.widget;

import com.jme3.math.FastMath;
import net.minecraft.client.gui.widget.AbstractSlider;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.StringTextComponent;

import java.text.DecimalFormat;
import java.util.function.Consumer;
import java.util.function.Supplier;


// todo: use abstract slider
public class SnappySlider extends AbstractSlider {
  private Consumer<Widget> onClick;
  private Consumer<Float> setNewValue;
  private Supplier<Float> getMin;
  private Supplier<Float> getMax;

  /*
   * slider value is from 0 to 1
   * */
  public SnappySlider(
    int xIn,
    int yIn,
    int width,
    int height,
    float lerpValue,
    Supplier<Float> getMin,
    Supplier<Float> getMax,
    Consumer<Float> setNewValue,
    Consumer<Widget> onClick
  ) {
    super(xIn, yIn, width, height, new StringTextComponent(""), lerpValue);
    this.getMin = getMin;
    this.getMax = getMax;
    this.setNewValue = setNewValue;
    this.onClick = onClick;

    float min = this.getMin.get();
    float max = this.getMax.get();
    float value = lerpValue * max + (1f - lerpValue) * min;
    this.setText(value);
  }

  public void setValue(float value) {
    float min = this.getMin.get();
    float max = this.getMax.get();
    value = FastMath.clamp(value, min, max);
    float diff = value - min;
    float range = max - min;
    float ratio = diff / range;

    double original = this.value;
    this.value = ratio;
    if (original != ratio) {
      this.applyValue();
    }
    this.updateMessage();

    this.setText(value);
  }

  public void setText(float value) {
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    String str = df.format(value);
    //        this.setMessage(new StringTextComponent(Integer.toString((int) FastMath.floor(value))));
    this.setMessage(new StringTextComponent(str));
  }


  /*
   * Looks like this function is called when a new value is set.
   * */
  @Override
  protected void applyValue() {
    float lerpValue = (float) this.value;
    float min = this.getMin.get();
    float max = this.getMax.get();
    float value = lerpValue * max + (1f - lerpValue) * min;
    this.setNewValue.accept(value);
  }


  /*
   * I think this is when the button is clicked.
   * */
  @Override
  public void onRelease(double mouseX, double mouseY) {
    super.onRelease(mouseX, mouseY);
    this.onClick.accept(this);
  }

  /*
   * Not sure what this does. OptionSlider uses it to set a value which looks like the label. And also has something to do with the narrator.
   * */
  @Override
  protected void updateMessage() {

  }
}
