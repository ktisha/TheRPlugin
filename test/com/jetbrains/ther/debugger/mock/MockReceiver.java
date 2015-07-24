package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class MockReceiver<T> implements TheRDebuggerEvaluator.Receiver<T> { // TODO [dbg][usages]

  @NotNull
  private final T myExpectedResult;

  private int myResultReceived;

  public MockReceiver(@NotNull final T expectedResult) {
    myExpectedResult = expectedResult;
    myResultReceived = 0;
  }

  @Override
  public void receiveResult(@NotNull final T result) {
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
