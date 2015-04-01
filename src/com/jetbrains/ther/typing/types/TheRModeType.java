package com.jetbrains.ther.typing.types;

import com.jetbrains.ther.typing.types.TheRType;

public abstract class TheRModeType extends TheRType {
  @Override
  public TheRType getSubscriptionType() {
    return this;
  }
}
