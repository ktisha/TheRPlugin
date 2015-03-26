package com.jetbrains.ther.typing;

public class TheRCharacterType implements TheRType {
  public static TheRCharacterType INSTANCE = new TheRCharacterType();

  @Override
  public String getName() {
    return "character";
  }

  @Override
  public TheRType resolveType(TheRTypeEnvironment env) {
    return INSTANCE;
  }
}
