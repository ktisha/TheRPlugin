package com.jetbrains.ther.typing;

public class TheRLogicalType implements  TheRType {
  public static TheRType INSTANCE = new TheRLogicalType();

  @Override
  public String getName() {
    return "logical";
  }

  @Override
  public TheRType resolveType(TheRTypeEnvironment env) {
    return INSTANCE;
  }
}
