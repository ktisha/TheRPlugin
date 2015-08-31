package com.jetbrains.ther.debugger.executor;

import org.jetbrains.annotations.NotNull;

public interface TheRExecutionResultCalculator {

  boolean isComplete(@NotNull final CharSequence output);

  @NotNull
  TheRExecutionResult calculate(@NotNull final CharSequence output, @NotNull final String error);
}
