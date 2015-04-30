package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;

public class TheRVar {

  @NotNull
  private final String myName;

  @NotNull
  private final String myType;

  @NotNull
  private final String myValue;

  public TheRVar(@NotNull final String name, @NotNull final String type, @NotNull final String value) {
    myName = name;
    myType = type;
    myValue = value;
  }

  @NotNull
  public String getName() {
    return myName;
  }

  @NotNull
  public String getType() {
    return myType;
  }

  @NotNull
  public String getValue() {
    return myValue;
  }
}
