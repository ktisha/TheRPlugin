package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.function.TheRFunctionDebugger;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRFunctionDebugger implements TheRFunctionDebugger {

  @NotNull
  @Override
  public TheRLocation getLocation() {
    throw new IllegalStateException("GetLocation shouldn't be called");
  }

  @Override
  public boolean hasNext() {
    throw new IllegalStateException("HasNext shouldn't be called");
  }

  @Override
  public void advance() throws TheRDebuggerException {
    throw new IllegalStateException("Advance shouldn't be called");
  }

  @NotNull
  @Override
  public String getResult() {
    throw new IllegalStateException("GetResult shouldn't be called");
  }
}