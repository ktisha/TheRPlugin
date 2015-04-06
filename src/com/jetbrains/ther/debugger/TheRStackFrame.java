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

  public TheRStackFrame(@NotNull TheRStackFrameData frame) {
    myFrame = frame;
  }

  @NotNull
  @Override
  public XSourcePosition getSourcePosition() {
    return myFrame.getPosition();
  }

  @Override
  public void computeChildren(@NotNull XCompositeNode node) {
    node.addChildren(calculateVars(), true);
  }

  @NotNull
  private XValueChildrenList calculateVars() {
    List<String> names = new ArrayList<>(myFrame.getVarRepresentations().keySet());
    Collections.sort(names);

    XValueChildrenList result = new XValueChildrenList();

    for (String name : names) {
      String value = myFrame.getVarRepresentations().get(name);
      String type = myFrame.getVarTypes().get(name);

      result.add(
        new TheRVar(name, type, value)
      );
    }

    return result;
  }
}
