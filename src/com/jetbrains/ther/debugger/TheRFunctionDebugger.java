package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRLocation;
import com.jetbrains.ther.debugger.data.TheRVar;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

public interface TheRFunctionDebugger {

  @NotNull
  TheRLocation getLocation();

  @NotNull
  List<TheRVar> getVars();

  boolean hasNext();

  void advance() throws IOException, InterruptedException;

  @NotNull
  String getResult();
}
