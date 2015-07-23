package com.jetbrains.ther.xdebugger.stack;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.*;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO [xdbg][test-compute-children]
class TheRXStackFrame extends XStackFrame {

  @NotNull
  private final TheRStackFrame myFrame;

  @NotNull
  private final XSourcePosition myPosition;

  @Nullable
  private TheRXDebuggerEvaluator myEvaluator;

  public TheRXStackFrame(@NotNull final TheRStackFrame frame, @NotNull final XSourcePosition position) {
    myPosition = position;
    myFrame = frame;
    myEvaluator = null;
  }

  @NotNull
  @Override
  public XSourcePosition getSourcePosition() {
    return myPosition;
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    node.addChildren(calculateVars(), true);
  }

  @NotNull
  @Override
  public XDebuggerEvaluator getEvaluator() {
    if (myEvaluator == null) {
      myEvaluator = new TheRXDebuggerEvaluator(myFrame.getEvaluator());
    }

    return myEvaluator;
  }

  @NotNull
  private XValueChildrenList calculateVars() {
    final XValueChildrenList result = new XValueChildrenList();

    for (final TheRVar var : myFrame.getVars()) {
      result.add(new TheRXVar(var));
    }

    return result;
  }

  private static class TheRXVar extends XNamedValue {

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
}
