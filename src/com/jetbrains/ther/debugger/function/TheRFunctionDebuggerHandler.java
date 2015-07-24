package com.jetbrains.ther.debugger.function;

import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebuggerHandler {

  void appendDebugger(@NotNull final TheRFunctionDebugger debugger);

  void setReturnLineNumber(final int lineNumber);

  void setDropFrames(final int number);
}
