package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

public class TheRDebuggerEvaluatorErrorReceiver implements TheRDebuggerEvaluator.Receiver {

  private int myErrorReceived = 0;

  @Override
  public void receiveResult(@NotNull final String result) {
    throw new IllegalStateException("ReceiveResult shouldn't be called");
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    myErrorReceived++;
  }

  @Override
  public void receiveError(@NotNull final String error) {
    myErrorReceived++;
  }

  public int getErrorReceived() {
    return myErrorReceived;
  }
}
