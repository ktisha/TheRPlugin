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

  public static void traceAndDebugFunctions(@NotNull final TheRProcess process, @NotNull final TheROutputReceiver receiver)
    throws TheRDebuggerException {
    final String output = execute(
      process,
      "Filter(function(x) x == \"closure\", eapply(" + ENVIRONMENT + "(), " + TYPEOF_COMMAND + "))",
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
    if (functionName.startsWith(SERVICE_FUNCTION_PREFIX) &&
        (functionName.endsWith(SERVICE_ENTER_FUNCTION_SUFFIX) || functionName.endsWith(SERVICE_EXIT_FUNCTION_SUFFIX))) {
      return;
    }

    execute(process, enterFunction(functionName), EMPTY, receiver);
    execute(process, exitFunction(functionName), EMPTY, receiver);
    execute(process, traceCommand(functionName), RESPONSE, receiver);
    execute(process, debugCommand(functionName), EMPTY, receiver);
  }

  @NotNull
  private static String enterFunction(@NotNull final String var) {
    return enterFunctionName(var) + " <- function() { print(\"" + var + "\") }";
  }

  @NotNull
  private static String exitFunction(@NotNull final String var) {
    return exitFunctionName(var) + " <- function() { print(\"" + var + "\") }";
  }

  @NotNull
  private static String traceCommand(@NotNull final String var) {
    return TRACE_COMMAND +
           "(" +
           var +
           ", " +
           enterFunctionName(var) +
           ", exit = " +
           exitFunctionName(var) +
           ", where = environment()" +
           ")";
  }

  @NotNull
  private static String debugCommand(@NotNull final String var) {
    return DEBUG_COMMAND + "(" + var + ")";
  }

  @NotNull
  private static String enterFunctionName(@NotNull final String var) {
    return SERVICE_FUNCTION_PREFIX + var + SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String exitFunctionName(@NotNull final String var) {
    return SERVICE_FUNCTION_PREFIX + var + SERVICE_EXIT_FUNCTION_SUFFIX;
  }
}
