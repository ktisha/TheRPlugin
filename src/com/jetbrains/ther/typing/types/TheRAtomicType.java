package com.jetbrains.ther.typing.types;

public abstract class TheRAtomicType extends TheRType {
  @Override
  public TheRType getSubscriptionType() {
    return this;
  }
}
