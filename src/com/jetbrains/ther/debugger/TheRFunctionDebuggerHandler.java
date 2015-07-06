package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebuggerHandler {

  void appendOutput(@NotNull final String text);

  void appendDebugger(@NotNull final TheRFunctionDebugger debugger);

  void setReturnLineNumber(int lineNumber);

  @NotNull
  TheRFunction resolveFunction(@NotNull final TheRFunction currentFunction, @NotNull final String nextFunctionName);
}
