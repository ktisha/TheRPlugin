package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class TheRDebuggerEvaluatorConditionReceiver implements TheRDebuggerEvaluator.Receiver<Boolean> {

  private final boolean myExpectedResult;

  private int myResultReceived;

  public TheRDebuggerEvaluatorConditionReceiver(final boolean result) {
    myExpectedResult = result;
    myResultReceived = 0;
  }

  @Override
  public void receiveResult(@NotNull final Boolean result) {
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
