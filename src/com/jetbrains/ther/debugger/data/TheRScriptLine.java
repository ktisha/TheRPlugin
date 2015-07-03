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

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRScriptLine line = (TheRScriptLine)o;

    return myNumber == line.myNumber && (myText == null ? line.myText == null : myText.equals(line.myText));
  }

  @Override
  public int hashCode() {
    return 31 * (myText == null ? 0 : myText.hashCode()) + myNumber;
  }
}
