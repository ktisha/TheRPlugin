package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheRStackFrame {

  @NotNull
  private final TheRLocation myLocation;

  @NotNull
  private final List<TheRVar> myVars;

  public TheRStackFrame(@NotNull final TheRLocation location, @NotNull final List<TheRVar> vars) {
    myLocation = location;
    myVars = Collections.unmodifiableList(new ArrayList<TheRVar>(vars));
  }

  @NotNull
  public TheRLocation getLocation() {
    return myLocation;
  }

  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRStackFrame frame = (TheRStackFrame)o;

    return myLocation.equals(frame.myLocation) && myVars.equals(frame.myVars);
  }

  @Override
  public int hashCode() {
    return 31 * myLocation.hashCode() + myVars.hashCode();
  }
}
