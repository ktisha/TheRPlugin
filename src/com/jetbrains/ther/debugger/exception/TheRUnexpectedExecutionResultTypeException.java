package com.jetbrains.ther.debugger.exception;

import org.jetbrains.annotations.NotNull;

public class TheRUnexpectedExecutionResultTypeException extends TheRDebuggerException {

  public TheRUnexpectedExecutionResultTypeException(@NotNull final String message) {
    super(message);
  }
}
