package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

public class TheRLocation {

  @NotNull
  private final String myFunction;

  private final int myLine;

  public TheRLocation(@NotNull final String function, final int line) {
    myFunction = function;
    myLine = line;
  }

  @NotNull
  public String getFunction() {
    return myFunction;
  }

  public int getLine() {
    return myLine;
  }
}
