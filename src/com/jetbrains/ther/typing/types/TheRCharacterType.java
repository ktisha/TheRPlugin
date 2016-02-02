package com.jetbrains.ther.typing.types;

public class TheRCharacterType extends TheRAtomicType {
  public static TheRCharacterType INSTANCE = new TheRCharacterType();

  @Override
  public String getCanonicalName() {
    return "character";
  }
}
