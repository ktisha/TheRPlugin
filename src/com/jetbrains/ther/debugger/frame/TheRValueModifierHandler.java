package com.jetbrains.ther.debugger.frame;

public interface TheRValueModifierHandler {

  boolean isModificationAvailable(final int frameNumber);

  void setLastFrameNumber(final int lastFrameNumber);
}
