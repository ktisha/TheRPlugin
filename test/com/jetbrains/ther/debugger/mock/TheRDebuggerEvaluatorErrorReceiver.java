package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

public class TheRDebuggerEvaluatorErrorReceiver implements TheRDebuggerEvaluator.Receiver {

  private int myCounter = 0;

  @Override
  public void receiveResult(@NotNull final String result) {
    throw new IllegalStateException("ReceiveResult shouldn't be called");
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    myCounter++;
  }

  @Override
  public void receiveError(@NotNull final String error) {
    myCounter++;
  }

  public int getCounter() {
    return myCounter;
  }
}
