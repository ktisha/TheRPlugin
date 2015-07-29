package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import com.jetbrains.ther.debugger.frame.TheRVarsLoader;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class EmptyTheRVarsLoader implements TheRVarsLoader {

  @NotNull
  @Override
  public List<TheRVar> load() throws TheRDebuggerException {
    return Collections.emptyList();
  }
}
