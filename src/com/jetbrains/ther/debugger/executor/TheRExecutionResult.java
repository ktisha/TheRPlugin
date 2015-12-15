package com.jetbrains.ther.debugger.executor;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

public class TheRExecutionResult {

  @NotNull
  private final String myOutput;

  @NotNull
  private final TheRExecutionResultType myType;

  @NotNull
  private final TextRange myResultRange;

  @NotNull
  private final String myError;

  public TheRExecutionResult(@NotNull final String output,
                             @NotNull final TheRExecutionResultType type,
                             @NotNull final TextRange resultRange,
                             @NotNull final String error) {
    myOutput = output;
    myType = type;
    myResultRange = resultRange;
    myError = error;
  }

  @NotNull
  public String getOutput() {
    return myOutput;
  }

  @NotNull
  public TheRExecutionResultType getType() {
    return myType;
  }

  @NotNull
  public TextRange getResultRange() {
    return myResultRange;
  }

  @NotNull
  public String getError() {
    return myError;
  }
}
