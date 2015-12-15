package com.jetbrains.ther.debugger.exception;

import org.jetbrains.annotations.NotNull;

public class TheRUnexpectedExecutionResultException extends TheRDebuggerException {

  public TheRUnexpectedExecutionResultException(@NotNull final String message) {
    super(message);
  }
}
