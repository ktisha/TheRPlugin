package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;

public abstract class MockTheRProcess implements TheRProcess {

  private int myCounter = 0;

  @NotNull
  @Override
  public TheRProcessResponse execute(@NotNull final String command) throws TheRDebuggerException {
    myCounter++;

    return doExecute(command);
  }

  @Override
  public void stop() {
  }

  public int getCounter() {
    return myCounter;
  }

  @NotNull
  protected abstract TheRProcessResponse doExecute(@NotNull final String command) throws TheRDebuggerException;
}
