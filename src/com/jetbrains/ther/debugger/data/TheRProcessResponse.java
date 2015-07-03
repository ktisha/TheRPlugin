package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRProcessResponse {

  @NotNull
  private final String myText;

  @NotNull
  private final TheRProcessResponseType myType;

  public TheRProcessResponse(@NotNull final String text, @NotNull final TheRProcessResponseType type) {
    myText = text;
    myType = type;
  }

  @NotNull
  public String getText() {
    return myText;
  }

  @NotNull
  public TheRProcessResponseType getType() {
    return myType;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRProcessResponse response = (TheRProcessResponse)o;

    return myType == response.myType && myText.equals(response.myText);
  }

  @Override
  public int hashCode() {
    return 31 * myText.hashCode() + myType.hashCode();
  }
}
