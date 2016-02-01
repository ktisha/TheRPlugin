package com.jetbrains.ther.debugger.evaluator;

import org.jetbrains.annotations.NotNull;

public interface TheRExpressionHandler {

  @NotNull
  String handle(final int frameNumber, @NotNull final String expression);

  void setLastFrameNumber(final int lastFrameNumber);
}
