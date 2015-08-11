package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.TheRUnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findCurrentLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findNextLineBegin;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.START_TRACE_BRACE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.START_TRACE_UNBRACE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

public class TheRFunctionDebuggerFactoryImpl implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheROutputReceiver outputReceiver)
    throws TheRDebuggerException {
    execute(process, EXECUTE_AND_STEP_COMMAND, TheRProcessResponseType.DEBUG_AT, outputReceiver);

    final TheRProcessResponse startTraceResponse = execute(process, EXECUTE_AND_STEP_COMMAND, outputReceiver);

    switch (startTraceResponse.getType()) {
      case START_TRACE_BRACE:
        return new TheRNotMainBraceFunctionDebugger(
          process,
          this,
          debuggerHandler,
          outputReceiver,
          extractFunctionName(startTraceResponse.getOutput())
        );

      case START_TRACE_UNBRACE:
        return new TheRNotMainUnbraceFunctionDebugger(
          process,
          this,
          debuggerHandler,
          outputReceiver,
          extractFunctionName(startTraceResponse.getOutput())
        );
      default:
        throw new TheRUnexpectedResponseException(
          "Actual response type is not the same as expected: " +
          "[" +
          "actual: " + startTraceResponse.getType() + ", " +
          "expected: " +
          "[" + START_TRACE_BRACE + ", " + START_TRACE_UNBRACE + "]" +
          "]"
        );
    }
  }

  @NotNull
  @Override
  public TheRFunctionDebugger getMainFunctionDebugger(@NotNull final TheRProcess process,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheROutputReceiver outputReceiver,
                                                      @NotNull final TheRScriptReader scriptReader) {
    return new TheRMainFunctionDebugger(
      process, this, debuggerHandler, outputReceiver, scriptReader
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
