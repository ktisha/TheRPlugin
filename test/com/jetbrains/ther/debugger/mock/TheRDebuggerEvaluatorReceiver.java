package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class TheRDebuggerEvaluatorReceiver implements TheRDebuggerEvaluator.Receiver {

  @NotNull
  private final String myExpectedResult;

  private int myResultReceived;

  public TheRDebuggerEvaluatorReceiver(@NotNull final String expectedResult) {
    myExpectedResult = expectedResult;
    myResultReceived = 0;
  }

  @Override
  public void receiveResult(@NotNull final String result) {
    myResultReceived++;

    assertEquals(myExpectedResult, result);
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    throw new IllegalStateException("ReceiveError shouldn't be called");
  }

  @Override
  public void receiveError(@NotNull final String error) {
    throw new IllegalStateException("ReceiveError shouldn't be called");
  }

  public int getResultReceived() {
    return myResultReceived;
  }
}
