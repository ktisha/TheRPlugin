package com.jetbrains.ther.debugger.frame;

import org.jetbrains.annotations.NotNull;

// TODO [xdbg][test]
class TheRValueModifierImpl implements TheRValueModifier {

  @Override
  public boolean isDisabled() {
    return true; // TODO [dbg][impl]
  }

  @Override
  public void setValue(@NotNull final String name, @NotNull final String value, @NotNull final Listener listener) {
    if (isDisabled()) {
      throw new IllegalStateException("SetValue could be called only if isDisabled returns false");
    }

    listener.onError("Not implemented"); // TODO [dbg][impl]
  }
}
