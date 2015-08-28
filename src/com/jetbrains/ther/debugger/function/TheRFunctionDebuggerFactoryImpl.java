package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedExecutionResultException;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import com.jetbrains.ther.debugger.executor.TheRExecutionResultType;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findCurrentLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findNextLineBegin;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.START_TRACE_BRACE;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.START_TRACE_UNBRACE;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;

public class TheRFunctionDebuggerFactoryImpl implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRExecutor executor,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheROutputReceiver outputReceiver)
    throws TheRDebuggerException {
    execute(executor, EXECUTE_AND_STEP_COMMAND, TheRExecutionResultType.DEBUG_AT, outputReceiver);

    final TheRExecutionResult startTraceResult = execute(executor, EXECUTE_AND_STEP_COMMAND, outputReceiver);

    switch (startTraceResult.getType()) {
      case START_TRACE_BRACE:
        return new TheRNotMainBraceFunctionDebugger(
          executor,
          this,
          debuggerHandler,
          outputReceiver,
          extractFunctionName(startTraceResult.getOutput())
        );

      case START_TRACE_UNBRACE:
        return new TheRNotMainUnbraceFunctionDebugger(
          executor,
          this,
          debuggerHandler,
          outputReceiver,
          extractFunctionName(startTraceResult.getOutput())
        );
      default:
        throw new TheRUnexpectedExecutionResultException(
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
  @Override
  public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRExecutor executor,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheROutputReceiver outputReceiver,
                                                      @NotNull final TheRScriptReader scriptReader) {
    return new TheRMainFunctionDebugger(
      executor, this, debuggerHandler, outputReceiver, scriptReader
    );
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
