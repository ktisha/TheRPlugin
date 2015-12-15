package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

class TheRUnbraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRUnbraceFunctionDebugger(@NotNull final TheRExecutor executor,
                                     @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                     @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                     @NotNull final TheROutputReceiver outputReceiver,
                                     @NotNull final String functionName) throws TheRDebuggerException {
    super(executor, debuggerFactory, debuggerHandler, outputReceiver, functionName);
  }

  @Override
  protected void handleDebugAt(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    handleDebugAt(result, true, false);
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
