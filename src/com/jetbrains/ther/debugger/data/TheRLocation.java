package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TheRLocation {

  @NotNull
  private final TheRFunction myFunction;

  private final int myLine;

  public TheRLocation(@NotNull final TheRFunction function, final int line) {
    myFunction = function;
    myLine = line;
  }

  @NotNull
  public TheRFunction getFunction() {
    return myFunction;
  }

  public int getLine() {
    return myLine;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRLocation location = (TheRLocation)o;

    return myLine == location.myLine && myFunction.equals(location.myFunction);
  }

  @Override
  public int hashCode() {
    return 31 * myFunction.hashCode() + myLine;
  }
}
