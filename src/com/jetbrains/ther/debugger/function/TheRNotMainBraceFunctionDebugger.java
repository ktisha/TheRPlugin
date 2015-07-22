package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.*;

// TODO [dbg][test]
class TheRNotMainBraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainBraceFunctionDebugger(@NotNull final TheRProcess process,
                                          @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                          @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                          @NotNull final TheRLoadableVarHandler varHandler,
                                          @NotNull final String functionName) throws TheRDebuggerException {
    super(process, debuggerFactory, debuggerHandler, varHandler, functionName);
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
        throw new UnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + response.getType() + ", " +
          "expected: " +
          "[" + DEBUG_AT + ", " + CONTINUE_TRACE + ", " + END_TRACE + ", " + DEBUGGING_IN + ", " + RECURSIVE_END_TRACE + "]" +
          "]"
        );
    }
  }

  @Override
  protected int initCurrentLine() throws TheRDebuggerException {
    return loadLineNumber();
  }
}
