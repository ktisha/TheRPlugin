package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

interface TheRFunctionDebuggerHandler {

  void appendOutput(@NotNull final String text);

  void appendDebugger(@NotNull final TheRFunctionDebugger debugger);

  void setReturnLineNumber(final int lineNumber);
}
