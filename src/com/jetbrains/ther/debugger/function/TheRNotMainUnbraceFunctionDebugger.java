package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

// TODO [dbg][test]
class TheRNotMainUnbraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainUnbraceFunctionDebugger(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                            @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                            @NotNull final TheRLoadableVarHandler varHandler,
                                            @NotNull final String functionName) throws TheRDebuggerException {
    super(process, debuggerFactory, debuggerHandler, varHandler, functionName);
  }

  @Override
  protected void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
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
  protected int initCurrentLine() throws TheRDebuggerException {
    return 0;
  }
}
