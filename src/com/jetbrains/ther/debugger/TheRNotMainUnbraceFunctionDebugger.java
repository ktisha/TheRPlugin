package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.utils.TheRLoadableVarHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

// TODO [dbg][test]
class TheRNotMainUnbraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainUnbraceFunctionDebugger(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                            @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                            @NotNull final TheRFunctionResolver functionResolver,
                                            @NotNull final TheRLoadableVarHandler varHandler,
                                            @NotNull final TheRFunction function) throws IOException, InterruptedException {
    super(process, debuggerFactory, debuggerHandler, functionResolver, varHandler, function);
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
      default:
        throw new IllegalStateException("Unexpected response from interpreter");
    }
  }

  @Override
  protected int initCurrentLine() throws IOException, InterruptedException {
    return 0;
  }
}
