package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.*;

class TheRNotMainBraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainBraceFunctionDebugger(@NotNull final TheRProcess process,
                                          @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                          @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                          @NotNull final TheROutputReceiver outputReceiver,
                                          @NotNull final String functionName) throws TheRDebuggerException {
    super(process, debuggerFactory, debuggerHandler, outputReceiver, functionName);
  }

  @Override
  protected void handleResponse(@NotNull final TheRProcessResponse response) throws TheRDebuggerException {
    switch (response.getType()) {
      case DEBUG_AT:
        handleDebugAt(response);
        break;
      case CONTINUE_TRACE:
        handleContinueTrace(response);
        break;
      case EXITING_FROM:
        handleEndTrace(response);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn(response);
        break;
      case RECURSIVE_EXITING_FROM:
        handleRecursiveEndTrace(response);
        break;
      default:
        throw new UnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + DEBUG_AT + ", " + CONTINUE_TRACE + ", " + EXITING_FROM + ", " + DEBUGGING_IN + ", " + RECURSIVE_EXITING_FROM + "]" +
          "]"
        );
    }
  }

  @Override
  protected int initCurrentLine() throws TheRDebuggerException {
    return loadLineNumber();
  }

  @NotNull
  @Override
  protected TheRProcessResponseType getStartTraceType() {
    return START_TRACE_BRACE;
  }
}
