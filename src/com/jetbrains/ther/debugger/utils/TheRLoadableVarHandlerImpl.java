package com.jetbrains.ther.debugger.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

// TODO [dbg][test]
public class TheRLoadableVarHandlerImpl implements TheRLoadableVarHandler {

  @Override
  @Nullable
  public String handleType(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws IOException, InterruptedException {
    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      if (var.startsWith(TheRDebugConstants.SERVICE_FUNCTION_PREFIX)) {
        return null;
      }
      else {
        traceAndDebug(process, var);
      }
    }

    return type;
  }

  @Override
  @NotNull
  public String handleValue(@NotNull final TheRProcess process,
                            @NotNull final String var,
                            @NotNull final String type,
                            @NotNull final String value) {
    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      final String[] lines = StringUtil.splitByLinesKeepSeparators(value);
      final StringBuilder sb = new StringBuilder();

      for (int i = 2; i < lines.length - 1; i++) {
        sb.append(lines[i]);
      }

      while (StringUtil.endsWithLineBreak(sb)) {
        sb.setLength(sb.length() - 1);
      }

      return sb.toString();
    }
    else {
      return value;
    }
  }

  private void traceAndDebug(@NotNull final TheRProcess process, @NotNull final String var)
    throws IOException, InterruptedException {
    process.execute(createEnterFunction(var), TheRProcessResponseType.JUST_BROWSE);
    process.execute(createExitFunction(var), TheRProcessResponseType.JUST_BROWSE);
    process.execute(createTraceCommand(var), TheRProcessResponseType.RESPONSE_AND_BROWSE);
    process.execute(createDebugCommand(var), TheRProcessResponseType.JUST_BROWSE);
  }

  @NotNull
  private String createEnterFunction(@NotNull final String var) {
    return createEnterFunctionName(var) + " <- function() { print(\"enter " + var + "\") }";
  }

  @NotNull
  private String createEnterFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private String createExitFunction(@NotNull final String var) {
    return createExitFunctionName(var) + " <- function() { print(\"exit " + var + "\") }";
  }

  @NotNull
  private String createExitFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_EXIT_FUNCTION_SUFFIX;
  }

  @NotNull
  private String createTraceCommand(@NotNull final String var) {
    return TheRDebugConstants.TRACE_COMMAND +
           "(" +
           var +
           ", " +
           createEnterFunctionName(var) +
           ", exit = " +
           createExitFunctionName(var) +
           ")";
  }

  @NotNull
  private String createDebugCommand(@NotNull final String var) {
    return TheRDebugConstants.DEBUG_COMMAND + "(" + var + ")";
  }
}
