package com.jetbrains.ther.debugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

public class TheRVar extends XNamedValue {

  @NotNull
  private final String myValue;

  @NotNull
  private final String myType;

  public TheRVar(@NotNull final String name, @NotNull final String type, @NotNull final String value) {
    super(name);

    myValue = value;
    myType = type;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    node.setPresentation(AllIcons.Debugger.Value, myType, myValue, false);
  }
}
