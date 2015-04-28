package com.jetbrains.ther.debugger.intellij;

import com.intellij.xdebugger.frame.XExecutionStack;
import com.intellij.xdebugger.frame.XSuspendContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheRSuspendContext extends XSuspendContext {

  @NotNull
  private final TheRExecutionStack myExecutionStack;

  public TheRSuspendContext(@NotNull final List<TheRStackFrameData> stackFramesData) {
    myExecutionStack = new TheRExecutionStack(stackFramesData);
  }

  @NotNull
  @Override
  public XExecutionStack getActiveExecutionStack() {
    return myExecutionStack;
  }
}
