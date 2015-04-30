package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

public class TheRProcessResponseAndType {

  @NotNull
  private final String myResponse;

  @NotNull
  private final TheRProcessResponseType myType;

  public TheRProcessResponseAndType(@NotNull final String response, @NotNull final TheRProcessResponseType type) {
    myResponse = response;
    myType = type;
  }

  @NotNull
  public String getResponse() {
    return myResponse;
  }

  @NotNull
  public TheRProcessResponseType getType() {
    return myType;
  }
}
