package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

public interface TheRProcess {

  @NotNull
  TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException;

  void stop();
}
