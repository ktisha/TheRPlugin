package com.jetbrains.ther.debugger.interpreter;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

public class TheRProcessResponse {

  @NotNull
  private final String myOutput;

  @NotNull
  private final TheRProcessResponseType myType;

  @NotNull
  private final TextRange myResultRange;

  @NotNull
  private final String myError;

  public TheRProcessResponse(@NotNull final String output,
                             @NotNull final TheRProcessResponseType type,
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
  public TheRProcessResponseType getType() {
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
