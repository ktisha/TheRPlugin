package com.jetbrains.ther.debugger.function;

import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebuggerHandler {

  void appendOutput(@NotNull final String text);

  void appendError(@NotNull final String text);

  void appendDebugger(@NotNull final TheRFunctionDebugger debugger);

  void setReturnLineNumber(final int lineNumber);

  void setDropFrames(final int number);
}
