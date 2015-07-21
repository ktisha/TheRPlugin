package com.jetbrains.ther.xdebugger.resolve;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TheRXFunctionDescriptor {

  @NotNull
  private final String myName;

  @Nullable
  private final TheRXFunctionDescriptor myParent;

  @NotNull
  private final Map<String, List<TheRXFunctionDescriptor>> myChildren;

  private final int myStartLine;
  private final int myEndLine;

  public TheRXFunctionDescriptor(@NotNull final String name,
                                 @Nullable final TheRXFunctionDescriptor parent,
                                 final int startLine,
                                 final int endLine) {
    myName = name;
    myParent = parent;
    myChildren = new HashMap<String, List<TheRXFunctionDescriptor>>();
    myStartLine = startLine;
    myEndLine = endLine;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @Nullable
  public TheRXFunctionDescriptor getParent() {
    return myParent;
  }

  @NotNull
  public Map<String, List<TheRXFunctionDescriptor>> getChildren() {
    return myChildren;
  }

  public int getStartLine() {
    return myStartLine;
  }

  public int getEndLine() {
    return myEndLine;
  }
}
