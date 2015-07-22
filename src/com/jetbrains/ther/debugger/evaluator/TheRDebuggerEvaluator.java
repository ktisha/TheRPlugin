package com.jetbrains.ther.debugger.evaluator;

import org.jetbrains.annotations.NotNull;

public interface TheRDebuggerEvaluator {

  void evalCondition(@NotNull final String condition, @NotNull final ConditionReceiver receiver);

  void evalExpression(@NotNull final String expression, @NotNull final ExpressionReceiver receiver);

  interface ConditionReceiver {

    void receiveResult(final boolean result);

    void receiveError(@NotNull final Exception e);
  }

  interface ExpressionReceiver {

    void receiveResult(@NotNull final String result);

    void receiveError(@NotNull final Exception e);
  }
}
