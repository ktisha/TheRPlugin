package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

interface TheRFunctionDebugger {

  @NotNull
  TheRFunction getFunction();

  int getCurrentLineNumber();

  @NotNull
  List<TheRVar> getVars();

  boolean hasNext();

  void advance() throws IOException, InterruptedException;

  @NotNull
  String getResult();
}
