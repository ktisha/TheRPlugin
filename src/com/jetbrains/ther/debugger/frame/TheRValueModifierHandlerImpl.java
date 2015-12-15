package com.jetbrains.ther.debugger.frame;

public class TheRValueModifierHandlerImpl implements TheRValueModifierHandler {

  private int myMaxFrameNumber = 0;

  @Override
  public boolean isModificationAvailable(final int frameNumber) {
    return myMaxFrameNumber == frameNumber;
  }

  @Override
  public void setMaxFrameNumber(final int maxFrameNumber) {
    myMaxFrameNumber = maxFrameNumber;
  }
}
