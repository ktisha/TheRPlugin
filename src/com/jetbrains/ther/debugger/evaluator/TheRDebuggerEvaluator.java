package com.jetbrains.ther.debugger.evaluator;

import org.jetbrains.annotations.NotNull;

public interface TheRDebuggerEvaluator {

  void evalCondition(@NotNull final String condition, @NotNull final Receiver<Boolean> receiver);

  void evalExpression(@NotNull final String expression, @NotNull final Receiver<String> receiver);

  interface Receiver<T> {

    void receiveResult(@NotNull final T result);

    void receiveError(@NotNull final Exception e);

    void receiveError(@NotNull final String error);
  }
}
