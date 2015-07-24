package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRDebuggerEvaluator implements TheRDebuggerEvaluator {

  @Override
  public void evalCondition(@NotNull final String condition, @NotNull final Receiver<Boolean> receiver) {
    throw new IllegalStateException("EvalCondition shouldn't be called");
  }

  @Override
  public void evalExpression(@NotNull final String expression, @NotNull final Receiver<String> receiver) {
    throw new IllegalStateException("EvalExpression shouldn't be called");
  }
}
