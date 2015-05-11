package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

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
}
