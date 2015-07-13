package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class MockConditionReceiver implements TheRDebuggerEvaluator.ConditionReceiver {

  private final boolean myExpectedResult;

  private int myResultReceived;

  public MockConditionReceiver(final boolean result) {
    myExpectedResult = result;
    myResultReceived = 0;
  }

  @Override
  public void receiveResult(final boolean result) {
    myResultReceived++;

    assertEquals(myExpectedResult, result);
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    throw new IllegalStateException("ReceiveError shouldn't be called");
  }

  public int getResultReceived() {
    return myResultReceived;
  }
}
