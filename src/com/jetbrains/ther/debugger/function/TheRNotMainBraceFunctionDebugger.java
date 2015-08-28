package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;

class TheRNotMainBraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRNotMainBraceFunctionDebugger(@NotNull final TheRExecutor executor,
                                          @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                          @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                          @NotNull final TheROutputReceiver outputReceiver,
                                          @NotNull final String functionName) throws TheRDebuggerException {
    super(executor, debuggerFactory, debuggerHandler, outputReceiver, functionName);
  }

  @Override
  protected void handleExecutionResult(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    switch (result.getType()) {
      case DEBUG_AT:
        handleDebugAt(result);
        break;
      case CONTINUE_TRACE:
        handleContinueTrace(result);
        break;
      case EXITING_FROM:
        handleEndTrace(result);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn(result);
        break;
      case RECURSIVE_EXITING_FROM:
        handleRecursiveEndTrace(result);
        break;
      default:
        throw new TheRUnexpectedExecutionResultException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
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
  protected TheRExecutionResultType getStartTraceType() {
    return START_TRACE_BRACE;
  }
}
