package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TheRStackFrame {

  @NotNull
  private final String myFunction;

  private final int myLine;

  @NotNull
  private final List<TheRVar> myVars;

  public TheRStackFrame(@NotNull final String function, final int line, @NotNull final List<TheRVar> vars) {
    myFunction = function;
    myLine = line;
    myVars = vars;
  }

  @NotNull
  public String getFunction() {
    return myFunction;
  }

  public int getLine() {
    return myLine;
  }

  @NotNull
  public List<TheRVar> getVars() {
    return myVars;
  }
}
