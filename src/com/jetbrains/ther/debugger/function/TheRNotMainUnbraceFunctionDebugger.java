package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;

class TheRNotMainUnbraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainUnbraceFunctionDebugger(@NotNull final TheRProcess process,
                                            @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                            @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                            @NotNull final TheROutputReceiver outputReceiver,
                                            @NotNull final String functionName) throws TheRDebuggerException {
    super(process, debuggerFactory, debuggerHandler, outputReceiver, functionName);
  }

  @Override
  protected void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    switch (response.getType()) {
      case END_TRACE:
        handleEndTrace(response);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn(response);
        break;
      case RECURSIVE_END_TRACE:
        handleRecursiveEndTrace(response);
        break;
      case CONTINUE_TRACE:
        handleContinueTrace(response);
        break;
      default:
        throw new UnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + END_TRACE + ", " + DEBUGGING_IN + ", " + RECURSIVE_END_TRACE + ", " + CONTINUE_TRACE + "]" +
          "]"
        );
    }
  }

  @Override
  protected int initCurrentLine() throws TheRDebuggerException {
    return 0;
  }

  @NotNull
  @Override
  protected TheRProcessResponseType getStartTraceType() {
    return TheRProcessResponseType.START_TRACE_UNBRACE;
  }

  @Override
  protected void handleDebuggingIn(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    super.handleDebuggingIn(response);

    setCurrentLineNumber(-1);
  }
}
