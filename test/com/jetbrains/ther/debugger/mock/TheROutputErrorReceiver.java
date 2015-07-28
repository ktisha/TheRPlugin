package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class TheROutputErrorReceiver implements TheROutputReceiver {

  @NotNull
  private final String myExpectedError;

  private int myErrorReceived;

  public TheROutputErrorReceiver(@NotNull final String expectedError) {
    myExpectedError = expectedError;
    myErrorReceived = 0;
  }

  @Override
  public void receiveOutput(@NotNull final String output) {
    throw new IllegalStateException("ReceiveOutput shouldn't be called");
  }

  @Override
  public void receiveError(@NotNull final String error) {
    myErrorReceived++;

    assertEquals(myExpectedError, error);
  }

  public int getErrorReceived() {
    return myErrorReceived;
  }
}
