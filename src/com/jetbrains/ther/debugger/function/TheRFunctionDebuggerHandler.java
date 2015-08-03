package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebuggerHandler {

  void appendDebugger(@NotNull final TheRFunctionDebugger debugger) throws TheRDebuggerException;

  void setReturnLineNumber(final int lineNumber);

  void setDropFrames(final int number);
}
