package com.jetbrains.ther.xdebugger;

import com.intellij.xdebugger.frame.XNamedValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;

class TheRXVar extends XNamedValue {

  @NotNull
  private final TheRVar myVar;

  public TheRXVar(@NotNull final TheRVar var) {
    super(var.getName());

    myVar = var;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    TheRXPresentationUtils.computePresentation(myVar, node);
  }
}
