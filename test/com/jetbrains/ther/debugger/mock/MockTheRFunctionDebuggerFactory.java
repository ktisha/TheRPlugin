package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerFactory;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockTheRFunctionDebuggerFactory implements TheRFunctionDebuggerFactory {

  @Nullable
  private final MockTheRFunctionDebugger myNotMainDebugger;

  @Nullable
  private final MockTheRFunctionDebugger myMainDebugger;

  private int myNotMainCounter;
  private int myMainCounter;

  public MockTheRFunctionDebuggerFactory(@Nullable final MockTheRFunctionDebugger notMainDebugger,
                                         @Nullable final MockTheRFunctionDebugger mainDebugger) {
    myNotMainDebugger = notMainDebugger;
    myMainDebugger = mainDebugger;

    myNotMainCounter = 0;
    myMainCounter = 0;
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheROutputReceiver outputReceiver) throws TheRDebuggerException {
    if (myNotMainDebugger == null) {
      throw new IllegalStateException("GetNotMainFunctionDebugger shouldn't be called");
    }

    myNotMainCounter++;

    myNotMainDebugger.setHandler(debuggerHandler);

    return myNotMainDebugger;
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheROutputReceiver outputReceiver,
                                                      @NotNull final TheRScriptReader scriptReader) {
    if (myMainDebugger == null) {
      throw new IllegalStateException("GetMainFunctionDebugger shouldn't be called");
    }

    myMainCounter++;

    myMainDebugger.setHandler(debuggerHandler);

    return myMainDebugger;
  }

  public int getNotMainCounter() {
    return myNotMainCounter;
  }

  public int getMainCounter() {
    return myMainCounter;
  }
}
