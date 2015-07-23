package com.jetbrains.ther.xdebugger.stack;

import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import com.jetbrains.ther.xdebugger.resolve.TheRXResolver;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// TODO [xdbg][test]
public class TheRXStack {

  @NotNull
  private final List<TheRStackFrame> myOriginalStack;

  @NotNull
  private final TheRXResolver myResolver;

  @NotNull
  private List<TheRXStackFrame> myStack;

  @NotNull
  private XSuspendContext mySuspendContext;

  public TheRXStack(@NotNull final List<TheRStackFrame> stack, @NotNull final TheRXResolver resolver) {
    myOriginalStack = stack;
    myResolver = resolver;

    /*
    myStack = calculateStack();
    mySuspendContext = new TheRXSuspendContext(myStack);
    */
  }

  public void update() {
    myStack = calculateStack();
    mySuspendContext = new TheRXSuspendContext(myStack);
  }

  @NotNull
  public XSuspendContext getSuspendContext() {
    return mySuspendContext;
  }

  @NotNull
  private List<TheRXStackFrame> calculateStack() {
    final TheRXStackFrame[] result = new TheRXStackFrame[myOriginalStack.size()];
    final TheRXResolver.Session session = myResolver.getSession();

    int index = 0;
    for (final TheRStackFrame frame : myOriginalStack) {
      result[myOriginalStack.size() - 1 - index] =
        new TheRXStackFrame(
          frame,
          session.resolveNext(frame) // TODO [xdbg][null]
        );

      index++;
    }

    return Arrays.asList(result);
  }
}
