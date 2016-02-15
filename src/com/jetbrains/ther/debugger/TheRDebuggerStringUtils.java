package com.jetbrains.ther.debugger;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.executor.TheRExecutionResult;
import org.jetbrains.annotations.NotNull;

public final class TheRDebuggerStringUtils {

  public static void appendError(@NotNull final TheRExecutionResult result, @NotNull final TheROutputReceiver receiver) {
    final String error = result.getError();

    if (!error.isEmpty()) {
      receiver.receiveError(error);
    }
  }

  public static void appendResult(@NotNull final TheRExecutionResult result, @NotNull final TheROutputReceiver receiver) {
    final TextRange range = result.getResultRange();

    if (!range.isEmpty()) {
      receiver.receiveOutput(
        range.substring(
          result.getOutput()
        )
      );
    }
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

  // TODO [dbg][test]
  public static int findLastLineBegin(@NotNull final CharSequence sequence) {
    int current = sequence.length() - 1;

    while (current > -1 && !StringUtil.isLineBreak(sequence.charAt(current))) {
      current--;
    }

    return current + 1;
  }

  // TODO [dbg][test]
  public static int findLastButOneLineEnd(@NotNull final CharSequence sequence, final int lastLineBegin) {
    int current = lastLineBegin - 1;

    while (current > -1 && StringUtil.isLineBreak(sequence.charAt(current))) {
      current--;
    }

    return current + 1;
  }
}
