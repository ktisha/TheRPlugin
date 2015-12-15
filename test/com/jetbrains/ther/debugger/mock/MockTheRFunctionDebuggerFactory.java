package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockTheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

  @Nullable
  private final MockTheRFunctionDebugger myDebugger;

  private int myCounter;

  public MockTheRFunctionDebuggerFactory(@Nullable final MockTheRFunctionDebugger debugger) {
    myDebugger = debugger;
    myCounter = 0;
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getFunctionDebugger(@NotNull final TheRExecutor executor,
                                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                  @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
    if (myDebugger == null) {
      throw new IllegalStateException("GetFunctionDebugger shouldn't be called");
    }

    myCounter++;

    myDebugger.setHandler(debuggerHandler);

    return myDebugger;
  }

  public int getCounter() {
    return myCounter;
  }
}
