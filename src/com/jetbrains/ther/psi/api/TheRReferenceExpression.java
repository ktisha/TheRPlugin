package com.jetbrains.ther.psi.api;

import org.jetbrains.annotations.Nullable;

public interface TheRReferenceExpression extends TheRExpression {

  @Nullable
  public String getNamespace();
}
