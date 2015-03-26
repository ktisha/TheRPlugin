package com.jetbrains.ther.typing;

public class TheRLogicalType extends TheRType {
  public static TheRType INSTANCE = new TheRLogicalType();

  @Override
  public String getName() {
    return "logical";
  }
}
