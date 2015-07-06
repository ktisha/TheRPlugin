package com.jetbrains.ther.debugger.utils;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

final class TheRLoadableVarHandlerUtils {

  public static void traceAndDebug(@NotNull final TheRProcess process, @NotNull final String var)
    throws IOException, InterruptedException {
    process.execute(enterFunction(var), TheRProcessResponseType.EMPTY);
    process.execute(exitFunction(var), TheRProcessResponseType.EMPTY);
    process.execute(traceCommand(var), TheRProcessResponseType.RESPONSE);
    process.execute(debugCommand(var), TheRProcessResponseType.EMPTY);
  }

  @NotNull
  public static String enterFunction(@NotNull final String var) {
    return enterFunctionName(var) + " <- function() { print(\"enter " + var + "\") }";
  }

  @NotNull
  public static String exitFunction(@NotNull final String var) {
    return exitFunctionName(var) + " <- function() { print(\"exit " + var + "\") }";
  }

  @NotNull
  public static String traceCommand(@NotNull final String var) {
    return TheRDebugConstants.TRACE_COMMAND +
           "(" +
           var +
           ", " +
           enterFunctionName(var) +
           ", exit = " +
           exitFunctionName(var) +
           ")";
  }

  @NotNull
  public static String debugCommand(@NotNull final String var) {
    return TheRDebugConstants.DEBUG_COMMAND + "(" + var + ")";
  }

  @NotNull
  private static String enterFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String exitFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_EXIT_FUNCTION_SUFFIX;
  }
}
