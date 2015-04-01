package com.jetbrains.ther.typing.types;

public class TheRIntegerType extends TheRNumericType {
  public static TheRIntegerType INSTANCE = new TheRIntegerType();

  @Override
  public String getName() {
    return "integer";
  }
}
