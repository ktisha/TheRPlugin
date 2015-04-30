package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TheRStack {

  @NotNull
  private final List<TheRStackFrame> myStack;

  public TheRStack() {
    myStack = new ArrayList<TheRStackFrame>();
  }

  public void addEntry() {
    myStack.add(null);
  }

  public void removeEntry() {
    myStack.remove(myStack.size() - 1);
  }

  public void updateCurrent(@NotNull final TheRStackFrame current) {
    myStack.set(myStack.size() - 1, current);
  }
}
