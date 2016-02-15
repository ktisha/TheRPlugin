package com.jetbrains.ther.debugger.frame;

public class TheRValueModifierHandlerImpl implements TheRValueModifierHandler {

  private int myLastFrameNumber = 0;

  @Override
  public boolean isModificationAvailable(final int frameNumber) {
    return myLastFrameNumber == frameNumber;
  }

  @Override
  public void setLastFrameNumber(final int lastFrameNumber) {
    myLastFrameNumber = lastFrameNumber;
  }
}
