package com.jetbrains.ther.psi.api;

import org.jetbrains.annotations.Nullable;

public interface TheRForStatement extends TheRElement {
  @Nullable
  TheRExpression getTarget();

}
