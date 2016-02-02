package com.jetbrains.ther.typing.types;

/**
 * Actually this class implements bottom type from type theory
 */
public class TheRErrorType extends TheRType {
  private final String myErrorMessage;

  public TheRErrorType(String errorMessage) {
    myErrorMessage = errorMessage;
  }

  @Override
  public String getCanonicalName() {
    return "error: " + myErrorMessage;
  }

  public String getErrorMessage() {
    return myErrorMessage;
  }
}
