package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import org.jetbrains.annotations.NotNull;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;

final class TheRProcessCommandUtils {

  @NotNull
  public static String enterFunction(@NotNull final String var) {
    return enterFunctionName(var) + " <- function() { print(\"" + var + "\") }";
  }

  @NotNull
  public static String exitFunction(@NotNull final String var) {
    return exitFunctionName(var) + " <- function() { print(\"" + var + "\") }";
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
           ", where = environment()" +
           ")";
  }

  @NotNull
  public static String debugCommand(@NotNull final String var) {
    return TheRDebugConstants.DEBUG_COMMAND + "(" + var + ")";
  }

  @NotNull
  public static String valueCommand(@NotNull final String var, @NotNull final String type) {
    if (type.equals(FUNCTION_TYPE)) {
      return ATTR_COMMAND + "(" + var + ", \"original\")";
    }
    else {
      return PRINT_COMMAND + "(" + var + ")";
    }
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
