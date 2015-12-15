package com.jetbrains.ther.debugger.executor;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

public interface TheRExecutor {

  @NotNull
  TheRExecutionResult execute(@NotNull final String command) throws TheRDebuggerException;
}
