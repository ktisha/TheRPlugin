package com.jetbrains.ther.debugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheRExecutionStack extends XExecutionStack {

  @NotNull
  private final List<TheRStackFrame> myStackFrames;

  public TheRExecutionStack(@NotNull List<TheRStackFrameData> data) {
    super(""); // TODO

    myStackFrames = new ArrayList<>(data.size());

    for (TheRStackFrameData stackFrameData : data) {
      myStackFrames.add(
        new TheRStackFrame(stackFrameData)
      );
    }

    Collections.reverse(myStackFrames);
  }

  @Nullable
  @Override
  public XStackFrame getTopFrame() {
    return myStackFrames.isEmpty() ? null : myStackFrames.get(myStackFrames.size() - 1);
  }

  @Override
  public void computeStackFrames(int firstFrameIndex, XStackFrameContainer container) {
    if (firstFrameIndex <= myStackFrames.size()) {
      List<TheRStackFrame> stackFrames = myStackFrames.subList(firstFrameIndex, myStackFrames.size());

      container.addStackFrames(stackFrames, true);
    }
  }
}
