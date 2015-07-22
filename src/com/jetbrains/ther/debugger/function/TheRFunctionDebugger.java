package com.jetbrains.ther.debugger.function;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TheRFunctionDebugger {

  @NotNull
  TheRLocation getLocation();

  @NotNull
  List<TheRVar> getVars();

  boolean hasNext();

  void advance() throws TheRDebuggerException;

  @NotNull
  String getResult();
}
