package com.jetbrains.ther.debugger;

import org.jetbrains.annotations.Nullable;

public class TheROutput {

  @Nullable
  private String myNormalOutput;

  @Nullable
  private String myErrorOutput;

  @Nullable
  public String getNormalOutput() {
    return myNormalOutput;
  }

  public void setNormalOutput(@Nullable final String normalOutput) {
    myNormalOutput = normalOutput;
  }

  @Nullable
  public String getErrorOutput() {
    return myErrorOutput;
  }

  public void setErrorOutput(@Nullable final String errorOutput) {
    myErrorOutput = errorOutput;
  }

  public void reset() {
    myNormalOutput = null;
    myErrorOutput = null;
  }
}
