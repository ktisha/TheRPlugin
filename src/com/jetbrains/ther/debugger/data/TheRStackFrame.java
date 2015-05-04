package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheRStackFrame {

  @NotNull
  private final TheRLocation myLocation;

  @NotNull
  private final List<TheRVar> myVars;

  public TheRStackFrame(@NotNull final TheRLocation location, @NotNull final List<TheRVar> vars) {
    myLocation = location;
    myVars = vars;
  }

  @NotNull
  public TheRLocation getLocation() {
    return myLocation;
  }

  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
  }
}
