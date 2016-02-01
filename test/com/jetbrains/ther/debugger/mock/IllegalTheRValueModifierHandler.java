package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.frame.TheRValueModifierHandler;

public class IllegalTheRValueModifierHandler implements TheRValueModifierHandler {

  @Override
  public boolean isModificationAvailable(final int frameNumber) {
    throw new IllegalStateException("IsModificationAvailable shouldn't be called");
  }

  @Override
  public void setLastFrameNumber(final int lastFrameNumber) {
    throw new IllegalStateException("SetLastFrameNumber shouldn't be called");
  }
}
