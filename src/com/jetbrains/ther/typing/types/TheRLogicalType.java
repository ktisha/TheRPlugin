package com.jetbrains.ther.typing.types;

public class TheRLogicalType extends TheRIntegerType {
  public static TheRType INSTANCE = new TheRLogicalType();

  @Override
  public String getCanonicalName() {
    return "logical";
  }
}
