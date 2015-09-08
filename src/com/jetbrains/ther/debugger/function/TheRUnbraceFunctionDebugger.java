package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.*;

class TheRUnbraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRUnbraceFunctionDebugger(@NotNull final TheRExecutor executor,
                                     @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                     @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                     @NotNull final TheROutputReceiver outputReceiver,
                                     @NotNull final String functionName) throws TheRDebuggerException {
    super(executor, debuggerFactory, debuggerHandler, outputReceiver, functionName);
  }

  @Override
  protected void handleExecutionResult(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    switch (result.getType()) {
      case EXITING_FROM:
        handleEndTrace(result);
        break;
      case DEBUGGING_IN:
        handleDebuggingIn(result);
        break;
      case RECURSIVE_EXITING_FROM:
        handleRecursiveEndTrace(result);
        break;
      case CONTINUE_TRACE:
        handleContinueTrace(result);
        break;
      case EMPTY:
        handleEmpty(result);
        break;
      default:
        throw new TheRUnexpectedExecutionResultException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + result.getType() + ", " +
          "expected: " +
          "[" + EXITING_FROM + ", " + DEBUGGING_IN + ", " + RECURSIVE_EXITING_FROM + ", " + CONTINUE_TRACE + ", " + EMPTY + "]" +
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
  protected TheRExecutionResultType getStartTraceType() {
    return TheRExecutionResultType.START_TRACE_UNBRACE;
  }

  @Override
  protected void handleDebuggingIn(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    super.handleDebuggingIn(result);

    setCurrentLineNumber(-1);
  }
}
