package com.jetbrains.ther.debugger.mock;

import com.jetbrains.ther.debugger.TheRFunctionResolver;
import com.jetbrains.ther.debugger.data.TheRFunction;
import com.jetbrains.ther.debugger.data.TheRLocation;
import org.jetbrains.annotations.NotNull;

public class IllegalTheRFunctionResolver implements TheRFunctionResolver {

  @NotNull
  @Override
  public TheRFunction resolve(@NotNull final TheRLocation currentLocation, @NotNull final String nextFunctionName) {
    throw new IllegalStateException("Resolve shouldn't be called");
  }
}
