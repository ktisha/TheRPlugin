package com.jetbrains.ther.typing.types;

public class TheRNullType extends TheRAtomicType {
  public static TheRType INSTANCE = new TheRNullType();

  @Override
  public String getCanonicalName() {
    return "null";
  }
}
