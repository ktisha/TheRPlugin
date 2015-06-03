package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

class TheRDebuggedFunctionsNode {

  @NotNull
  private final String myName;

  @Nullable
  private final TheRDebuggedFunctionsNode myParent;

  @NotNull
  private final Map<String, TheRDebuggedFunctionsNode> myChildren;

  private boolean myDebugged;

  public TheRDebuggedFunctionsNode(@NotNull final String name, @Nullable final TheRDebuggedFunctionsNode parent) {
    myName = name;
    myParent = parent;

    myChildren = new HashMap<String, TheRDebuggedFunctionsNode>();
    myDebugged = false;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public TheRDebuggedFunctionsNode getParent() {
    return myParent;
  }

  @NotNull
  public Map<String, TheRDebuggedFunctionsNode> getChildren() {
    return myChildren;
  }

  public boolean isDebugged() {
    return myDebugged;
  }

  public void setDebugged(final boolean debugged) {
    myDebugged = debugged;
  }
}
