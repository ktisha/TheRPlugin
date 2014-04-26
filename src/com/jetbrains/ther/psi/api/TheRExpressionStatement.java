package com.jetbrains.ther.psi.api;

import org.jetbrains.annotations.NotNull;

public interface TheRExpressionStatement extends TheRStatement {
  @NotNull
  TheRExpression getExpression();
}
