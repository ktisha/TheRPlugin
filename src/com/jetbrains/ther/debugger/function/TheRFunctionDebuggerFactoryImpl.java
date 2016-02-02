package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultTypeException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findCurrentLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findNextLineBegin;
import static com.jetbrains.ther.debugger.data.TheRCommands.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.START_TRACE_BRACE;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.START_TRACE_UNBRACE;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;

public class TheRFunctionDebuggerFactoryImpl implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getFunctionDebugger(@NotNull final TheRExecutor executor,
                                                  @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                  @NotNull final TheROutputReceiver outputReceiver)
    throws TheRDebuggerException {
    execute(executor, EXECUTE_AND_STEP_COMMAND, TheRExecutionResultType.DEBUG_AT, outputReceiver);

    final TheRExecutionResult startTraceResult = execute(executor, EXECUTE_AND_STEP_COMMAND, outputReceiver);

    switch (startTraceResult.getType()) {
      case START_TRACE_BRACE:
        return new TheRBraceFunctionDebugger(
          executor,
          this,
          debuggerHandler,
          outputReceiver,
          extractFunctionName(startTraceResult.getOutput())
        );

      case START_TRACE_UNBRACE:
        return new TheRUnbraceFunctionDebugger(
          executor,
          this,
          debuggerHandler,
          outputReceiver,
          extractFunctionName(startTraceResult.getOutput())
        );
      default:
        throw new TheRUnexpectedExecutionResultTypeException(
          "Actual type is not the same as expected: " +
          "[" +
          "actual: " + startTraceResult.getType() + ", " +
          "expected: " +
          "[" + START_TRACE_BRACE + ", " + START_TRACE_UNBRACE + "]" +
          "]"
        );
    }
  }

  @NotNull
  private static String extractFunctionName(@NotNull final String startTraceText) {
    final int secondLineBegin = findNextLineBegin(startTraceText, 0);
    final int secondLineEnd = findCurrentLineEnd(startTraceText, secondLineBegin);

    return startTraceText.substring(
      secondLineBegin + "[1] \"".length(),
      secondLineEnd - "\"".length()
    );
  }
}
