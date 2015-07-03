package com.jetbrains.ther.debugger.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TheRFunction {

  @NotNull
  private final List<String> myDefinition;

  public TheRFunction(@NotNull final List<String> definition) {
    if (definition.isEmpty()) {
      throw new IllegalArgumentException("Definition of function couldn't be empty");
    }

    myDefinition = Collections.unmodifiableList(new ArrayList<String>(definition));
  }

  @NotNull
  public List<String> getDefinition() {
    return myDefinition;
  }

  @NotNull
  public String getName() {
    return myDefinition.get(myDefinition.size() - 1);
  }

  @Override
  public boolean equals(@Nullable final Object o) {
    if (o == this) return true;
    if (o == null || getClass() != o.getClass()) return false;

    final TheRFunction function = (TheRFunction)o;

    return myDefinition.equals(function.myDefinition);
  }

  @Override
  public int hashCode() {
    return myDefinition.hashCode();
  }
}
