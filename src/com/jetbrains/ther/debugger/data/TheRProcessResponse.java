package com.jetbrains.ther.debugger.data;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRProcessResponse {

  @NotNull
  private final String myText;

  @NotNull
  private final TheRProcessResponseType myType;

  @NotNull
  private final TextRange myOutputRange;


  public TheRProcessResponse(@NotNull final String text,
                             @NotNull final TheRProcessResponseType type,
                             @NotNull final TextRange outputRange) {
    myText = text;
    myType = type;
    myOutputRange = outputRange;
  }

  @NotNull
  public String getText() {
    return myText;
  }

  @NotNull
  public TheRProcessResponseType getType() {
    return myType;
  }

  @NotNull
  public TextRange getOutputRange() {
    return myOutputRange;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRProcessResponse response = (TheRProcessResponse)o;

    return myType == response.myType && myText.equals(response.myText) && myOutputRange.equals(response.myOutputRange);
  }

  @Override
  public int hashCode() {
    return 31 * (31 * myText.hashCode() + myType.hashCode()) + myOutputRange.hashCode();
  }
}
