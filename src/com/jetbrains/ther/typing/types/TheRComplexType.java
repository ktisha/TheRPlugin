package com.jetbrains.ther.typing.types;

public class TheRComplexType extends TheRAtomicType {
  public static TheRComplexType INSTANCE = new TheRComplexType();

  @Override
  public String getName() {
    return "complex";
  }
}
