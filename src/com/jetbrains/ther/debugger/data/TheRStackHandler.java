package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheRStackHandler {

  @NotNull
  private final List<TheRStackFrame> myStack;

  @NotNull
  private final List<TheRStackFrame> myUnmodifiableStack;

  public TheRStackHandler() {
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);
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

  @NotNull
  public List<TheRStackFrame> getStack() {
    return myUnmodifiableStack;
  }
}
