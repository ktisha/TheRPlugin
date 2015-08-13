package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.frame.TheRValueModifier;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRValueModifier implements TheRValueModifier {

  @Override
  public boolean isDisabled() {
    throw new IllegalStateException("IsDisabled shouldn't be called");
  }

  @Override
  public void setValue(@NotNull final String name, @NotNull final String value, @NotNull final Listener listener) {
    throw new IllegalStateException("SetValue shouldn't be called");
  }
}
