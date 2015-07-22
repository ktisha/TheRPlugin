package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.evaluator.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class MockExpressionReceiver implements TheRDebuggerEvaluator.ExpressionReceiver {

  @NotNull
  private final String myText;

  private int myResultReceived;

  public MockExpressionReceiver(@NotNull final String text) {
    myText = text;
    myResultReceived = 0;
  }

  @Override
  public void receiveResult(@NotNull final String result) {
    myResultReceived++;

    assertEquals(myText, result);
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    throw new IllegalStateException("ReceiveError shouldn't be called");
  }

  public int getResultReceived() {
    return myResultReceived;
  }
}
