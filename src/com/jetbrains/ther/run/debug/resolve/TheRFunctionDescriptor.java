package com.jetbrains.ther.run.debug.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TheRFunctionDescriptor {

  @NotNull
  private final String myName;

  @Nullable
  private final TheRFunctionDescriptor myParent;

  @NotNull
  private final Map<String, List<TheRFunctionDescriptor>> myChildren;

  private final int myStartLine;
  private final int myEndLine;

  public TheRFunctionDescriptor(@NotNull final String name,
                                @Nullable final TheRFunctionDescriptor parent,
                                final int startLine,
                                final int endLine) {
    myName = name;
    myParent = parent;
    myChildren = new HashMap<String, List<TheRFunctionDescriptor>>();
    myStartLine = startLine;
    myEndLine = endLine;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public TheRFunctionDescriptor getParent() {
    return myParent;
  }

  @NotNull
  public Map<String, List<TheRFunctionDescriptor>> getChildren() {
    return myChildren;
  }

  public int getStartLine() {
    return myStartLine;
  }

  public int getEndLine() {
    return myEndLine;
  }
}
