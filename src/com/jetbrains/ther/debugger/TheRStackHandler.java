package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRStackFrame;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class TheRStackHandler {

  @NotNull
  private final List<TheRStackFrame> myStack;

  @NotNull
  private final List<TheRStackFrame> myUnmodifiableStack;

  TheRStackHandler() {
    myStack = new ArrayList<TheRStackFrame>();
    myUnmodifiableStack = Collections.unmodifiableList(myStack);
  }

  public void addFrame() {
    myStack.add(null);
  }

  public void removeFrame() {
    myStack.remove(myStack.size() - 1);
  }

  public void updateCurrentFrame(@NotNull final TheRStackFrame frame) {
    myStack.set(myStack.size() - 1, frame);
  }

  @NotNull
  public List<TheRStackFrame> getStack() {
    return myUnmodifiableStack;
  }

  public boolean isMain() {
    return myStack.size() == 1;
  }

  @NotNull
  public TheRLocation getCurrentLocation() {
    return myStack.get(myStack.size() - 1).getLocation();
  }
}
