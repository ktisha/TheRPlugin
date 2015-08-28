package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

public abstract class MockTheRExecutor implements TheRExecutor {

  private int myCounter = 0;

  @NotNull
  @Override
  public TheRExecutionResult execute(@NotNull final String command) throws TheRDebuggerException {
    myCounter++;

    return doExecute(command);
  }

  public int getCounter() {
    return myCounter;
  }

  @NotNull
  protected abstract TheRExecutionResult doExecute(@NotNull final String command) throws TheRDebuggerException;
}
