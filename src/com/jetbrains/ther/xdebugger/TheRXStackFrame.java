package com.jetbrains.ther.xdebugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// TODO [xdbg][test]
public class TheRXStackFrame extends XStackFrame {

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
}
