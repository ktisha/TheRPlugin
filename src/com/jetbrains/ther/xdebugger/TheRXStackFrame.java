package com.jetbrains.ther.xdebugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;

public class TheRXStackFrame extends XStackFrame {

  @NotNull
  private final XSourcePosition myPosition;

  @NotNull
  private final TheRStackFrame myFrame;

  public TheRXStackFrame(@NotNull final XSourcePosition position, @NotNull final TheRStackFrame frame) {
    myPosition = position;
    myFrame = frame;
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
  private XValueChildrenList calculateVars() {
    final XValueChildrenList result = new XValueChildrenList();

    for (final TheRVar var : myFrame.getVars()) {
      result.add(new TheRXVar(var));
    }

    return result;
  }
}
