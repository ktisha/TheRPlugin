package com.jetbrains.ther.typing.types;

public class TheRNullType extends TheRType {
  public static TheRType INSTANCE = new TheRNullType();

  @Override
  public String getName() {
    return "null";
  }
}
