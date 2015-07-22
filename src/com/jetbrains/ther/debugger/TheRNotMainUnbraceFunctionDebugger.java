package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

// TODO [dbg][test]
class TheRNotMainUnbraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainUnbraceFunctionDebugger(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                            @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                            @NotNull final TheRLoadableVarHandler varHandler,
                                            @NotNull final String functionName) throws IOException, InterruptedException {
    super(process, debuggerFactory, debuggerHandler, varHandler, functionName);
  }

  @Override
  protected void handleResponse(@NotNull final TheRProcessResponse response) throws IOException, InterruptedException {
    switch (response.getType()) {
      case END_TRACE:
        handleEndTrace(response);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn();
        break;
      case RECURSIVE_END_TRACE:
        handleRecursiveEndTrace(response);
        break;
      default:
        throw new IllegalStateException("Unexpected response from interpreter");
    }
  }

  @Override
  protected int initCurrentLine() throws IOException, InterruptedException {
    return 0;
  }
}
