package com.jetbrains.ther.typing;

public class TheRNumericType implements TheRType {
  public static TheRNumericType INSTANCE = new TheRNumericType();

  @Override
  public String getName() {
    return "numeric";
  }
}
