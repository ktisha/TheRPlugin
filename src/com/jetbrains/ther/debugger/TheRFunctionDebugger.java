package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface TheRFunctionDebugger {

  int getCurrentLineNumber();

  @NotNull
  List<TheRVar> getVars();

  void advance() throws IOException, InterruptedException;
}
