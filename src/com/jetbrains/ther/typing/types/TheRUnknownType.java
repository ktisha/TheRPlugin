package com.jetbrains.ther.typing.types;

/**
 * Created by liana on 11/05/15.
 */
public class TheRUnknownType extends TheRType {
  public static final TheRUnknownType INSTANCE = new TheRUnknownType();

  @Override
  public String getCanonicalName() {
    return "Unknown";
  }
}
