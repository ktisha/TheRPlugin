package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheRDebuggerEvaluator;
import org.jetbrains.annotations.NotNull;

public class ErrorExpressionReceiver implements TheRDebuggerEvaluator.ExpressionReceiver {

  private boolean myErrorReceived = false;

  @Override
  public void receiveResult(@NotNull final String result) {
    throw new IllegalStateException("ReceiveResult shouldn't be called");
  }

  @Override
  public void receiveError(@NotNull final Exception e) {
    myErrorReceived = true;
  }

  public boolean isErrorReceived() {
    return myErrorReceived;
  }
}
