package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRDebuggerEvaluator implements TheRDebuggerEvaluator {

  @Override
  public void evalExpression(@NotNull final String expression, @NotNull final Receiver receiver) {
    throw new IllegalStateException("EvalExpression shouldn't be called");
  }
}
