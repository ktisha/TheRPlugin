package com.jetbrains.ther.debugger.exception;

import org.jetbrains.annotations.NotNull;

public class TheRDebuggerException extends Exception {

  public TheRDebuggerException(@NotNull final Exception cause) {
    super(cause);
  }

  public TheRDebuggerException(@NotNull final String message) {
    super(message);
  }
}
