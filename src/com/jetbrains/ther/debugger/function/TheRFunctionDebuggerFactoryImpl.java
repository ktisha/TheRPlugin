package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheRScriptReader;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import com.jetbrains.ther.debugger.interpreter.TheRLoadableVarHandler;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findCurrentLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findNextLineBegin;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.EXECUTE_AND_STEP_COMMAND;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

// TODO [dbg][test]
public class TheRFunctionDebuggerFactoryImpl implements TheRFunctionDebuggerFactory {

  @NotNull
  @Override
  public TheRFunctionDebugger getNotMainFunctionDebugger(@NotNull final TheRProcess process,
                                                         @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                         @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                         @NotNull final TheRLoadableVarHandler varHandler)
    throws TheRDebuggerException {
    execute(process, EXECUTE_AND_STEP_COMMAND, RESPONSE);
    execute(process, EXECUTE_AND_STEP_COMMAND, RESPONSE);
    execute(process, EXECUTE_AND_STEP_COMMAND, RESPONSE);

    final TheRProcessResponse startTraceResponse = process.execute(EXECUTE_AND_STEP_COMMAND);

    switch (startTraceResponse.getType()) {
      case START_TRACE_BRACE:
        return new TheRNotMainBraceFunctionDebugger(
          process,
          debuggerFactory,
          debuggerHandler,
          varHandler,
          extractFunctionName(startTraceResponse.getOutput())
        );

      case START_TRACE_UNBRACE:
        return new TheRNotMainUnbraceFunctionDebugger(
          process,
          debuggerFactory,
          debuggerHandler,
          varHandler,
          extractFunctionName(startTraceResponse.getOutput())
        );
      default:
        throw new UnexpectedResponseException(
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
                                                      @NotNull final TheRFunctionDebuggerFactory debuggerFactory,
                                                      @NotNull final TheRFunctionDebuggerHandler debuggerHandler,
                                                      @NotNull final TheRLoadableVarHandler varHandler,
                                                      @NotNull final TheRScriptReader scriptReader) {
    return new TheRMainFunctionDebugger(
      process, debuggerFactory, debuggerHandler, varHandler, scriptReader
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
