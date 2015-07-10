package com.jetbrains.ther.xdebugger;

import com.intellij.icons.AllIcons;
import com.intellij.xdebugger.frame.*;
import com.intellij.xdebugger.frame.presentation.XStringValuePresentation;
import com.intellij.xdebugger.frame.presentation.XValuePresentation;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO [xdbg][test]
class TheRXVar extends XNamedValue {

  @NotNull
  private final TheRVar myVar;

  public TheRXVar(@NotNull final TheRVar var) {
    super(var.getName());

    myVar = var;
  }

  @Override
  public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
    if (isFunction()) {
      node.setPresentation(
        AllIcons.Debugger.Value,
        new FunctionXValuePresentation(),
        true
      );
    }
    else {
      node.setPresentation(
        AllIcons.Debugger.Value,
        myVar.getType(),
        myVar.getValue(),
        false
      );
    }
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    if (isFunction()) {
      node.addChildren(
        XValueChildrenList.singleton(new FunctionXNamedValue(myVar)),
        true
      );
    }
  }

  private boolean isFunction() {
    return myVar.getType().equals(TheRDebugConstants.FUNCTION_TYPE);
  }

  private static class FunctionXValuePresentation extends XValuePresentation {

    @Nullable
    @Override
    public String getType() {
      return TheRDebugConstants.FUNCTION_TYPE;
    }

    @Override
    public void renderValue(@NotNull final XValueTextRenderer renderer) {
    }
  }

  private static class FunctionXNamedValue extends XNamedValue {

    @NotNull
    private final TheRVar myVar;

    public FunctionXNamedValue(@NotNull final TheRVar var) {
      super(var.getName());

      myVar = var;
    }

    @Override
    public void computePresentation(@NotNull final XValueNode node, @NotNull final XValuePlace place) {
      node.setPresentation(
        AllIcons.Debugger.Value,
        new XStringValuePresentation(myVar.getValue()),
        false
      );
    }
  }
}
