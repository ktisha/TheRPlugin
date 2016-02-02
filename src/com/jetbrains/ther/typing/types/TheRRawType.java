package com.jetbrains.ther.typing.types;

public class TheRRawType extends TheRAtomicType {
  public static TheRRawType INSTANCE = new TheRRawType();

  @Override
  public String getCanonicalName() {
    return "raw";
  }
}
