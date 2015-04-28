package com.jetbrains.ther.debugger.intellij;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import org.jetbrains.annotations.NotNull;

public class TheRVar extends XNamedValue {

  @NotNull
  private final String myType;

  @NotNull
  private final String myValue;

  public TheRVar(@NotNull final String name, @NotNull final String type, @NotNull final String value) {
    super(name);

    myType = type;
    myValue = value;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    node.setPresentation(AllIcons.Debugger.Value, myType, myValue, false);
  }
}
