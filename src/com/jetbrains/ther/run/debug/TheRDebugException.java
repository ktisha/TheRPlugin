package com.jetbrains.ther.run.debug;

import org.jetbrains.annotations.NotNull;

public class TheRDebugException extends Exception {

  public TheRDebugException(@NotNull final String message) {
    super(message);
  }
}
