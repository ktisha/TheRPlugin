package com.jetbrains.ther.typing;

public class TheRNumericType extends TheRModeType {
  public static TheRNumericType INSTANCE = new TheRNumericType();

  @Override
  public String getName() {
    return "numeric";
  }
}
