package com.jetbrains.ther.debugger.exception;

import org.jetbrains.annotations.NotNull;

public class TheRUnexpectedResponseException extends TheRDebuggerException {

  public TheRUnexpectedResponseException(@NotNull final String message) {
    super(message);
  }
}
