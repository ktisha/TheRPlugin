package com.jetbrains.ther.debugger.frame;

import com.jetbrains.ther.debugger.data.TheRVar;
import com.jetbrains.ther.debugger.exception.TheRDebuggerException;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TheRVarsLoader {

  @NotNull
  List<TheRVar> load() throws TheRDebuggerException;

  void markAsLast();

  void markAsNotLast();
}
