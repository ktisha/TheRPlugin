package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionResolver {

  @NotNull
  TheRFunction resolve(@NotNull final TheRFunction currentFunction, @NotNull final String nextFunctionName);
}
