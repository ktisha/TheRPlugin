package com.jetbrains.ther.debugger.utils;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.interpreter.TheRProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.RESPONSE;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.START_TRACE_BRACE;

public final class TheRDebuggerUtils {

  @NotNull
  public static List<TheRVar> loadVars(@NotNull final TheRProcess process, @NotNull final TheRLoadableVarHandler handler)
    throws IOException, InterruptedException {
    final String text = process.execute(
      LS_COMMAND,
      RESPONSE
    );

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
  public static List<TheRVar> loadUnmodifiableVars(@NotNull final TheRProcess process, @NotNull final TheRLoadableVarHandler handler)
    throws IOException, InterruptedException {
    return Collections.unmodifiableList(
      loadVars(process, handler)
    );
  }

  @NotNull
  public static String loadFunctionName(@NotNull final TheRProcess process) throws IOException, InterruptedException {
    process.execute(EXECUTE_AND_STEP_COMMAND, RESPONSE);
    process.execute(EXECUTE_AND_STEP_COMMAND, RESPONSE);
    process.execute(EXECUTE_AND_STEP_COMMAND, RESPONSE);

    return extractFunctionName(
      process.execute(
        EXECUTE_AND_STEP_COMMAND,
        START_TRACE_BRACE
      )
    );
  }

  public static boolean isCommentOrSpaces(@Nullable final CharSequence line) {
    if (line == null) {
      return false;
    }

    for (int i = 0; i < line.length(); i++) {
      if (StringUtil.isWhiteSpace(line.charAt(i))) {
        continue;
      }

      return line.charAt(i) == COMMENT_SYMBOL;
    }

    return true;
  }

  public static int findNextLineBegin(@NotNull final CharSequence sequence, final int index) {
    int current = index;

    while (current < sequence.length() && !StringUtil.isLineBreak(sequence.charAt(current))) {
      current++;
    }

    while (current < sequence.length() && StringUtil.isLineBreak(sequence.charAt(current))) {
      current++;
    }

    return current;
  }

  public static int findCurrentLineEnd(@NotNull final CharSequence sequence, final int index) {
    int current = index;

    while (current < sequence.length() && !StringUtil.isLineBreak(sequence.charAt(current))) {
      current++;
    }

    return current;
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
                                 @NotNull final String var)
    throws IOException, InterruptedException {
    final String type = handler.handleType(
      process,
      var,
      process.execute(
        TYPEOF_COMMAND + "(" + var + ")",
        RESPONSE
      )
    );

    if (type == null) {
      return null;
    }

    return new TheRVar(var, type, loadValue(process, handler, var, type));
  }

  @NotNull
  private static String extractFunctionName(@NotNull final String entryText) {
    final int secondLineBegin = findNextLineBegin(entryText, 0);
    final int secondLineEnd = findCurrentLineEnd(entryText, secondLineBegin);

    return entryText.substring(
      secondLineBegin + "[1] \"".length() + "enter ".length(),
      secondLineEnd - "\"".length()
    );
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
                                  @NotNull final String type)
    throws IOException, InterruptedException {
    return handler.handleValue(
      var,
      type,
      process.execute(var, RESPONSE)
    );
  }
}
