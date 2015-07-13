package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

import static org.junit.Assert.assertEquals;

public class MockExpressionReceiver implements TheRDebuggerEvaluator.ExpressionReceiver {

  @NotNull
  private final String myText;

  private boolean myResultReceived;

  public MockExpressionReceiver(@NotNull final String text) {
    myText = text;
    myResultReceived = false;
  }

  @Override
  public void receiveResult(@NotNull final String result) {
    myResultReceived = true;

    assertEquals(myText, result);
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    throw new IllegalStateException("ReceiveError shouldn't be called");
  }

  public boolean isResultReceived() {
    return myResultReceived;
  }
}
