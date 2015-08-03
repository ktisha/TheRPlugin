package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findCurrentLineEnd;
import static com.jetbrains.ther.debugger.TheRDebuggerStringUtils.findNextLineBegin;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.EMPTY;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessUtils.execute;

final class TheRTraceAndDebugUtils {

  @NotNull
  static final String LS_FUNCTIONS_COMMAND = "Filter(function(x) x == \"closure\", eapply(" + ENVIRONMENT + "(), " + TYPEOF_COMMAND + "))";

  @NotNull
  static final String NO_FUNCTIONS_RESPONSE = "named list()";

  public static void traceAndDebugFunctions(@NotNull final TheRProcess process, @NotNull final TheROutputReceiver receiver)
    throws TheRDebuggerException {
    final String output = execute(
      process,
      LS_FUNCTIONS_COMMAND,
      RESPONSE,
      receiver
    );

    int index = 0;

    while (output.startsWith("$", index)) {
      final int currentLineEnd = findCurrentLineEnd(output, index + 2);

      traceAndDebugFunction(
        process,
        receiver,
        output.substring(
          index + 1,
          currentLineEnd
        )
      );

      index = findNextLineBegin(output, findNextLineBegin(output, index));
    }
  }

  private static void traceAndDebugFunction(@NotNull final TheRProcess process,
                                            @NotNull final TheROutputReceiver receiver,
                                            @NotNull final String functionName) throws TheRDebuggerException {
    if (functionName.startsWith(SERVICE_FUNCTION_PREFIX) && functionName.endsWith(SERVICE_ENTER_FUNCTION_SUFFIX)) {
      return;
    }

    execute(process, enterFunction(functionName), EMPTY, receiver);
    execute(process, traceCommand(functionName), RESPONSE, receiver);
    execute(process, debugCommand(functionName), EMPTY, receiver);
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
           ", where = " + ENVIRONMENT + "()" +
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
