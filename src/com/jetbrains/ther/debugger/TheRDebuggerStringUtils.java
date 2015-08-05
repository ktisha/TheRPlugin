package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.interpreter.TheRProcessResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.COMMENT_SYMBOL;
import static com.jetbrains.ther.debugger.data.TheRDebugConstants.ENVIRONMENT;

public final class TheRDebuggerStringUtils {

  public static void appendError(@NotNull final TheRProcessResponse response, @NotNull final TheROutputReceiver receiver) {
    final String error = response.getError();

    if (!error.isEmpty()) {
      receiver.receiveError(error);
    }
  }

  public static void appendResult(@NotNull final TheRProcessResponse response, @NotNull final TheROutputReceiver receiver) {
    final TextRange range = response.getResultRange();

    if (!range.isEmpty()) {
      receiver.receiveOutput(
        range.substring(
          response.getOutput()
        )
      );
    }
  }

  public static void appendOutput(@NotNull final TheRProcessResponse response, @NotNull final TheROutputReceiver receiver) {
    final String output = response.getOutput();

    if (!output.isEmpty()) {
      receiver.receiveOutput(output);
    }
  }

  @NotNull
  public static String handleFunctionValue(@NotNull final String functionValue) {
    final int lastLineBegin = findLastLineBegin(functionValue);

    if (functionValue.startsWith(ENVIRONMENT, lastLineBegin + "<".length())) {
      return functionValue.substring(
        0,
        findLastButOneLineEnd(functionValue, lastLineBegin)
      );
    }
    else {
      return functionValue;
    }
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

  private static int findLastLineBegin(@NotNull final String text) {
    int current = text.length() - 1;

    while (current > -1 && !StringUtil.isLineBreak(text.charAt(current))) {
      current--;
    }

    return current + 1;
  }

  private static int findLastButOneLineEnd(@NotNull final String text, final int lastLineBegin) {
    int current = lastLineBegin - 1;

    while (current > -1 && StringUtil.isLineBreak(text.charAt(current))) {
      current--;
    }

    return current + 1;
  }
}
