package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public enum TheRProcessResponseType {

  PLUS, JUST_BROWSE, RESPONSE_AND_BROWSE, DEBUG;

  @NotNull
  private static final Pattern JUST_BROWSE_PATTERN = Pattern.compile("^Browse\\[\\d+\\]> $");

  @NotNull
  private static final Pattern ENDS_WITH_BROWSE_PATTERN = Pattern.compile("^.*Browse\\[\\d+\\]> $", Pattern.DOTALL);

  @NotNull
  private static final Pattern DEBUGGING_PATTERN = Pattern.compile("^debugging in.*$", Pattern.DOTALL);

  @Nullable
  public static TheRProcessResponseType calculateResponseType(@NotNull final CharSequence response) {
    if (endsWithPlusAndSpace(response)) {
      return PLUS;
    }

    if (JUST_BROWSE_PATTERN.matcher(response).matches()) {
      return JUST_BROWSE;
    }

    if (DEBUGGING_PATTERN.matcher(response).matches()) {
      return DEBUG;
    }

    if (ENDS_WITH_BROWSE_PATTERN.matcher(response).matches()) {
      return RESPONSE_AND_BROWSE;
    }

    return null;
  }

  private static boolean endsWithPlusAndSpace(@NotNull final CharSequence sequence) {
    final int length = sequence.length();

    return length >= 2 && sequence.charAt(length - 1) == ' ' && sequence.charAt(length - 2) == '+';
  }
}
