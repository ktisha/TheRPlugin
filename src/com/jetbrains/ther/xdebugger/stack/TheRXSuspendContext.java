package com.jetbrains.ther.xdebugger.stack;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

class TheRXSuspendContext extends XSuspendContext {

  @NotNull
  private final TheRXExecutionStack myExecutionStack;

  public TheRXSuspendContext(@NotNull final List<TheRXStackFrame> stack) {
    myExecutionStack = new TheRXExecutionStack(stack);
  }

  @NotNull
  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myExecutionStack;
  }

  private static class TheRXExecutionStack extends XExecutionStack {

    @NotNull
    private final List<TheRXStackFrame> myStack;

    private TheRXExecutionStack(@NotNull final List<TheRXStackFrame> stack) {
      super(""); // argument used as a description for current thread

      myStack = stack;
    }

    @Nullable
    @Override
    public XStackFrame getTopFrame() {
      return myStack.isEmpty() ? null : myStack.get(0);
    }

    @Override
    public void computeStackFrames(final int firstFrameIndex, final XStackFrameContainer container) {
      if (firstFrameIndex <= myStack.size()) {
        final List<TheRXStackFrame> stackFrames = myStack.subList(firstFrameIndex, myStack.size());

        container.addStackFrames(stackFrames, true);
      }
    }
  }
}
