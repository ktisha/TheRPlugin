package com.jetbrains.ther.typing;

public class TheRCharacterType extends TheRType {
  public static TheRCharacterType INSTANCE = new TheRCharacterType();

  @Override
  public String getName() {
    return "character";
  }
}
