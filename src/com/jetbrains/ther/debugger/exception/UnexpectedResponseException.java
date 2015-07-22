package com.jetbrains.ther.debugger.exception;

import org.jetbrains.annotations.NotNull;

public class UnexpectedResponseException extends TheRDebuggerException {

  public UnexpectedResponseException(@NotNull final String message) {
    super(message);
  }
}
