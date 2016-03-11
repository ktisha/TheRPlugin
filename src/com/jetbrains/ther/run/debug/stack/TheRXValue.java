package com.jetbrains.ther.run.debug.stack;

import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

public class TheRXValue extends XValue {

  @NotNull
  private final String myValue;

  public TheRXValue(@NotNull final String value) {
    myValue = value;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    TheRXPresentationUtils.computePresentation(myValue, node);
  }

  @NotNull
  public String getValue() {
    return myValue;
  }
}
