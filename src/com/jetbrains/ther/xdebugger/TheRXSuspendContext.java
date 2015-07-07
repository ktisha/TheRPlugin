package com.jetbrains.ther.xdebugger;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import org.jetbrains.annotations.NotNull;

import java.util.List;

class TheRXSuspendContext extends XSuspendContext {

  @NotNull
  private final TheRXExecutionStack myExecutionStack;

  public TheRXSuspendContext(@NotNull final List<TheRStackFrame> stack, @NotNull final TheRLocationResolver locationResolver) {
    myExecutionStack = new TheRXExecutionStack(stack, locationResolver);
  }

  @NotNull
  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myExecutionStack;
  }
}
