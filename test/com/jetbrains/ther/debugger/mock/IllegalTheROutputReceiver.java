package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheROutputReceiver;
import org.jetbrains.annotations.NotNull;

public class IllegalTheROutputReceiver implements TheROutputReceiver {

  @Override
  public void receiveOutput(@NotNull final String output) {
    throw new IllegalStateException("ReceiverOutput shouldn't be called");
  }

  @Override
  public void receiveError(@NotNull final String error) {
    throw new IllegalStateException("ReceiverError shouldn't be called");
  }
}
