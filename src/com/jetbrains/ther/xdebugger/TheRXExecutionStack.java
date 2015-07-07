package com.jetbrains.ther.xdebugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XStackFrame;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

// TODO [xdbg][test]
class TheRXExecutionStack extends XExecutionStack {

  @NotNull
  private final List<TheRXStackFrame> myStack;

  public TheRXExecutionStack(@NotNull final List<TheRStackFrame> stack, @NotNull final TheRLocationResolver locationResolver) {
    super(""); // argument used as a description for current thread

    myStack = new ArrayList<TheRXStackFrame>(stack.size());

    final ListIterator<TheRStackFrame> stackIterator = stack.listIterator(stack.size());

    while (stackIterator.hasPrevious()) {
      final TheRStackFrame frame = stackIterator.previous();

      myStack.add(
        new TheRXStackFrame(
          frame,
          locationResolver.resolve(frame.getLocation())
        )
      );
    }
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
