package com.jetbrains.ther.debugger.frame;

import org.jetbrains.annotations.NotNull;

public class TheRValueModifierFactoryImpl implements TheRValueModifierFactory {

  @NotNull
  @Override
  public TheRValueModifier getModifier() {
    return new TheRValueModifierImpl(); // TODO [dbg][update]
  }
}
