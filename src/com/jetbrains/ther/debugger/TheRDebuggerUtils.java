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
  public static List<TheRVar> loadVars(@NotNull final TheRProcess process, @NotNull final VarHandler handler)
    throws IOException, InterruptedException {
    final String text = executeAndCheckType(
      process,
      TheRDebugConstants.LS_COMMAND,
      TheRProcessResponseType.RESPONSE_AND_BROWSE
    ).getText();

    final List<TheRVar> vars = new ArrayList<TheRVar>();

    for (final String variableName : calculateVariableNames(text)) {
      final TheRVar var = loadVar(process, handler, variableName);

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
  private static TheRVar loadVar(@NotNull final TheRProcess process, @NotNull final VarHandler handler, @NotNull final String var)
    throws IOException, InterruptedException {
    final String type = handler.handleType(
      process,
      var,
      executeAndCheckType(
        process,
        TheRDebugConstants.TYPEOF_COMMAND + "(" + var + ")",
        TheRProcessResponseType.RESPONSE_AND_BROWSE
      ).getText()
    );

    if (type == null) {
      return null;
    }

    return new TheRVar(var, type, loadValue(process, handler, var, type));
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

  @NotNull
  private static String loadValue(@NotNull final TheRProcess process,
                                  @NotNull final VarHandler handler,
                                  @NotNull final String var,
                                  @NotNull final String type)
    throws IOException, InterruptedException {
    return handler.handleValue(
      process,
      var,
      type,
      executeAndCheckType(process, var, TheRProcessResponseType.RESPONSE_AND_BROWSE).getText()
    );
  }
}
