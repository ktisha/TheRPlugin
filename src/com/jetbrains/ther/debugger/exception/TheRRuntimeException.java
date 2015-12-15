package com.jetbrains.ther.debugger.exception;

import org.jetbrains.annotations.NotNull;

public class TheRRuntimeException extends TheRDebuggerException {

  public TheRRuntimeException(@NotNull final String message) {
    super(message);
  }
}
