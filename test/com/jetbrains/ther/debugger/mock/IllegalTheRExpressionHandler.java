package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRExpressionHandler;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRExpressionHandler implements TheRExpressionHandler {

  @NotNull
  @Override
  public String handle(final int frameNumber, @NotNull final String expression) {
    throw new IllegalStateException("Handle shouldn't be called");
  }

  @Override
  public void setMaxFrameNumber(final int maxFrameNumber) {
    throw new IllegalStateException("SetMaxFrameNumber shouldn't be called");
  }
}
