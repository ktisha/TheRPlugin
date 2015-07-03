package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRVar var = (TheRVar)o;

    return myName.equals(var.myName) && myType.equals(var.myType) && myValue.equals(var.myValue);
  }

  @Override
  public int hashCode() {
    return 31 * (31 * myName.hashCode() + myType.hashCode()) + myValue.hashCode();
  }
}
