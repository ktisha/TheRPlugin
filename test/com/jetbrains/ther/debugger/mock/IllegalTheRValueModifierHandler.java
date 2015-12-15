package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.frame.TheRValueModifierHandler;

public class IllegalTheRValueModifierHandler implements TheRValueModifierHandler {

  @Override
  public boolean isModificationAvailable(final int frameNumber) {
    throw new IllegalStateException("IsModificationAvailable shouldn't be called");
  }

  @Override
  public void setMaxFrameNumber(final int maxFrameNumber) {
    throw new IllegalStateException("SetMaxFrameNumber shouldn't be called");
  }
}
