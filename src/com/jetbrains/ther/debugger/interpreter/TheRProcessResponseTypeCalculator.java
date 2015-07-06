package com.jetbrains.ther.debugger.interpreter;

import com.jetbrains.ther.debugger.data.TheRDebugConstants;
import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

import static com.jetbrains.ther.debugger.data.TheRDebugConstants.*;
import static com.jetbrains.ther.debugger.data.TheRProcessResponseType.*;

final class TheRProcessResponseTypeCalculator {

  @NotNull
  private static final Pattern START_TRACE_PATTERN = Pattern.compile("^" + TRACING + " .* on entry");

  @NotNull
  private static final Pattern CONTINUE_TRACE_PATTERN =
    Pattern.compile("^" + TRACING + " .* on exit.*" + TheRDebugConstants.DEBUGGING_IN, Pattern.DOTALL);

  @NotNull
  private static final Pattern END_TRACE_PATTERN = Pattern.compile("^" + TRACING + " .* on exit");

  public static boolean isComplete(@NotNull final CharSequence response) {
    return endsPlusAndSpace(response) || endsBrowseAndSpace(response);
  }

  @Nullable
  public static TheRProcessResponseType calculate(@NotNull final CharSequence response, final int beginIndex) {
    return calculate(new OffsetSequence(response, beginIndex));
  }

  @Nullable
  public static TheRProcessResponseType calculate(@NotNull final CharSequence response) {
    if (justPlusAndSpace(response)) {
      return PLUS;
    }

    if (justBrowseAndSpace(response)) {
      return EMPTY;
    }

    if (!endsBrowseAndSpace(response)) {
      return null;
    }

    if (debugging(response)) {
      return TheRProcessResponseType.DEBUGGING_IN;
    }

    if (debugAt(response)) {
      return TheRProcessResponseType.DEBUG_AT;
    }

    if (START_TRACE_PATTERN.matcher(response).find()) {
      return START_TRACE;
    }

    if (CONTINUE_TRACE_PATTERN.matcher(response).find()) {
      return CONTINUE_TRACE;
    }

    if (END_TRACE_PATTERN.matcher(response).find()) {
      return END_TRACE;
    }

    return RESPONSE;
  }

  private static boolean endsPlusAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return isSubstring(PLUS_AND_SPACE, sequence, length - PLUS_AND_SPACE.length()) &&
           isSubstring(
             LINE_SEPARATOR,
             sequence,
             length - PLUS_AND_SPACE.length() - LINE_SEPARATOR.length()
           );
  }

  private static boolean endsBrowseAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    if (isSubstring(BROWSE_SUFFIX, sequence, length - BROWSE_SUFFIX.length())) {
      final int index = readDigitsBackward(sequence, length - BROWSE_SUFFIX.length() - 1);

      return index != -1 &&
             index != length - BROWSE_SUFFIX.length() - 1 &&
             isSubstring(BROWSE_PREFIX, sequence, index - BROWSE_PREFIX.length() + 1) &&
             isSubstring(
               LINE_SEPARATOR,
               sequence,
               index - BROWSE_PREFIX.length() + 1 - LINE_SEPARATOR.length()
             );
    }
    else {
      return false;
    }
  }

  private static boolean justPlusAndSpace(@NotNull final CharSequence sequence) {
    // don't use "equals" here because it is not overridden in OffsetSequence

    return sequence.length() == PLUS_AND_SPACE.length() && isSubstring(PLUS_AND_SPACE, sequence, 0);
  }

  private static boolean justBrowseAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return isSubstring(BROWSE_PREFIX, sequence, 0) &&
           isSubstring(BROWSE_SUFFIX, sequence, length - BROWSE_SUFFIX.length()) &&
           isDigits(sequence, BROWSE_PREFIX.length(), length - BROWSE_SUFFIX.length() - 1);
  }

  private static boolean debugging(@NotNull final CharSequence sequence) {
    return isSubstring(TheRDebugConstants.DEBUGGING_IN, sequence, 0);
  }

  private static boolean debugAt(@NotNull final CharSequence sequence) {
    return isSubstring(TheRDebugConstants.DEBUG_AT, sequence, 0);
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

  private static class OffsetSequence implements CharSequence {

    @NotNull
    private final CharSequence mySequence;

    private final int myOffset;

    public OffsetSequence(@NotNull final CharSequence sequence, final int offset) {
      mySequence = sequence;
      myOffset = offset;
    }

    @Override
    public int length() {
      return mySequence.length() - myOffset;
    }

    @Override
    public char charAt(final int index) {
      return mySequence.charAt(myOffset + index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
      return mySequence.subSequence(myOffset + start, myOffset + end);
    }
  }
}
