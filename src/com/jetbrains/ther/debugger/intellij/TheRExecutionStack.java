package com.jetbrains.ther.debugger.intellij;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TheRExecutionStack extends XExecutionStack {

  @NotNull
  private final List<TheRStackFrame> myStackFrames;

  public TheRExecutionStack(@NotNull final List<TheRStackFrameData> data) {
    super(""); // argument used as a description for current thread

    myStackFrames = new ArrayList<TheRStackFrame>(data.size());

    for (final TheRStackFrameData stackFrameData : data) {
      myStackFrames.add(
        new TheRStackFrame(stackFrameData)
      );
    }
  }

  @Nullable
  @Override
  public XStackFrame getTopFrame() {
    return myStackFrames.isEmpty() ? null : myStackFrames.get(myStackFrames.size() - 1);
  }

  @Override
  public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
    if (firstFrameIndex <= myStackFrames.size()) {
      final List<TheRStackFrame> stackFrames = myStackFrames.subList(firstFrameIndex, myStackFrames.size());

      container.addStackFrames(stackFrames, true);
    }
  }
}
