package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.exception.UnexpectedResponseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.LS_COMMAND;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.TYPEOF_COMMAND;
import static com.jetbrains.ther.debugger.interpreter.TheRProcessResponseType.RESPONSE;

public final class TheRProcessUtils {

  @NotNull
  public static TheRProcessResponse execute(@NotNull final TheRProcess process,
                                            @NotNull final String command,
                                            @NotNull final TheRProcessResponseType expectedType) throws TheRDebuggerException {
    final TheRProcessResponse response = process.execute(command);

    if (response.getType() != expectedType) {
      throw new UnexpectedResponseException(
        "Actual response type is not the same as expected: [actual: " + response.getType() + ", expected: " + expectedType + "]"
      );
    }

    return response;
  }

  @NotNull
  public static List<TheRVar> loadUnmodifiableVars(@NotNull final TheRProcess process, @NotNull final TheRLoadableVarHandler handler)
    throws TheRDebuggerException {
    return Collections.unmodifiableList(
      loadVars(process, handler)
    );
  }

  @NotNull
  public static List<TheRVar> loadVars(@NotNull final TheRProcess process, @NotNull final TheRLoadableVarHandler handler) // TODO [dbg][update]
    throws TheRDebuggerException {
    final String text = execute(
      process,
      LS_COMMAND,
      RESPONSE
    ).getOutput();

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
  private static TheRVar loadVar(@NotNull final TheRProcess process,
                                 @NotNull final TheRLoadableVarHandler handler,
                                 @NotNull final String var) throws TheRDebuggerException {
    final String type = handler.handleType(
      process,
      var,
      execute(
        process,
        TYPEOF_COMMAND + "(" + var + ")",
        RESPONSE
      ).getOutput()
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
                                  @NotNull final TheRLoadableVarHandler handler,
                                  @NotNull final String var,
                                  @NotNull final String type) throws TheRDebuggerException {
    return handler.handleValue(
      var,
      type,
      execute(
        process,
        TheRProcessCommandUtils.valueCommand(var, type),
        RESPONSE
      ).getOutput()
    );
  }
}
