package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRProcessResponseType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public final class TheRProcessResponseTypeCalculator {

  @NotNull
  private static final String BROWSE_REGEX = "Browse\\[\\d+\\]>";

  @NotNull
  private static final Pattern JUST_BROWSE_PATTERN = Pattern.compile("^" + BROWSE_REGEX + " $");

  @NotNull
  private static final Pattern DEBUGGING_PATTERN = Pattern.compile("^debugging in.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern START_TRACE_PATTERN = Pattern.compile("^Tracing .* on entry.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern CONTINUE_TRACE_PATTERN =
    Pattern.compile("^Tracing .* on exit.*debugging in.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern END_TRACE_PATTERN = Pattern.compile("^Tracing .* on exit.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  @NotNull
  private static final Pattern RESPONSE_AND_BROWSE_PATTERN = Pattern.compile("^.*" + BROWSE_REGEX + " $", Pattern.DOTALL);

  public static boolean isComplete(@NotNull final CharSequence response) {
    return isPlus(response) || RESPONSE_AND_BROWSE_PATTERN.matcher(response).matches();
  }

  @Nullable
  public static TheRProcessResponseType calculate(@NotNull final CharSequence response) {
    if (isPlus(response)) {
      return TheRProcessResponseType.PLUS;
    }

    if (JUST_BROWSE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.JUST_BROWSE;
    }

    if (DEBUGGING_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.DEBUGGING;
    }

    if (START_TRACE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.START_TRACE;
    }

    if (CONTINUE_TRACE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.CONTINUE_TRACE;
    }

    if (END_TRACE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.END_TRACE;
    }

    if (RESPONSE_AND_BROWSE_PATTERN.matcher(response).matches()) {
      return TheRProcessResponseType.RESPONSE_AND_BROWSE;
    }

    return null;
  }

  private static boolean isPlus(@NotNull final CharSequence response) {
    final int length = response.length();

    return length >= 2 && response.charAt(length - 1) == ' ' && response.charAt(length - 2) == '+';
    /*
    TODO
    final int length = response.length();
    final String lineSeparator = TheRDebugConstants.LINE_SEPARATOR;

    return length >= 2 + lineSeparator.length() &&
           response.charAt(length - 1) == ' ' &&
           response.charAt(length - 2) == '+' &&
           response.subSequence(length - 2 - lineSeparator.length(), length - 2).equals(lineSeparator);
    */
  }
}
