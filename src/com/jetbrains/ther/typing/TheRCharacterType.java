package com.jetbrains.ther.typing;

public class TheRCharacterType extends TheRModeType {
  public static TheRCharacterType INSTANCE = new TheRCharacterType();

  @Override
  public String getName() {
    return "character";
  }
}
