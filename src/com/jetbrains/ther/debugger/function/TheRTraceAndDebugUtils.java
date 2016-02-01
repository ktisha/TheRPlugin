package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.data.TheRResponseConstants;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.executor.TheRExecutor;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findCurrentLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findNextLineBegin;
import static com.jetbrains.ther.debugger.data.TheRCommands.*;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.EMPTY;
import static com.jetbrains.ther.debugger.executor.TheRExecutionResultType.RESPONSE;
import static com.jetbrains.ther.debugger.executor.TheRExecutorUtils.execute;

public final class TheRTraceAndDebugUtils {

  @NotNull
  private static final String LS_FUNCTIONS_COMMAND = FILTER_COMMAND + "(" +
                                                     "function(x) x == \"" + CLOSURE + "\", " +
                                                     EAPPLY_COMMAND +
                                                     "(" +
                                                     TheRResponseConstants.ENVIRONMENT +
                                                     "(), " +
                                                     TYPEOF_COMMAND +
                                                     ")" +
                                                     ")";

  public static void traceAndDebugFunctions(@NotNull final TheRExecutor executor, @NotNull final TheROutputReceiver receiver)
    throws TheRDebuggerException {
    final String output = execute(
      executor,
      LS_FUNCTIONS_COMMAND,
      RESPONSE,
      receiver
    );

    int index = 0;

    while (output.startsWith("$", index)) {
      final int currentLineEnd = findCurrentLineEnd(output, index + 2);

      traceAndDebugFunction(
        executor,
        receiver,
        output.substring(
          index + 1,
          currentLineEnd
        )
      );

      index = findNextLineBegin(output, findNextLineBegin(output, index));
    }
  }

  private static void traceAndDebugFunction(@NotNull final TheRExecutor executor,
                                            @NotNull final TheROutputReceiver receiver,
                                            @NotNull final String functionName) throws TheRDebuggerException {
    if (functionName.startsWith(SERVICE_FUNCTION_PREFIX) && !functionName.equals(MAIN_FUNCTION_NAME)) {
      return;
    }

    execute(executor, enterFunction(functionName), EMPTY, receiver);
    execute(executor, traceCommand(functionName), RESPONSE);
    execute(executor, debugCommand(functionName), EMPTY, receiver);
  }

  @NotNull
  private static String enterFunction(@NotNull final String functionName) {
    return enterFunctionName(functionName) + " <- function() { print(\"" + functionName + "\") }";
  }

  @NotNull
  private static String traceCommand(@NotNull final String functionName) {
    return TRACE_COMMAND +
           "(" +
           functionName +
           ", " +
           enterFunctionName(functionName) +
           ", where = " + TheRResponseConstants.ENVIRONMENT + "()" +
           ")";
  }

  @NotNull
  private static String debugCommand(@NotNull final String functionName) {
    return DEBUG_COMMAND + "(" + functionName + ")";
  }

  @NotNull
  private static String enterFunctionName(@NotNull final String functionName) {
    return SERVICE_FUNCTION_PREFIX + functionName + SERVICE_ENTER_FUNCTION_SUFFIX;
  }
}
