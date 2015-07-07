package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.NotNull;

public interface TheRDebuggerEvaluator {

  void evalCondition(@NotNull final String condition, @NotNull final ConditionReceiver receiver);

  void evalExpression(@NotNull final String expression, @NotNull final ExpressionReceiver receiver);

  interface Receiver<T> {

    void receiveResult(@NotNull final T result);

    void receiveError(@NotNull final Exception e);

    void receiveError(@NotNull final String error);
  }

  interface ConditionReceiver extends Receiver<Boolean> {
  }

  interface ExpressionReceiver extends Receiver<String> {
  }
}
