package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

// TODO [dbg][test]
public final class TheRDebuggerUtils {

  @NotNull
  public static List<TheRVar> loadVars(@NotNull final TheRProcess process) throws IOException, InterruptedException {
    final String text = executeAndCheckType(
      process,
      TheRDebugConstants.LS_COMMAND,
      TheRProcessResponseType.RESPONSE_AND_BROWSE
    ).getText();

    final List<TheRVar> vars = new ArrayList<TheRVar>();

    for (final String variableName : calculateVariableNames(text)) {
      final TheRVar var = loadVar(process, variableName);

      if (var != null) {
        vars.add(var);
      }
    }

    return vars;
  }

  @NotNull
  public static TheRProcessResponse executeAndCheckType(@NotNull final TheRProcess process,
                                                        @NotNull final String command,
                                                        @NotNull final TheRProcessResponseType expectedType)
    throws IOException, InterruptedException {
    final TheRProcessResponse response = process.execute(command);

    if (response.getType() != expectedType) {
      throw new IOException(); // TODO [dbg][update]
    }

    return response;
  }


  @NotNull
  private static List<String> calculateVariableNames(@NotNull final String response) {
    final List<String> result = new ArrayList<String>();

    for (final String line : StringUtil.splitByLines(response)) {
      for (final String token : StringUtil.tokenize(new StringTokenizer(line))) {
        final String var = getVariableName(token);

        if (var != null) {
          result.add(var);
        }
      }
    }

    return result;
  }

  @Nullable
  private static TheRVar loadVar(@NotNull final TheRProcess process, @NotNull final String var) throws IOException, InterruptedException {
    final String type = executeAndCheckType(
      process,
      TheRDebugConstants.TYPEOF_COMMAND + "(" + var + ")",
      TheRProcessResponseType.RESPONSE_AND_BROWSE
    ).getText();

    // TODO [dbg][update]
    if (type.equals(TheRDebugConstants.FUNCTION_TYPE)) {
      if (var.startsWith(TheRDebugConstants.SERVICE_FUNCTION_PREFIX)) {
        return null;
      }
      else {
        traceAndDebug(process, var);
      }
    }

    return new TheRVar(var, type, loadValue(process, var, type));
  }

  @Nullable
  private static String getVariableName(@NotNull final String token) {
    final boolean isNotEmptyQuotedString = StringUtil.isQuotedString(token) && token.length() > 2;

    if (isNotEmptyQuotedString) {
      return token.substring(1, token.length() - 1);
    }
    else {
      return null;
    }
  }

  private static void traceAndDebug(@NotNull final TheRProcess process, @NotNull final String var)
    throws IOException, InterruptedException {
    executeAndCheckType(process, createEnterFunction(var), TheRProcessResponseType.JUST_BROWSE);
    executeAndCheckType(process, createExitFunction(var), TheRProcessResponseType.JUST_BROWSE);
    executeAndCheckType(process, createTraceCommand(var), TheRProcessResponseType.RESPONSE_AND_BROWSE);
    executeAndCheckType(process, createDebugCommand(var), TheRProcessResponseType.JUST_BROWSE);
  }

  @NotNull
  private static String loadValue(@NotNull final TheRProcess process, @NotNull final String var, @NotNull final String type)
    throws IOException, InterruptedException {
    final String value = executeAndCheckType(process, var, TheRProcessResponseType.RESPONSE_AND_BROWSE).getText();

    // TODO [dbg][update]
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

  @NotNull
  private static String createEnterFunction(@NotNull final String var) {
    return createEnterFunctionName(var) + " <- function() { print(\"enter " + var + "\") }";
  }

  @NotNull
  private static String createEnterFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_ENTER_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String createExitFunction(@NotNull final String var) {
    return createExitFunctionName(var) + " <- function() { print(\"exit " + var + "\") }";
  }

  @NotNull
  private static String createExitFunctionName(@NotNull final String var) {
    return TheRDebugConstants.SERVICE_FUNCTION_PREFIX + var + TheRDebugConstants.SERVICE_EXIT_FUNCTION_SUFFIX;
  }

  @NotNull
  private static String createTraceCommand(@NotNull final String var) {
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
  private static String createDebugCommand(@NotNull final String var) {
    return TheRDebugConstants.DEBUG_COMMAND + "(" + var + ")";
  }
}
