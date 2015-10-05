package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.START_TRACE_BRACE;

class TheRBraceFunctionDebugger extends TheRFunctionDebuggerBase {

  public TheRBraceFunctionDebugger(@NotNull final TheRExecutor executor,
                                   @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                   @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                   @NotNull final TheROutputReceiver outputReceiver,
                                   @NotNull final String functionName) throws TheRDebuggerException {
    super(executor, debuggerFactory, debuggerHandler, outputReceiver, functionName);
  }

  @Override
  protected void handleDebugAt(@NotNull final TheRExecutionResult result) throws TheRDebuggerException {
    handleDebugAt(result, true, true);
  }

  @NotNull
  @Override
  protected TheRExecutionResultType getStartTraceType() {
    return START_TRACE_BRACE;
  }
}
