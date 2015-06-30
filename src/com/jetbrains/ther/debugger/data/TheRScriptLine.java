package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.Nullable;

public class TheRScriptLine {

  @Nullable
  private final String myText;

  private final int myNumber;

  public TheRScriptLine(@Nullable final String text, final int number) {
    myText = text;
    myNumber = number;
  }

  @Nullable
  public String getText() {
    return myText;
  }

  public int getNumber() {
    return myNumber;
  }
}
