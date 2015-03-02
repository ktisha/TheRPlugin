package com.jetbrains.ther.psi.stubs;

import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;

public class TheRElementTypeFactory {
  private TheRElementTypeFactory() {
  }

  public static IElementType getElementTypeByName(@NotNull String name) {
    if (name.equals("THE_R_ASSIGNMENT_STATEMENT")) {
      return new TheRAssignmentElementType(name);
    }
    throw new IllegalArgumentException("Unknown element type: " + name);
  }
}
