package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionDebugger {

  @NotNull
  TheRLocation getLocation();

  boolean hasNext();

  void advance() throws TheRDebuggerException;

  @NotNull
  String getResult();
}
