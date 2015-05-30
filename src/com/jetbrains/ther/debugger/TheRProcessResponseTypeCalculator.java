package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class TheRProcessResponseTypeCalculator {

  @NotNull
  private static final String LINE_SEPARATOR = TheRDebugConstants.LINE_SEPARATOR;

  @NotNull
  private static final Pattern START_TRACE_PATTERN = Pattern.compile("^" + TheRDebugConstants.TRACING + " .* on entry");

  @NotNull
  private static final Pattern CONTINUE_TRACE_PATTERN =
    Pattern.compile("^" + TheRDebugConstants.TRACING + " .* on exit.*" + TheRDebugConstants.DEBUGGING_IN, Pattern.DOTALL);

  @NotNull
  private static final Pattern END_TRACE_PATTERN = Pattern.compile("^" + TheRDebugConstants.TRACING + " .* on exit");

  public static boolean isComplete(@NotNull final CharSequence response) {
    return endsPlusAndSpace(response) || endsBrowseAndSpace(response);
  }

  @Nullable
  public static TheRProcessResponseType calculate(@NotNull final CharSequence response) {
    if (justPlusAndSpace(response)) {
      return TheRProcessResponseType.PLUS;
    }

    if (justBrowseAndSpace(response)) {
      return TheRProcessResponseType.JUST_BROWSE;
    }

    if (!endsBrowseAndSpace(response)) {
      return null;
    }

    if (debugging(response)) {
      return TheRProcessResponseType.DEBUGGING;
    }

    if (START_TRACE_PATTERN.matcher(response).find()) {
      return TheRProcessResponseType.START_TRACE;
    }

    if (CONTINUE_TRACE_PATTERN.matcher(response).find()) {
      return TheRProcessResponseType.CONTINUE_TRACE;
    }

    if (END_TRACE_PATTERN.matcher(response).find()) {
      return TheRProcessResponseType.END_TRACE;
    }

    return TheRProcessResponseType.RESPONSE_AND_BROWSE;
  }

  private static boolean endsPlusAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return isSubstring(TheRDebugConstants.PLUS_AND_SPACE, sequence, length - TheRDebugConstants.PLUS_AND_SPACE.length()) &&
           isSubstring(LINE_SEPARATOR, sequence, length - TheRDebugConstants.PLUS_AND_SPACE.length() - LINE_SEPARATOR.length());
  }

  private static boolean endsBrowseAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    if (isSubstring(TheRDebugConstants.BROWSE_SUFFIX, sequence, length - TheRDebugConstants.BROWSE_SUFFIX.length())) {
      final int index = readDigitsBackward(sequence, length - TheRDebugConstants.BROWSE_SUFFIX.length() - 1);

      return index != -1 &&
             index != length - TheRDebugConstants.BROWSE_SUFFIX.length() - 1 &&
             isSubstring(TheRDebugConstants.BROWSE_PREFIX, sequence, index - TheRDebugConstants.BROWSE_PREFIX.length() + 1) &&
             isSubstring(LINE_SEPARATOR, sequence, index - TheRDebugConstants.BROWSE_PREFIX.length() + 1 - LINE_SEPARATOR.length());
    }
    else {
      return false;
    }
  }

  private static boolean justPlusAndSpace(@NotNull final CharSequence sequence) {
    return sequence.equals(TheRDebugConstants.PLUS_AND_SPACE);
  }

  private static boolean justBrowseAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return isSubstring(TheRDebugConstants.BROWSE_PREFIX, sequence, 0) &&
           isSubstring(TheRDebugConstants.BROWSE_SUFFIX, sequence, length - TheRDebugConstants.BROWSE_SUFFIX.length()) &&
           isDigits(sequence, TheRDebugConstants.BROWSE_PREFIX.length(), length - TheRDebugConstants.BROWSE_SUFFIX.length() - 1);
  }

  private static boolean debugging(@NotNull final CharSequence sequence) {
    return isSubstring(TheRDebugConstants.DEBUGGING_IN, sequence, 0);
  }

  private static boolean isSubstring(@NotNull final CharSequence sequence,
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

  private static boolean isDigits(@NotNull final CharSequence sequence, final int beginIndex, final int endIndex) { // [l..r]
    return beginIndex >= 0 &&
           endIndex >= beginIndex &&
           endIndex < sequence.length() &&
           readDigitsBackward(sequence, endIndex) == beginIndex - 1;
  }
}
