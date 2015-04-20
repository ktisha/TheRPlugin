package com.jetbrains.ther.debugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XCompositeNode;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheRStackFrame extends XStackFrame {

  @NotNull
  private final TheRStackFrameData myFrame;

  public TheRStackFrame(@NotNull final TheRStackFrameData frame) {
    myFrame = frame;
  }

  @NotNull
  @Override
  public XSourcePosition getSourcePosition() {
    return myFrame.getPosition();
  }

  @Override
  public void computeChildren(@NotNull final XCompositeNode node) {
    node.addChildren(calculateVars(), true);
  }

  @NotNull
  private XValueChildrenList calculateVars() {
    final List<String> names = new ArrayList<String>(myFrame.getVarRepresentations().keySet());
    Collections.sort(names);

    final XValueChildrenList result = new XValueChildrenList();

    for (final String name : names) {
      final String value = myFrame.getVarRepresentations().get(name);
      final String type = myFrame.getVarTypes().get(name);

      result.add(
        new TheRVar(name, type, value)
      );
    }

    return result;
  }
}
