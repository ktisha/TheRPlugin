package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRVar;
import com.jetbrains.ther.debugger.frame.TheRVarsLoader;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IllegalTheRVarsLoader implements TheRVarsLoader {

  @NotNull
  @Override
  public List<TheRVar> load() throws TheRDebuggerException {
    throw new IllegalStateException("Load shouldn't be called");
  }
}
