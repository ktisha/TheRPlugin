package com.jetbrains.ther.debugger;

import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;

public interface TheRFunctionResolver {

  @NotNull
  TheRFunction resolve(@NotNull final TheRLocation currentLocation, @NotNull final String nextFunctionName);
}
