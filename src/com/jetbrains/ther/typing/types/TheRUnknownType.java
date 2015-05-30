package com.jetbrains.ther.typing.types;

public class TheRUnknownType extends TheRType {
  public static final TheRUnknownType INSTANCE = new TheRUnknownType();

  @Override
  public String getCanonicalName() {
    return "Unknown";
  }

  @Override
  public TheRType getSlotType(String tag) {
    return TheRUnknownType.INSTANCE;
  }
}
