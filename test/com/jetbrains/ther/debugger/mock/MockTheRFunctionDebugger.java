package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import com.jetbrains.ther.debugger.function.TheRFunctionDebuggerHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MockTheRFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  private final String myFunctionName;

  private final int myLimit;

  @Nullable
  private final String myResult;

  private int myCounter = 0;

  @Nullable
  private TheRFunctionDebuggerHandler myHandler;

  public MockTheRFunctionDebugger(@NotNull final String functionName, final int limit, @Nullable final String result) {
    myFunctionName = functionName;
    myLimit = limit;
    myResult = result;
  }

  @NotNull
  @Override
  public TheRLocation getLocation() {
    return new TheRLocation(myFunctionName, myCounter);
  }

  @Override
  public boolean hasNext() {
    return myCounter < myLimit;
  }

  @Override
  public void advance() throws TheRDebuggerException {
    myCounter++;
  }

  @NotNull
  @Override
  public String getResult() {
    if (myResult == null) {
      throw new IllegalStateException("GetResult shouldn't be called");
    }

    return myResult;
  }

  @Nullable
  public TheRFunctionDebuggerHandler getHandler() {
    return myHandler;
  }

  public void setHandler(@Nullable final TheRFunctionDebuggerHandler handler) {
    myHandler = handler;
  }

  public int getCounter() {
    return myCounter;
  }
}
