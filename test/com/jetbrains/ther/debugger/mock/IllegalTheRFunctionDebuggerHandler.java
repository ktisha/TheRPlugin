package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.TheRFunctionDebuggerHandler;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRFunctionDebuggerHandler implements TheRFunctionDebuggerHandler {

  @Override
  public void appendOutput(@NotNull final String text) {
    throw new IllegalStateException("AppendOutput shouldn't be called");
  }

  @Override
  public void appendDebugger(@NotNull final TheRFunctionDebugger debugger) {
    throw new IllegalStateException("AppendDebugger shouldn't be called");
  }

  @Override
  public void setReturnLineNumber(final int lineNumber) {
    throw new IllegalStateException("SetReturnLineNumber shouldn't be called");
  }
}