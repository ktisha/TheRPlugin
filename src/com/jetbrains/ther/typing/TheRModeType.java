package com.jetbrains.ther.typing;

public abstract class TheRModeType extends TheRType {
  @Override
  public TheRType getSubscriptionType() {
    return this;
  }
}
