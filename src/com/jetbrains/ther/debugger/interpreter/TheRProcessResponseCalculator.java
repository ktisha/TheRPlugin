package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponse;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.*;

final class TheRProcessResponseCalculator {

  @NotNull
  private static final Pattern START_TRACE_PATTERN = Pattern.compile("^" + TRACING + " .* on entry$");

  @NotNull
  private static final Pattern END_TRACE_PATTERN = Pattern.compile("^" + TRACING + " .* on exit$");

  public static boolean isComplete(@NotNull final CharSequence response) {
    return endsLineBreakAndPlusAndSpace(response) || endsLineBreakAndBrowseAndSpace(response);
  }

  @NotNull
  public static TheRProcessResponse calculate(@NotNull final String response) {
    final String[] lines = StringUtil.splitByLines(response);

    return new TheRProcessResponse(
      concatExcludeLast(lines),
      calculateType(lines)
    );
  }

  private static boolean endsLineBreakAndPlusAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return isSubsequence(PLUS_AND_SPACE, sequence, length - PLUS_AND_SPACE.length()) &&
           StringUtil.isLineBreak(sequence.charAt(length - PLUS_AND_SPACE.length() - 1));
  }

  private static boolean endsLineBreakAndBrowseAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    if (isSubsequence(BROWSE_SUFFIX, sequence, length - BROWSE_SUFFIX.length())) {
      final int index = readDigitsBackward(sequence, length - BROWSE_SUFFIX.length() - 1);

      return index != -1 &&
             index != length - BROWSE_SUFFIX.length() - 1 &&
             isSubsequence(BROWSE_PREFIX, sequence, index - BROWSE_PREFIX.length() + 1) &&
             StringUtil.isLineBreak(sequence.charAt(index - BROWSE_PREFIX.length()));
    }
    else {
      return false;
    }
  }

  @NotNull
  private static String concatExcludeLast(@NotNull final String[] lines) {
    final StringBuilder sb = new StringBuilder();

    for (int i = 0; i < lines.length - 1; i++) {
      sb.append(lines[i]);

      if (i != lines.length - 2) {
        sb.append(TheRDebugConstants.LINE_SEPARATOR);
      }
    }

    return sb.toString();
  }

  @NotNull
  private static TheRProcessResponseType calculateType(@NotNull final String[] lines) {
    if (justPlusAndSpace(lines)) {
      return PLUS;
    }

    if (justBrowseAndSpace(lines)) {
      return EMPTY;
    }

    if (!endsBrowseAndSpace(lines)) {
      throw new IllegalArgumentException("Response is incomplete");
    }

    if (debugging(lines)) {
      return TheRProcessResponseType.DEBUGGING_IN;
    }

    if (debugAt(lines)) {
      return TheRProcessResponseType.DEBUG_AT;
    }

    if (startTrace(lines)) {
      return START_TRACE;
    }

    if (continueTrace(lines)) {
      return CONTINUE_TRACE;
    }

    if (endTrace(lines)) {
      return END_TRACE;
    }

    return RESPONSE;
  }

  private static boolean isSubsequence(@NotNull final CharSequence sequence,
                                       @NotNull final CharSequence text,
                                       final int beginIndex) {
    if (beginIndex < 0 || beginIndex + sequence.length() > text.length()) {
      return false;
    }

    for (int i = 0; i < sequence.length(); i++) {
      if (sequence.charAt(i) != text.charAt(beginIndex + i)) {
        return false;
      }
    }

    return true;
  }

  private static boolean justPlusAndSpace(@NotNull final String[] lines) {
    return lines.length == 1 && lines[0].equals(PLUS_AND_SPACE);
  }

  private static boolean justBrowseAndSpace(@NotNull final String[] lines) {
    return lines.length == 1 && justBrowseAndSpace(lines[0]);
  }

  private static boolean endsBrowseAndSpace(@NotNull final String[] lines) {
    return lines.length > 0 && justBrowseAndSpace(lines[lines.length - 1]);
  }

  private static boolean debugging(@NotNull final String[] lines) {
    return lines.length > 0 && lines[0].startsWith(TheRDebugConstants.DEBUGGING_IN);
  }

  private static boolean debugAt(@NotNull final String[] lines) {
    return lines.length > 1 && lines[lines.length - 2].startsWith(TheRDebugConstants.DEBUG_AT);
  }

  private static boolean startTrace(@NotNull final String[] lines) {
    for (final String line : lines) {
      if (START_TRACE_PATTERN.matcher(line).find()) {
        return true;
      }
    }

    return false;
  }

  private static boolean continueTrace(@NotNull final String[] lines) {
    for (int i = 0; i < lines.length - 5; i++) {
      if (continueTrace(lines, i)) {
        return true;
      }
    }

    return false;
  }

  private static boolean endTrace(@NotNull final String[] lines) {
    for (final String line : lines) {
      if (END_TRACE_PATTERN.matcher(line).find()) {
        return true;
      }
    }

    return false;
  }

  private static int readDigitsBackward(@NotNull final CharSequence sequence, final int beginIndex) {
    if (sequence.length() <= beginIndex || beginIndex <= -1) {
      return beginIndex;
    }

    for (int i = beginIndex; i > -1; i--) {
      if (!Character.isDigit(sequence.charAt(i))) {
        return i;
      }
    }

    return -1;
  }

  private static boolean justBrowseAndSpace(@NotNull final String line) {
    return line.startsWith(BROWSE_PREFIX) &&
           line.endsWith(BROWSE_SUFFIX) &&
           isDigits(line, BROWSE_PREFIX.length(), line.length() - BROWSE_SUFFIX.length() - 1);
  }

  private static boolean continueTrace(@NotNull final String[] lines, final int i) {
    return lines[i + 2].startsWith(EXITING_FROM) &&
           lines[i + 3].startsWith(TheRDebugConstants.DEBUGGING_IN) &&
           END_TRACE_PATTERN.matcher(lines[i]).find();
  }

  private static boolean isDigits(@NotNull final CharSequence sequence, final int beginIndex, final int endIndex) { // [l..r]
    return beginIndex >= 0 &&
           endIndex >= beginIndex &&
           endIndex < sequence.length() &&
           readDigitsBackward(sequence, endIndex) == beginIndex - 1;
  }
}
