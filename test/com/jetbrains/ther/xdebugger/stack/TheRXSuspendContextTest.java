package com.jetbrains.ther.xdebugger.stack;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.frame.TheRStackFrame;
import com.jetbrains.ther.debugger.mock.IllegalTheRDebuggerEvaluator;
import com.jetbrains.ther.debugger.mock.IllegalTheRVarsLoader;
import com.jetbrains.ther.xdebugger.mock.MockXStackFrameContainer;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TheRXSuspendContextTest {

  @Test
  public void ordinary() {
    final TheRXStackFrame first = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation("abc", 2),
        new IllegalTheRVarsLoader(),
        new IllegalTheRDebuggerEvaluator()
      ),
      null
    );

    final TheRXStackFrame second = new TheRXStackFrame(
      new TheRStackFrame(
        new TheRLocation("def", 1),
        new IllegalTheRVarsLoader(),
        new IllegalTheRDebuggerEvaluator()
      ),
      null
    );

    final TheRXSuspendContext context = new TheRXSuspendContext(Arrays.asList(first, second));
    final XExecutionStack stack = context.getActiveExecutionStack();
    final MockXStackFrameContainer container = new MockXStackFrameContainer();

    stack.computeStackFrames(1, container);
    assertEquals(Collections.singletonList(second), container.getResult());
    assertEquals(first, stack.getTopFrame());
  }
}