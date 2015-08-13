package com.jetbrains.ther.debugger.frame;

import org.jetbrains.annotations.NotNull;

public class TheRVar {

  @NotNull
  private final String myName;

  @NotNull
  private final String myType;

  @NotNull
  private final String myValue;

  @NotNull
  private final TheRValueModifier myModifier;

  public TheRVar(@NotNull final String name,
                 @NotNull final String type,
                 @NotNull final String value,
                 @NotNull final TheRValueModifier modifier) {
    myName = name;
    myType = type;
    myValue = value;
    myModifier = modifier;
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

  @NotNull
  public TheRValueModifier getModifier() {
    return myModifier;
  }
}
