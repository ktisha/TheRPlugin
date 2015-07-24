package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRProcess implements TheRProcess {

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    throw new IllegalStateException("Execute shouldn't be called");
  }

  @Override
  public void stop() {
  }
}
